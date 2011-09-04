package com.andyspohn.android.groundhog.handler;

import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.server.Request;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultHandler extends org.eclipse.jetty.server.handler.DefaultHandler {

    public DefaultHandler() {
        super();
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (response.isCommitted() || baseRequest.isHandled())
            return;

        baseRequest.setHandled(true);

        String method = request.getMethod();

        if (!method.equals(HttpMethods.GET) || !request.getRequestURI().equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.sendRedirect("/s");
    }
}