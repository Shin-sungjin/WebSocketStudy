package com.example.test;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.rsocket.server.RSocketServer.Transport;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class WebSocketController {
	
	@RequestMapping(value="", method=RequestMethod.GET )
	public ModelAndView main(ModelAndView mav, Principal pric) {
		mav.setViewName("main");
		return mav;
	}
	 
	@RequestMapping(value = "/webSocket", method=RequestMethod.GET)
	public ModelAndView chat(ModelAndView mav) {
		/** @auto : Shinsungjijn 
		 *  @Date : 2024.05.12
		 *   어제까지 잘 이동하는 chat page 인데 white label 에러가 뜸
		 *  @Solution : WebSocketSession 을 파라미터로 받고 있어서 그랫음,,,
		 * */
		mav.setViewName("chat");
		return mav; 
	}
	
	@RequestMapping(value = "webSocket/chat", method=RequestMethod.GET)
	public ModelAndView chatLi(ModelAndView mav) {
		mav.setViewName("chatroom");
		return mav; 
	}
}
   