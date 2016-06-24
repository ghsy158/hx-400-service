package com.huaxia.call400.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huaxia.call400.dao.Call400Dao;
import com.huaxia.call400.facade.Call400Facade;

@Service("call400Service")
@com.alibaba.dubbo.config.annotation.Service(interfaceClass = com.huaxia.call400.facade.Call400Facade.class, protocol = { "rest",
		"dubbo" })
public class Call400Service implements Call400Facade {

	@Autowired
	private Call400Dao call400Dao;

	@Override
	public String get400Calls() throws Exception {
		return call400Dao.query400Calls();
	}

	@Override
	public void batchInsert2Opp(String json) throws Exception {
		call400Dao.batchInsert2Opp(json);
	}
}
