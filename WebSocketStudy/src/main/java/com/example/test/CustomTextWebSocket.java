package com.example.test;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class CustomTextWebSocket extends TextWebSocketHandler{


 
	/**@author Shinsungjin 
	 * 
	 *  주로 텍스트 기반의 메시지를 처리하는 데 사용됩니다.
	 *  CL -> Server or Server <- CL 간 텍스트 메세지 전송 
	 *  
	 *  생각해보니 이번 프로젝트는 실시간 공유 문서 스트리밍이 목적이기에 TEXT기반 메세지 처리가 유용할까? 라는 의문이든다. 
	 * */

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		// TODO Auto-generated method stub
		super.handleBinaryMessage(session, message);
	}
	
}

