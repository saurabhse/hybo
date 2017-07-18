
package com.hackovation.hybo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages={"com.hack17.hybo","com.hackovation.hybo"})
public class HyboApplication {
	

	public static void main(String[] args) {
		SpringApplication.run(HyboApplication.class, args);
/*		BasedOnThreshold based = new BasedOnThreshold();
		based.rebalance();
*/	}
}