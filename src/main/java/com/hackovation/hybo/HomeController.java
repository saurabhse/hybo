
package com.hackovation.hybo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class HomeController {
	
	@Autowired
	private UsersService userService;

	@RequestMapping(value = "/")
	public String index() {
		userService.listAllUsers();
		return "index";
	}

}
