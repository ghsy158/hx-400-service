package com.huaxia.call400.dao;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import fgh.common.dao.BaseJdbcDao;

/**
 * 
 * <b>系统名称：</b>400 DAO，查询400工单信息，并插入到ERP商机表,具体的数据库操作<br>
 * <b>模块名称：</b><br>
 * <b>中文类名：</b><br>
 * <b>概要说明：</b><br>
 * 
 * @author fgh
 * @since 2016年6月26日上午10:14:04
 */
@Repository("call400Dao")
public class Call400Dao extends BaseJdbcDao {

	private static final String TABLE_NAME = "pawn_busiOpportunity";
	private static final String SQL_SELECT_400_CALLS = "SELECT a.CASEID uuid,ISNULL(a.YWLX,'') bizType,	ISNULL(a.YWXX,'') mortgageName,ISNULL(a.HUIDA,'') loanIntention,ISNULL(a.UNAME, '') customerName,"
			+ " ISNULL(a.CALLER,'') customerMoile,ISNULL(a.DHLY,'') chnl400,ISNULL(a.JRMD,'') storeName400,"
			+ " ISNULL(a.KHWZ,'') customerLocation,'400user' createUser,'01' chnlType,'2' dealStatus,"
			+ " ISNULL(SUBSTRING (a.SDATE, 1, 4) + '-' + SUBSTRING (a.SDATE, 5, 2) + '-' + SUBSTRING (a.SDATE, 7, 2) + ' ' + SUBSTRING (a.STIME, 1, 2) + ':' + SUBSTRING (a.SDATE, 3, 2) + ':' + SUBSTRING (a.SDATE, 5, 2),CONVERT(varchar(100), GETDATE(),120)) AS createTime "
			+ " FROM CallThinck_CRM.dbo.CRM_CASE2 a";

	private static final String SQL_SELECT_400_CALLS_TODAY = SQL_SELECT_400_CALLS
			+ " where a.SDATE=convert(varchar, getdate(), 112)";
	
	private static final String SQL_SELECT_ERP_CALLS_TODAY = "SELECT a.uuid uuid,	IFNULL(a.bizType, '') bizType,IFNULL(a.mortgageName, '') mortgageName,"
			+ " IFNULL(a.loanIntention, '') loanIntention,IFNULL(a.customerName, '') customerName,IFNULL(a.customerMoile, '') customerMoile"
//			+ "	IFNULL(a.chnl400, '') chnl400,IFNULL(a.storeName400, '') storeName400,IFNULL(a.customerLocation, '') customerLocation,"
//			+ " IFNULL(a.createUser, '') createUser,IFNULL(a.chnlType, '') chnlType,IFNULL(a.dealStatus, '') dealStatus"
			+ " FROM  pawn_busiOpportunity a"
			+ " WHERE DATE_FORMAT(a.createTime, '%Y-%m-%d') = DATE_FORMAT(SYSDATE(), '%Y-%m-%d')"; 

	// private static final String INSERT_SQL = "INSERT INTO
	// pawn_busiOpportunity
	// (uuid,bizType,mortgageName,loanIntention,customerName,customerMoile,chnlType,chnl400,reply400,storeName400,customerLocation,createUser,createTime)
	// VALUES(?,?,?,?,?,?,'01',?,?,?,?,?, NOW()) ";

	/**
	 * <b>方法名称：</b>查询400所有工单信息<br>
	 * <b>概要说明：</b><br>
	 */
	public String query400CallsAll() throws Exception {
		return super.queryForJsonListString(SQL_SELECT_400_CALLS);
	}

	/**
	 * 
	 * <b>方法名称：</b>批量插入工单信息到ERP商机表<br>
	 * <b>概要说明：</b><br>
	 */
	public void batchInsert2Opp(String data) {
		List<LinkedHashMap<String, Object>> list = JSON.parseObject(data,
				new TypeReference<List<LinkedHashMap<String, Object>>>() {
				});
		// System.out.println(list);
		// super.insert(SQL_TABLE_NAME,
		// FastJsonConvert.convertObjectToJSONObject(list.get(0)));
		super.batchInsertByTableName(TABLE_NAME, list);
	}

	/**
	 * 
	 * <b>方法名称：</b>查询400工单当天的数据，并返回list<br>
	 * <b>概要说明：</b><br>
	 */
	public List<JSONObject> query400CallsToday() throws Exception {
		return super.queryForJsonList(SQL_SELECT_400_CALLS_TODAY);
	}

	/**
	 * 
	 * <b>方法名称：</b>单笔插入工单信息到ERP商机表<br>
	 * <b>概要说明：</b><br>
	 */
	public int singleInsert2Opp(JSONObject jsonObject) {
		return super.insert(TABLE_NAME, jsonObject);
	}
	
	/**
	 * 
	 * <b>方法名称：</b>查询ERP工单当天的数据，并返回list<br>
	 * <b>概要说明：</b><br>
	 */
	public List<JSONObject> queryERPCallsToday() throws Exception {
		return super.queryForJsonList(SQL_SELECT_ERP_CALLS_TODAY);
	}
	
	/**
	 * 
	 * <b>方法名称：</b>更新ERP信息<br>
	 * <b>概要说明：</b><br>
	 */
	public int updateERPInfo(JSONObject jsonObject, JSONObject whereKey) throws Exception{
		return super.update(TABLE_NAME, jsonObject, whereKey);
	}
}
