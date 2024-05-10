/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.Lifecycle;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.GlassFishRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.StandardWebSocketUpgradeStrategy;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.UndertowRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.WebLogicRequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.WebSphereRequestUpgradeStrategy;

/**
 * A base class for {@link HandshakeHandler} implementations, independent of the Servlet API.
 *
 * <p>Performs initial validation of the WebSocket handshake request - possibly rejecting it
 * through the appropriate HTTP status code - while also allowing its subclasses to override
 * various parts of the negotiation process (e.g. origin validation, sub-protocol negotiation,
 * extensions negotiation, etc).
 *
 * <p>If the negotiation succeeds, the actual upgrade is delegated to a server-specific
 * {@link org.springframework.web.socket.server.RequestUpgradeStrategy}, which will update
 * the response as necessary and initialize the WebSocket.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @since 4.2
 * @see org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy
 * @see org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy
 * @see org.springframework.web.socket.server.standard.UndertowRequestUpgradeStrategy
 * @see org.springframework.web.socket.server.standard.GlassFishRequestUpgradeStrategy
 * @deprecated
 */
public abstract class AbstractHandshakeHandler implements HandshakeHandler, Lifecycle {

	/**
	 * 각각의 ws 서버에 대한 boolean값 
	 * */
	private static final boolean tomcatWsPresent;

	private static final boolean jettyWsPresent;

	private static final boolean undertowWsPresent;

	private static final boolean glassfishWsPresent;

	private static final boolean weblogicWsPresent;

	private static final boolean websphereWsPresent;

	/** @note : 정적 초기화 블록
	 *	클래스가 로드 될 때 한번만 실행되며, 주로 클래스 변수의 초기화나, 다른 정적인 작업 수행하는데 사용
	 *  
	 *  @role : ClassLoader에 위 멤벼 변수 클래스들이 존재하는지 여부를 확인하게 
	 *         그것을 기반으로 웹 소켓 서버가 현재 사용 가능한 환경인지 판단. 
	 *         => 현재 사용되는 서블릿 컨테이너에서 웹 소켓을 지원하는지 여부를 판단하여 올바르게 동작
	 * */
	static {
		ClassLoader classLoader = AbstractHandshakeHandler.class.getClassLoader();
		tomcatWsPresent = ClassUtils.isPresent(
				"org.apache.tomcat.websocket.server.WsHttpUpgradeHandler", classLoader);
		jettyWsPresent = ClassUtils.isPresent(
				"org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServerContainer", classLoader);
		undertowWsPresent = ClassUtils.isPresent(
				"io.undertow.websockets.jsr.ServerWebSocketContainer", classLoader);
		glassfishWsPresent = ClassUtils.isPresent(
				"org.glassfish.tyrus.servlet.TyrusHttpUpgradeHandler", classLoader);
		weblogicWsPresent = ClassUtils.isPresent(
				"weblogic.websocket.tyrus.TyrusServletWriter", classLoader);
		websphereWsPresent = ClassUtils.isPresent(
				"com.ibm.websphere.wsoc.WsWsocServerContainer", classLoader);
		/** @note : 멤버 변수 true, false 초기화
		 * */
	}


	protected final Log logger = LogFactory.getLog(getClass());

	
	/**WebSocket 연결을 업그레이드에 사용되는 객체*/
	private final RequestUpgradeStrategy requestUpgradeStrategy;

	/**WebSocket 지원 목록 객체*/
	private final List<String> supportedProtocols = new ArrayList<>();

	private volatile boolean running;

	/**
	 * Default constructor that auto-detects and instantiates a
	 * {@link RequestUpgradeStrategy} suitable for the runtime container.
	 * @throws IllegalStateException if no {@link RequestUpgradeStrategy} can be found.
	 */
	protected AbstractHandshakeHandler() {
		this(initRequestUpgradeStrategy());
	}

	/**
	 * A constructor that accepts a runtime-specific {@link RequestUpgradeStrategy}.
	 * @param requestUpgradeStrategy the upgrade strategy to use
	 */
	protected AbstractHandshakeHandler(RequestUpgradeStrategy requestUpgradeStrategy) {
		Assert.notNull(requestUpgradeStrategy, "RequestUpgradeStrategy must not be null");
		this.requestUpgradeStrategy = requestUpgradeStrategy;
	}


	/**
	 * Return the {@link RequestUpgradeStrategy} for WebSocket requests.
	 */
	public RequestUpgradeStrategy getRequestUpgradeStrategy() {
		return this.requestUpgradeStrategy;
	}

	/**
	 * Use this property to configure the list of supported sub-protocols.
	 * The first configured sub-protocol that matches a client-requested sub-protocol
	 * is accepted. If there are no matches the response will not contain a
	 * {@literal Sec-WebSocket-Protocol} header.
	 * <p>Note that if the WebSocketHandler passed in at runtime is an instance of
	 * {@link SubProtocolCapable} then there is no need to explicitly configure
	 * this property. That is certainly the case with the built-in STOMP over
	 * WebSocket support. Therefore, this property should be configured explicitly
	 * only if the WebSocketHandler does not implement {@code SubProtocolCapable}.
	 */
	public void setSupportedProtocols(String... protocols) {
		this.supportedProtocols.clear();
		for (String protocol : protocols) {
			this.supportedProtocols.add(protocol.toLowerCase());
		}
	}

	/**
	 * Return the list of supported sub-protocols.
	 */
	public String[] getSupportedProtocols() {
		return StringUtils.toStringArray(this.supportedProtocols);
	}


	@Override
	public void start() {
		if (!isRunning()) {
			this.running = true;
			doStart();
		}
	}

	protected void doStart() {
		if (this.requestUpgradeStrategy instanceof Lifecycle lifecycle) {
			lifecycle.start();
		}
	}

	@Override
	public void stop() {
		if (isRunning()) {
			this.running = false;
			doStop();
		}
	}

	protected void doStop() {
		if (this.requestUpgradeStrategy instanceof Lifecycle lifecycle) {
			lifecycle.stop();
		}
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}


	
	/** @auth : Shinsungin 
	 *  @note: 메인 기능, doHandshake를 통해 Header  Upgrade 
	 * */
	@Override
	public final boolean doHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) throws HandshakeFailureException {

		WebSocketHttpHeaders headers = new WebSocketHttpHeaders(request.getHeaders());
		/**@note: webSocketHTTPHears를 불러온다. 여기에 어떤 정보가 들어있는지 확인해 보기 위해서 Junit을 사용해서 테스트를 진행해보고자 하였으나 
		 *  ServerHTTPRequest를 어떻게 초기화 해야 할지 몰라서 실패 했다 
		 * 
		 * @note2: webSDocketHTTPHeaders가 가지고 있는 정보 
		 * 		@member1 Sec_Websocket에 관련된 정보를 가지고 있다. 
		 * 				[Accept, Extensions, Key, Protocol, Version] + 직렬화 된 시리얼 버전 UID
		 * `	@member2 final HttpHeader가 있다. 
		 * 
		 * @note3 : 생성자 
		 *       => 즉, webSocketHTTPHeaders에 현재 주소의 HttpHeader을 정의한다. 
		 * */
		if (logger.isTraceEnabled()) {
			logger.trace("Processing request " + request.getURI() + " with headers=" + headers);
		}
		try {
			/** @StartHandShake
			 *  @note1: http 요청이 Get 방식인지 확인한다. 
			 * */
			if (HttpMethod.GET != request.getMethod()) {
				/** @note1 : @branch1.Start
				 *  http 요청이 Get이 아닌 경우 실행 
				 *  1.response 객체에 허용되지 않는 접근 방식임을 저장한다. [status = 405]
				 *  2.response 객체의 헤더 정보에, 'Get' 방식을 접근이 가능함을 저장한다.		
				 * */
				response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
				response.getHeaders().setAllow(Collections.singleton(HttpMethod.GET));
				if (logger.isErrorEnabled()) {
					/**@note1.1 logger의 레벨이 error 단계일경우 수행 */
					logger.error("Handshake failed due to unexpected HTTP method: " + request.getMethod());
				}
				/**@branch1.Finish*/
				return false;
			}
			
			if (!"WebSocket".equalsIgnoreCase(headers.getUpgrade())) {
				
				/**@note2: @branch2.Start
				 * 메소드 시작 부분에서 CurrentRequest의 header를 WebSocketHTTPHerader에 담았다. 
				 * 그 해당 Header의 UPgrade의 필드값이 WebSoket이 아닐 경우에 걸린다. 
				 * @How? :그래서 WebSocketHeader를 살펴보니, Upgrade에 대한 정보가 없다. 
				 * 		그러면 getUpgrade는 뭘까? 
				 * 		=>HttpHeaders 클래스에 있는 메소드이다. 
				 * 		기본적으로 전역 상수로 "Upgrade 값을 가지고 있다."
				 * @How2? : 그렇다면 이 조건을 통과할려면, client가 웹 소켓을 요청을 보낼 떄, 필드 값으로 "WebSocket"이 HttpHeader클래스의 SetUpgrade까지 도달해야 한다. 
				 * 
				 * @result : 이 조건에 걸리면, 하단에 있는 Method를 통해, Response에 BadRequest를 보낸다 . 
				 * */
				handleInvalidUpgradeHeader(request, response);
				/**@branch2.Finish*/
				return false;
			}
			
			if (!headers.getConnection().contains("Upgrade") && !headers.getConnection().contains("upgrade")) {
				/** @note3 : @branch3.Start
				 *  HTTP connection 멤버의 필드값이 Upgrade 혹은 upgrade 가 맞는지 검사한다. 
				 *  근데 이건 && and 연산아닌가,,,? 아 아닌 것을 걸러내는 거니까, Upgrade 와 upgrade 둘다 아니어야 한다. 그리나 and가 맞네,,ㅎ 
				 *  
				 *  @result : @branch2와 동일하게 response 객체에 badRequest 전달. 
				 * */
				handleInvalidConnectHeader(request, response);
				/**@branch3.Finish*/
				return false;
			}
			
			if (!isWebSocketVersionSupported(headers)) {
				/** @note4 : @branch4.Start
				 *   위에서 WebSocketHeader는 Version 정보를 담고 있다고 하였다. 그정보의 값이 True 일 때 Exception 이 걸린다. 
				 *  @How? :
				 *   WesocketHttpHeaders에서 version 정보를 가져온다. 
				 *  RequestUpgradeStrategy 클래스에는 지원하는 버전의 정보가 담긴 리스트를 멤버변수로 담고 있다. 
				 *  이를 통해 현재 버전 WebSocket 버전 정보가 지원하는 버전인지 확인한다. 
				 *  일치 시 true, 아닐시 false
				 *   
				 *  @result = 없을 시 response 객체에  UPGRADE_REQUIRED 즉 버전 업그레이드 요구를 전송한다. 
				 * */
				handleWebSocketVersionNotSupported(request, response);
				return false;
				/**@branch4.Finish*/
			}
			
			if (!isValidOrigin(request)) {
				/** @note5 : @branch5.Start
				 *  클라이언트 Header 웹 소켓의 EndPoint에 접근가능한지 확인한다. 
				 *  Default True. 
				 *  모든 사용자에 대해서 허용한다. 
				 *  
				 *  @CustomExample
						protected boolean isValidOrigin(ServerHttpRequest request) {
						 	String origin = request.getHeaders().getOrigin();
						    return origin != null && origin.equals("https://example.com");
			`	 * 위와 같은 형식으로 특정 도메인 값만 허용할 수 있다. 
				 * 
				 * @result : 다른 경우 접근 권한 없음을 선언한다. 
				 * */
				response.setStatusCode(HttpStatus.FORBIDDEN);
				/**@branch5.Finish*/
				return false;
			}
			
			/**@note6 : headers 객체에서 WebSocketKey값을 불러온다.*/
			String wsKey = headers.getSecWebSocketKey();
			if (wsKey == null) {
				/**@note7 : @branch6.Start 
				 * 가져온키가 null 인지 확인한다. 여기서에서 isBlank() 까지 사용해도 더 좋을 것 같긴하다..!
				 * null 이라면 Bad_Request 발생
				 * */
				if (logger.isErrorEnabled()) {
					logger.error("Missing \"Sec-WebSocket-Key\" header");
				}
				response.setStatusCode(HttpStatus.BAD_REQUEST);
				/**@branch6.Finish*/
				return false;
			}
		}
		catch (IOException ex) {
			throw new HandshakeFailureException(
					"Response update failed during upgrade to WebSocket: " + request.getURI(), ex);
		}
		/** 여기까지 validation 검증 과정이었던거 같다,,,*/
		
		/**@note8 : @handShake.Start
		 * @How1 : webSocketProtocla에 대한 subProtocol설정 
		 *  	@param1, WebSocketHttpHeader 에 저장 된 정보
		 *  	@param2, WebSocketHandler Interface
		 * 		@Process1 - WebSocketHandler가 지원하는 protocol list 호출 
		 * `	@Process2 - Client 가 요청한 protocal 이 protocol list 에 있는지 확인
		 * 		@Process3 - 있다면, 해당 protocol 반환, 없다면  server 전체에서 지원하는 protocol list 에 해당 protocol 이 있는지 확인
		 * 		@Process4 - 그래도 없다면 null 반환 
		 * @How2 : webSocket 확장 목록 호출 
		 * 		- webSocket 연결에 추가기능 적용 할 때 사용 => header 로 전송
		 * 
		 * @How3: 서버가 지원한는 WebSocket 확장 목록 호출 
		 * 
		 * @How4: 최종적으로 결정할 확장 목록에 대한 필터링 
		 * 			-> 사용자가 요구하지 않은 확장 기능은 제거 
		 * 
		 * @How5: 웹 소켓 연결에 대한 사용자 정보 결정 
		 *  	- 현재 이용중인 사용자 식별, 
		 * */
		String subProtocol = selectProtocol(headers.getSecWebSocketProtocol(), wsHandler);
		List<WebSocketExtension> requested = headers.getSecWebSocketExtensions();
		List<WebSocketExtension> supported = this.requestUpgradeStrategy.getSupportedExtensions(request);
		List<WebSocketExtension> extensions = filterRequestedExtensions(request, requested, supported);
		Principal user = determineUser(request, wsHandler, attributes);

		if (logger.isTraceEnabled()) {
			logger.trace("Upgrading to WebSocket, subProtocol=" + subProtocol + ", extensions=" + extensions);
		}
		/**@note9 : @HeaderUpgrade
		 * @Process1 : 위에서 정의된 각각의 정보들을 upgrade에 담는다. 
		 * 		@param1 request: webSocket handShake요청에 대한 객체, 
		 * 		@param2 response : 응답 객체, 서버에서 client 로 응답 정보 설정 
		 * 	`	@param3 subProtocal : 위에서 선택 된 protocol
		 * 		@param4 extensions : 선택 된 확장 목록
		 * 		@param5 user : 사용자 정보 
		 * 		@param6 wsHandler: WebSocketHandler 객체입니다. 서버-client 간 통신 처리 
		 * 		@param7 attribute : 세션에 연결 된 사용자 정의 속성, 
		 * @why? : 근데 이 upgrade는 어디서 처리가 되는거지,, 상위 클래스인 RequestUpgradeStrategy에는 Interface라 로직이 없다.. hm,,
		 * */
		this.requestUpgradeStrategy.upgrade(request, response, subProtocol, extensions, user, wsHandler, attributes);
		return true;
	}

	
	
	
	
	protected void handleInvalidUpgradeHeader(ServerHttpRequest request, ServerHttpResponse response) throws IOException {
		if (logger.isErrorEnabled()) {
			logger.error(LogFormatUtils.formatValue(
					"Handshake failed due to invalid Upgrade header: " + request.getHeaders().getUpgrade(), -1, true));
		}
		response.setStatusCode(HttpStatus.BAD_REQUEST);
		response.getBody().write("Can \"Upgrade\" only to \"WebSocket\".".getBytes(StandardCharsets.UTF_8));
	}

	protected void handleInvalidConnectHeader(ServerHttpRequest request, ServerHttpResponse response) throws IOException {
		if (logger.isErrorEnabled()) {
			logger.error(LogFormatUtils.formatValue(
					"Handshake failed due to invalid Connection header" + request.getHeaders().getConnection(), -1, true));
		}
		response.setStatusCode(HttpStatus.BAD_REQUEST);
		response.getBody().write("\"Connection\" must be \"upgrade\".".getBytes(StandardCharsets.UTF_8));
	}

	
	
	
	protected boolean isWebSocketVersionSupported(WebSocketHttpHeaders httpHeaders) {
		String version = httpHeaders.getSecWebSocketVersion();
		String[] supportedVersions = getSupportedVersions();
		for (String supportedVersion : supportedVersions) {
			if (supportedVersion.trim().equals(version)) {
				return true;
			}
		}
		return false;
	}

	protected String[] getSupportedVersions() {
		return this.requestUpgradeStrategy.getSupportedVersions();
	}

	protected void handleWebSocketVersionNotSupported(ServerHttpRequest request, ServerHttpResponse response) {
		if (logger.isErrorEnabled()) {
			String version = request.getHeaders().getFirst("Sec-WebSocket-Version");
			logger.error(LogFormatUtils.formatValue(
					"Handshake failed due to unsupported WebSocket version: " + version +
							". Supported versions: " + Arrays.toString(getSupportedVersions()), -1, true));
		}
		response.setStatusCode(HttpStatus.UPGRADE_REQUIRED);
		response.getHeaders().set(WebSocketHttpHeaders.SEC_WEBSOCKET_VERSION,
				StringUtils.arrayToCommaDelimitedString(getSupportedVersions()));
	}

	/**
	 * Return whether the request {@code Origin} header value is valid or not.
	 * By default, all origins as considered as valid. Consider using an
	 * {@link OriginHandshakeInterceptor} for filtering origins if needed.
	 */
	protected boolean isValidOrigin(ServerHttpRequest request) {
		return true;
	}

	/**
	 * Perform the sub-protocol negotiation based on requested and supported sub-protocols.
	 * For the list of supported sub-protocols, this method first checks if the target
	 * WebSocketHandler is a {@link SubProtocolCapable} and then also checks if any
	 * sub-protocols have been explicitly configured with
	 * {@link #setSupportedProtocols(String...)}.
	 * @param requestedProtocols the requested sub-protocols
	 * @param webSocketHandler the WebSocketHandler that will be used
	 * @return the selected protocols or {@code null}
	 * @see #determineHandlerSupportedProtocols(WebSocketHandler)
	 */
	@Nullable
	protected String selectProtocol(List<String> requestedProtocols, WebSocketHandler webSocketHandler) {
		List<String> handlerProtocols = determineHandlerSupportedProtocols(webSocketHandler);
		for (String protocol : requestedProtocols) {
			if (handlerProtocols.contains(protocol.toLowerCase())) {
				return protocol;
			}
			if (this.supportedProtocols.contains(protocol.toLowerCase())) {
				return protocol;
			}
		}
		return null;
	}

	/**
	 * Determine the sub-protocols supported by the given WebSocketHandler by
	 * checking whether it is an instance of {@link SubProtocolCapable}.
	 * @param handler the handler to check
	 * @return a list of supported protocols, or an empty list if none available
	 */
	protected final List<String> determineHandlerSupportedProtocols(WebSocketHandler handler) {
		WebSocketHandler handlerToCheck = WebSocketHandlerDecorator.unwrap(handler);
		List<String> subProtocols = null;
		if (handlerToCheck instanceof SubProtocolCapable subProtocolCapable) {
			subProtocols = subProtocolCapable.getSubProtocols();
		}
		return (subProtocols != null ? subProtocols : Collections.emptyList());
	}

	/**
	 * Filter the list of requested WebSocket extensions.
	 * <p>As of 4.1, the default implementation of this method filters the list to
	 * leave only extensions that are both requested and supported.
	 * @param request the current request
	 * @param requestedExtensions the list of extensions requested by the client
	 * @param supportedExtensions the list of extensions supported by the server
	 * @return the selected extensions or an empty list
	 */
	protected List<WebSocketExtension> filterRequestedExtensions(ServerHttpRequest request,
			List<WebSocketExtension> requestedExtensions, List<WebSocketExtension> supportedExtensions) {

		List<WebSocketExtension> result = new ArrayList<>(requestedExtensions.size());
		for (WebSocketExtension extension : requestedExtensions) {
			if (supportedExtensions.contains(extension)) {
				result.add(extension);
			}
		}
		return result;
	}

	/**
	 * A method that can be used to associate a user with the WebSocket session
	 * in the process of being established. The default implementation calls
	 * {@link ServerHttpRequest#getPrincipal()}
	 * <p>Subclasses can provide custom logic for associating a user with a session,
	 * for example for assigning a name to anonymous users (i.e. not fully authenticated).
	 * @param request the handshake request
	 * @param wsHandler the WebSocket handler that will handle messages
	 * @param attributes handshake attributes to pass to the WebSocket session
	 * @return the user for the WebSocket session, or {@code null} if not available
	 */
	@Nullable
	protected Principal determineUser(
			ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {

		return request.getPrincipal();
	}


	private static RequestUpgradeStrategy initRequestUpgradeStrategy() {
		if (tomcatWsPresent) {
			return new TomcatRequestUpgradeStrategy();
		}
		else if (jettyWsPresent) {
			return new JettyRequestUpgradeStrategy();
		}
		else if (undertowWsPresent) {
			return new UndertowRequestUpgradeStrategy();
		}
		else if (glassfishWsPresent) {
			return TyrusStrategyDelegate.forGlassFish();
		}
		else if (weblogicWsPresent) {
			return TyrusStrategyDelegate.forWebLogic();
		}
		else if (websphereWsPresent) {
			return new WebSphereRequestUpgradeStrategy();
		}
		else {
			// Let's assume Jakarta WebSocket API 2.1+
			return new StandardWebSocketUpgradeStrategy();
		}
	}


	/**
	 * Inner class to avoid a reachable dependency on Tyrus API.
	 */
	private static class TyrusStrategyDelegate {

		public static RequestUpgradeStrategy forGlassFish() {
			return new GlassFishRequestUpgradeStrategy();
		}

		public static RequestUpgradeStrategy forWebLogic() {
			return new WebLogicRequestUpgradeStrategy();
		}
	}

}
