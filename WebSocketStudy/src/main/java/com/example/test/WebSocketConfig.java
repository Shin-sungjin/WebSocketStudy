package com.example.test;

import java.net.http.HttpClient;

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
import lombok.extern.log4j.Log4j2;

@Log4j2
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
		
	/** @author : Shinsungjin 
	 *  @Date : 2024/05/13
	 *  
	 * 아래 addHandelr의 path는 /chat이다. 
	 * 하지만 ~/websocket ㄹ주소로 들어가면, 웹소켓이 생성된다. 
	 * chat.html 에서 webSocket을 스크립트 형식으로 불러오고있다. 
	 * 또, new WebSocket(~~아래 핸들러의 패스값을 사용한다)
	 * 
	 * 즉 여기서 핸들러 이후에 오는 path값은, 웹 소켓을 사요하는 page에서, 새로운 웹 소켓 링크를 만들어내기 위한 
	 * 주소값으로 사용된다고 이해하면 될 것 같다. 
	 * 
	 *	또 오늘 ClientLibrayURl 을 추가 했느데, 이제 /chat 링크로 가면, "Welcome to SockJS" 가 뜬다..흠
	 *
	 * 변경사항2
	 *  setAlloweOrign -> setAllowedOrignPatterns() 
	 * */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(myhandler(),"/chat")
		.setHandshakeHandler(new CustomHandShakeHandler())
		.setAllowedOriginPatterns("*").withSockJS()
		.setClientLibraryUrl("http://localhost:8081/WebSocketStudy/js/sockjs-client.js");
	}
		
	@Bean 
	public WebSocketHandler myhandler() {
	
		return new CustomWebSocketHandler();
		
	}
}
