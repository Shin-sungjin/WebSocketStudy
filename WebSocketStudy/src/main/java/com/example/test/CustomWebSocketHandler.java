package com.example.test;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class CustomWebSocketHandler implements WebSocketHandler {

	/**
	 * @implNote WebSocketHandler
	 * WebSocketHandler는 WebSocket 처리를 위한 인터페이스 구현
	 *  특정 타입의 WebSocket 메시지를 처리할 수 있는 다양한 기능을 제공합니다.
	 *  CL - Server간 WebSocket 연결 관리, 메세지 처리
	 * @author Shinsungjin 
	 * */
	
	
	/** WebSocket 연결 성공 시 사용할 준비가 완료 될 때 호출 될 메소드 
	 *  @param WebSocketSession
	 * */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// TODO Auto-generated method stub
	}

	/** 새로운 WebSocket 메시지가 도착했을 때 호출, 
	 *  수식 된 메세지 처리 및 응답 생성, 
	 *  ex) 특정 메시지 유형 파싱 -> 그에 대한 응답 생성
	 *  @param WebSocketSession ㅡ WebSocketMessage
	 * */
	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/** 웹소켓 오류 처리 
	 * 연결이 유실 or 오류 발생 시 대처 방법
	 * */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		// TODO Auto-generated method stub
		
	}

	
	/**연결 종료 이후 호출 
	 * 정리 작업 등 ㄷ
	 * DB 저장에 사용을 하거나, 저장 처리 하면 될 것 같다 
	 * */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		
	}

	/**
	 * 이 메소드는 WebSocketHandler가 부분 메세지를 처리하느지에 대한 여부 
	 * 부분 메세지 처리시 True , 아닐 경우 False 
	 *  실시간 구현이라면 True가 적절해 보임
	 *  
	 * */
	@Override
	public boolean supportsPartialMessages() {
		boolean ck =true;
		return ck;
	}

}
