package com.huaxia.call400.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.huaxia.call400.facade.Call400Facade;

import fgh.common.datasource.MultipleDataSource;

@Component
public class Sync400CallsTask {

	@Autowired
	private Call400Facade call400Facade;

	@Scheduled(cron = "0/1 * *  * * ? ")
	public void sync400Calls() throws Exception {
		MultipleDataSource.setDataSourceKey("sqlServerDataSource");
		String json = call400Facade.get400Calls();
//		System.out.println("同步的数据为：" + json);
		
		MultipleDataSource.setDataSourceKey("mySqlDataSource");
		System.out.println("插入到数据库");
		call400Facade.batchInsert2Opp(json);
	}

}
