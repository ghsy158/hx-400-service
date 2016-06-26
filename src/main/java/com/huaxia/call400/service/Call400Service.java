package com.huaxia.call400.service;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.huaxia.call400.dao.Call400Dao;
import com.huaxia.call400.facade.Call400Facade;

/**
 * 
 * 
 * <b>系统名称：</b>400serce，查询400工单信息，并插入到ERP商机表<br>
 * <b>模块名称：</b><br>
 * <b>中文类名：</b><br>
 * <b>概要说明：</b><br>
 * @author fgh
 * @since 2016年6月26日上午10:13:15
 */
@Service("call400Service")
@com.alibaba.dubbo.config.annotation.Service(interfaceClass = com.huaxia.call400.facade.Call400Facade.class, protocol = { "rest",
		"dubbo" })
public class Call400Service implements Call400Facade {

	@Autowired
	private Call400Dao call400Dao;

	@Override
	public String get400Calls() throws Exception {
		return call400Dao.query400CallsAll();
	}

	@Override
	public void batchInsert2Opp(String json) throws Exception {
		call400Dao.batchInsert2Opp(json);
	}

	@Override
	public List<JSONObject> query400CallsToday() throws Exception {
		return call400Dao.query400CallsToday();
	}

	@Override
	public int singleInsert2Opp(JSONObject jsonObject) throws Exception {
		return call400Dao.singleInsert2Opp(jsonObject);
	}

	@Override
	public Date getCurrentTime() {
		return call400Dao.getCurrentTime();
	}

}
