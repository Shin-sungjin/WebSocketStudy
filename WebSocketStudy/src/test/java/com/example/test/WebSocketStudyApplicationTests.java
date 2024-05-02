package com.example.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
class WebSocketStudyApplicationTests {

	@Autowired
	CustomWebSocketHandler wsHandler;
	
	@Test
	void contextLoads() {
	
	
	}

}
