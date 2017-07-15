
package com.hackovation.hybo;


import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import com.hackovation.hybo.scheduled.BasedOnThreshold;



@SpringBootApplication
@EnableScheduling
public class HyboApplication {
	

	public static void main(String[] args) {
		SpringApplication.run(HyboApplication.class, args);
/*		BasedOnThreshold based = new BasedOnThreshold();
		based.rebalance();
*/	}
}