package com.veeteq.auth.authservice.rest.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping(path = "/api/hello")
	public String hello() {
		return "Hello!";
	}
}
