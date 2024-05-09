package com.example.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeHandler;

import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{

	/**@author Shinsungjin
	 * 
	 * 웹 소켓에 대한 기본적인 경로 설정을 하는 부분
	 * webSocket을 컨트롤 할 handler를 선언하고, webSocket 기본 경로를 지정한다. 
	 * 대부분의 WebSocket 기능은, CustomWebSocket부분에서 구현
	 * 
	 * 
	 * *setAllowedOrigins : 웹소켓 cors 정책으로인해, 허용 도메인 지정.
	 * */
		
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(new CustomWebSocketHandler(),"/webSocketTest")
		.setHandshakeHandler(new CustomHandShakeHandler())
		.setAllowedOrigins("*");
	}
		
}
