package com.example.test;

import java.security.Principal;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class WebSocketController {
	
	@RequestMapping(value="", method=RequestMethod.GET )
	public ModelAndView main(ModelAndView mav, Principal pric) {
		mav.setViewName("main");
		return mav;
	}
	
	@RequestMapping("/webSocket")
	public ModelAndView chat(ModelAndView mav) {
		mav.setViewName("chat");
		return mav;
		
	}
	
}
   