package com.jeremyroman.gcmfilepush.server;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

/**
 * Provides data allowing clients to configure themselves.
 * This saves the client from knowing anything besides the server URL.
 */
public class ClientConfigServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    JSONObject config = new JSONObject();
    config.put("sender_id", System.getProperty("com.jeremyroman.gcmfilepush.server.SENDER_ID"));
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().print(config);
  }
}
