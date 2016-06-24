package com.huaxia.call400.dao;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import fgh.common.dao.BaseJdbcDao;

@Repository("call400Dao")
public class Call400Dao extends BaseJdbcDao {

	private static final String SQL_TABLE_NAME = "pawn_busiOpportunity";
	private static final String SQL_SELECT_400_CALLS = "SELECT a.CASEID uuid,ISNULL(a.YWLX,'') bizType,	ISNULL(a.YWXX,'') mortgageName,ISNULL(a.HUIDA,'') loanIntention,ISNULL(a.UNAME, '') customerName,"
			+ " ISNULL(a.CALLER,'') customerMoile,ISNULL(a.DHLY,'') chnl400,ISNULL(a.JRMD,'') storeName400,"
			+ " ISNULL(a.KHWZ,'') customerLocation,'400user' createUser,'01' chnlType,"
			+ "ISNULL(SUBSTRING (a.SDATE, 1, 4) + '-' + SUBSTRING (a.SDATE, 5, 2) + '-' + SUBSTRING (a.SDATE, 7, 2) + ' ' + SUBSTRING (a.STIME, 1, 2) + ':' + SUBSTRING (a.SDATE, 3, 2) + ':' + SUBSTRING (a.SDATE, 5, 2),CONVERT(varchar(100), GETDATE(),120)) AS createTime"
			+ " FROM CallThinck_CRM.dbo.CRM_CASE2 a";

//	private static final String INSERT_SQL = "INSERT INTO pawn_busiOpportunity (uuid,bizType,mortgageName,loanIntention,customerName,customerMoile,chnlType,chnl400,reply400,storeName400,customerLocation,createUser,createTime) VALUES(?,?,?,?,?,?,'01',?,?,?,?,?, NOW()) ";

	/**
	 * <b>方法名称：</b>查询400工单信息<br>
	 * <b>概要说明：</b><br>
	 */
	public String query400Calls() throws Exception {
		return super.queryForJsonListString(SQL_SELECT_400_CALLS);
	}

	public void batchInsert2Opp(String data) {
		List<LinkedHashMap<String, Object>> list = JSON.parseObject(data,
				new TypeReference<List<LinkedHashMap<String, Object>>>() {
				});
//		System.out.println(list);
//		super.insert(SQL_TABLE_NAME, FastJsonConvert.convertObjectToJSONObject(list.get(0)));
		super.batchInsertByTableName(SQL_TABLE_NAME, list);
	}

}
