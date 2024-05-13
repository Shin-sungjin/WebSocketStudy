package com.example.test;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import jakarta.servlet.ServletContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CustomHandShakeHandler extends DefaultHandshakeHandler {

	private ServletContext servletContext;
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		log.info("suepr class = > {}", super.getClass());
		log.info("sueprgetSupportedVersions = > {}", super.getSupportedVersions());
		this.servletContext = servletContext;
		super.setServletContext(servletContext);
	}
}
