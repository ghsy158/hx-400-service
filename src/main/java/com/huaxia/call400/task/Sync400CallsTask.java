package com.huaxia.call400.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.huaxia.call400.facade.Call400Facade;

import fgh.common.datasource.MultipleDataSource;
import fgh.common.util.FastJsonConvert;

@Component
public class Sync400CallsTask {

	private static Logger logger = Logger.getLogger(Sync400CallsTask.class);
	
	/**存放当天已经同步的工单ID**/
	private Set<String> caseIdSet = new HashSet<String>(128);
	/**存放当天同步失败的工单ID**/
	private Set<String> caseIdSetFailed = new HashSet<String>(128);

	@Autowired
	private Call400Facade call400Facade;

	/**
	 * 
	 * <b>方法名称：</b>批量同步自动任务<br>
	 * <b>概要说明：</b><br>
	 */
//	@Scheduled(cron = "0/1 * *  * * ? ")
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
	 * @throws Exception 
	 */
	@Scheduled(cron = "0/5 * *  * * ? ")
	public synchronized void sync400CallsToday(){
		logger.info("同步400工单信息");
		MultipleDataSource.setDataSourceKey("sqlServerDataSource");//400数据源
		List<JSONObject> todayCaseList = null;;
		try {
			todayCaseList = call400Facade.query400CallsToday();
		} catch (Exception e1) {
			logger.error("查询当天工单失败",e1);
		}
		
		MultipleDataSource.setDataSourceKey("mySqlDataSource");//ERP数据源
		for(JSONObject jsonObject:todayCaseList){
			//先看下这笔工单是否已经同步过，如果已经同步过，不处理
			String caseId = jsonObject.getString("uuid");
			if(caseIdSet.contains(caseId)|| caseIdSetFailed.contains(caseId)){
				logger.info("该工单已同步,工单id["+caseId+"]");
				continue;
			}else{
				int count =0;;
				try {
					logger.info("同步工单:"+FastJsonConvert.convertObjectToJSON(jsonObject));
					count = call400Facade.singleInsert2Opp(jsonObject);
					//如果插入到ERP成功，把他缓存起来，下次不再入库
					if(count>0){
						caseIdSet.add(caseId);
					}else{
						logger.info("该工单同步到ERP数据库失败,工单id["+caseId+"]");
					}
				} catch (Exception e) {
					if(e.getMessage().indexOf("Duplicate entry")>-1){
						caseIdSet.add(caseId);
						logger.info("该工单已同步,工单id["+caseId+"]");
					}else{
						caseIdSetFailed.add(caseId);
						logger.info("该工单同步到ERP数据库失败,需要手工处理,工单id["+caseId+"]");
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * <b>方法名称：</b>清空缓存的工单ID<br>
	 * <b>概要说明：</b><br>
	 */
	@Scheduled(cron = "0 30 23 * * ?")
//	@Scheduled(cron = "0/10 * *  * * ? ")
	public void clearCaseIdSet(){
		logger.info("清空缓存的工单ID["+call400Facade.getCurrentTime()+"]");
		caseIdSet.clear();
	}
	
	public static void main(String[] args) {
//		List<String> caseList = new LinkedList<String>();
		Set<String> set = new HashSet<String>();
		// caseList.add("123");
		// caseList.add("123");
		// caseList.add("123");
		set.add("123");
		set.add("123");
		set.add("123");
		set.add("123");
		System.out.println(set);
	}

}
