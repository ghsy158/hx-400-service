package com.huaxia.call400.task;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.huaxia.call400.facade.Call400Facade;

import fgh.common.datasource.MultipleDataSource;
import fgh.common.util.FastJsonConvert;

/**
 * 
 * <b>系统名称：</b>同步400工单信息到ERP商机表<br>
 * <b>模块名称：</b><br>
 * <b>中文类名：</b><br>
 * <b>概要说明：</b><br>
 * 
 * @author fgh
 * @since 2016年6月29日上午9:12:40
 */
@Component
public class Sync400CallsTask {

	private static Logger logger = Logger.getLogger(Sync400CallsTask.class);

	/** 存放当天已经同步的工单ID **/
	private Set<String> caseIdSet = new HashSet<String>(128);

	/** 存放当天同步失败的工单ID **/
	private Set<String> caseIdSetFailed = new HashSet<String>(128);

	/** 存放当天400工单记录，用来比较是否有变化 **/
//	private ConcurrentHashMap<String, JSONObject> todayCaseMap400 = new ConcurrentHashMap<String, JSONObject>();

	/** 存放当天ERP工单记录，用来比较是否有变化 **/
	private ConcurrentHashMap<String, JSONObject> todayCaseMapERP = new ConcurrentHashMap<String, JSONObject>();

	@Autowired
	private Call400Facade call400Facade;

	/**
	 * 
	 * <b>方法名称：</b>批量同步自动任务<br>
	 * <b>概要说明：</b><br>
	 */
	// @Scheduled(cron = "0/1 * * * * ? ")
	public void sync400Calls() throws Exception {
		MultipleDataSource.setDataSourceKey("sqlServerDataSource");
		String json = call400Facade.get400Calls();
		// System.out.println("同步的数据为：" + json);

		MultipleDataSource.setDataSourceKey("mySqlDataSource");
		System.out.println("插入到数据库");
		call400Facade.batchInsert2Opp(json);
	}

	/**
	 * 
	 * <b>方法名称：</b>同步当天的400工单<br>
	 * <b>概要说明：</b><br>
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "0/30 * * * * ?")
	public synchronized void sync400CallsToday() {
//		logger.info("同步400工单信息");
		List<JSONObject> todayCaseList400 = null;
		try {
			todayCaseList400 = call400Facade.query400CallsToday();
		} catch (Exception e1) {
			logger.error("查询400当天工单失败", e1);
		}
		
		if(null == todayCaseList400 || todayCaseList400.isEmpty()){
			return;
		}
		
//		MultipleDataSource.setDataSourceKey("mySqlDataSource");// ERP数据源
		// 查询ERP数据
		List<JSONObject> todayCaseListERP = null;

		try {
			todayCaseListERP = call400Facade.queryERPCallsToday();
		} catch (Exception e1) {
			logger.error("查询ERP当天工单失败", e1);
		}
		
		convertERP(todayCaseListERP);
		
		JSONObject caseERP = null;
		for (JSONObject case400 : todayCaseList400) {
			String caseId = case400.getString("uuid");
			// 先看下这笔工单是否已经同步过，如果已经同步过，不处理
			if (caseIdSet.contains(caseId) || caseIdSetFailed.contains(caseId)) {
				//判断有没有更新
				caseERP = todayCaseMapERP.get(caseId);
				// 比较数据是否有变化，如果有变化，更新ERP，否则不处理
				if(isUpdate(case400, caseERP)){
					logger.info("该工单已同步,数据有变化,需要更新ERP,工单id[" + caseId + "]");
					case400.remove("uuid");
					JSONObject whereKey = new JSONObject();
					whereKey.put("uuid", caseId);
					try {
						call400Facade.updateERPInfo(case400, whereKey);
					} catch (Exception e1) {
						logger.info("更新ERP失败,工单id[" + caseId + "]",e1);
					}
				}else{
//					logger.info("该工单已同步,没有变化,工单id[" + caseId + "]");
				}
			} else {
				int count = 0;
				try {
					logger.info("同步工单:" + FastJsonConvert.convertObjectToJSON(case400));
					count = call400Facade.singleInsert2Opp(case400);
					// 如果插入到ERP成功，把他缓存起来，下次不再入库
					if (count > 0) {
						caseIdSet.add(caseId);
					} else {
						logger.info("该工单同步到ERP数据库失败,工单id[" + caseId + "]");
					}
				} catch (Exception e) {
					if (e.getMessage().indexOf("Duplicate entry") > -1) {
						caseIdSet.add(caseId);
//						logger.info("该工单已同步,工单id[" + caseId + "]");
					} else {
						caseIdSetFailed.add(caseId);
						logger.info("该工单同步到ERP数据库失败,需要手工处理,工单id[" + caseId + "]");
					}
				}
			}
		}
	}

	/**
	 * 
	 * <b>方法名称：</b>启动时查询ERP当天数据，如果已有，添加到已处理的集合中，防止一开始就执行插入操作而报错<br>
	 * <b>概要说明：</b><br>
	 */
//	private void initData(){
//		try {
//			List<JSONObject> todayCaseListERP = call400Facade.queryERPCallsToday();
//			for(JSONObject data:todayCaseListERP){
//				caseIdSet.add(data.getString("uuid"));
//			}
//		} catch (Exception e) {
//			logger.error("查询ERP当天工单失败", e);
//		}
//	}
	
	/**
	 * 
	 * <b>方法名称：</b>判断数据是否变化<br>
	 * <b>概要说明：</b><br>
	 */
	private boolean isUpdate(JSONObject data400,JSONObject dataERP){
		//判断数据是否变化
		int count = 0;
		Set<Entry<String,Object>> set = dataERP.entrySet();//400
		for(Iterator<Entry<String,Object>> iterator = set.iterator();iterator.hasNext();){
			Entry<String,Object> entryERP = iterator.next();
			String key = entryERP.getKey();
			String value400 = String.valueOf(entryERP.getValue());
			String erpValue = String.valueOf(data400.get(key));
			if(!value400.equals(erpValue)){
				count++;
			}
//			if("bizType".equals(key)||"mortgageName".equals(key)||"loanIntention".equals(key)|| "customerName".equals(key)
//					|| "customerMoile".equals(key)){
//			}else{
//				continue;
//			}
		}
		if(count>0){
			logger.info("******************该工单已修改,工单ID"+data400.get("uuid")+"******************");
			logger.info("*******修改前*******"+FastJsonConvert.convertObjectToJSON(dataERP));
			logger.info("*******修改后*******"+FastJsonConvert.convertObjectToJSON(data400));
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * <b>方法名称：</b>转换key是工单ID，value是数据集合<br>
	 * <b>概要说明：</b><br>
	 */
	private void convertERP(List<JSONObject> list){
		if(null==list){
			return;
		}
		for (JSONObject jsonObject : list) {
			String caseId = jsonObject.getString("uuid");
			todayCaseMapERP.put(caseId, jsonObject);
		}
	}
	
	/**
	 * 
	 * <b>方法名称：</b>清空缓存的工单ID<br>
	 * <b>概要说明：</b><br>
	 */
	@Scheduled(cron = "0 30 23 * * ?")
	public void clearCaseIdSet() {
		logger.info("清空缓存的工单ID");
		caseIdSet.clear();
		caseIdSetFailed.clear();
		todayCaseMapERP.clear();
	}
}
