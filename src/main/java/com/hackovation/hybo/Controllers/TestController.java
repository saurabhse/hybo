package com.hackovation.hybo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hack17.hybo.domain.UserClientMapping;
import com.hackovation.hybo.bean.ProfileResponse;
import com.hackovation.hybo.rebalance.Rebalance;

@RestController
@RequestMapping(value="/test")
@CrossOrigin
public class TestController {
	
	@Autowired
	Rebalance rebalance;
	
	@RequestMapping(value="/rebalanceTest", method=RequestMethod.GET)
	public void testRebalance(){
		rebalance.test();
	}
}
