package com.example.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@Component
public class CustomWebSocketHandler implements WebSocketHandler {

	/**
	 * @implNote WebSocketHandler
	 * WebSocketHandler는 WebSocket 처리를 위한 인터페이스 구현
	 *  특정 타입의 WebSocket 메시지를 처리할 수 있는 다양한 기능을 제공합니다.
	 *  CL - Server간 WebSocket 연결 관리, 메세지 처리
	 * @author Shinsungjin 
	 * */
	
	
	/* 웹 소켓 활성화 session 관리 */
	private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
		
	
	
	/** WebSocket 연결 성공 시 사용할 준비가 완료 될 때 호출 될 메소드 
	 *  @param WebSocketSession
	 * */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		//여기로 들어오는 Session에는 Session ID, WebSocketID가 들어와있음 
		log.info("{} 연결 활성화", session.getId());
		sessionMap.putIfAbsent(session.getId(), session);
		log.info("{} 웹 소켓 session 생성 완료", session);
	}

	/** 새로운 WebSocket 메시지가 도착했을 때 호출, 
	 *  수식 된 메세지 처리 및 응답 생성, 
	 *  ex) 특정 메시지 유형 파싱 -> 그에 대한 응답 생성
	 *  @param WebSocketSession ㅡ WebSocketMessage
	 * */
	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		// TODO Auto-generated method stub
		String payload = message.getPayload().toString();
		// payload = message
		log.info("received message, session id={}, message={}", session.getId().toString(), payload);
		//broadcasting message to all session
		log.info("testLog");
		sessionMap.forEach((sessionId, session1) -> {
			try {
				log.info("SessiongID ==>{} ,,  ssesison num  ==> {}", sessionId, session1);
				log.info("message  ==>{}", message);
				log.info("message class ==>{}", message.getClass());
				log.info("message payload    => {}", message.getPayload());
				log.info("message payloadLen=>{}", message.getPayloadLength());
				session1.sendMessage(message);
			} catch (Exception e) {
				log.error("fail to send message to session id={}, error={}",
					sessionId, e.getMessage());
			}
		});
		
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
		 log.info("{} 웹소켓 해제", session);
	     sessionMap.remove(session.getId());
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
