package com.marcobehler.microservices;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Thanks for watching this episode! Send any feedback to info@marcobehler.com!
 */
public class AuditServer {

    public static void main(String[] args) throws Exception {
        new AuditServer().start();
    }

    public void start() throws Exception {
        Server server = new Server(8999);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                response.setContentType("application/json; charset=utf-8"); // we always indicate json content
                response.setStatus(HttpServletResponse.SC_OK); // always return http 200
                response.getWriter().println("{\"status\" : \"OK\"}");
                baseRequest.setHandled(true); // we are done!
            }
        });
        server.start();
        server.join();
    }
}
