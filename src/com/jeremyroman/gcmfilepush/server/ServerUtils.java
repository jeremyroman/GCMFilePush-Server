package com.jeremyroman.gcmfilepush.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * Utilities shared by multiple GCMFilePush servlets.
 */
public class ServerUtils {
  private static final Pattern URL_PATTERN = Pattern.compile("/([A-Za-z0-9]+)");

  /** Returns the request body as a string, using some fixed maximum buffer size. */
  public static String getRequestBody(HttpServletRequest request, int bufferSize)
      throws IOException {
    BufferedReader reader = request.getReader();
    char[] buffer = new char[bufferSize];
    int read = reader.read(buffer, 0, buffer.length);
    if (read <= 0 || reader.read() != -1) {
      return null;
    } else {
      return new String(buffer, 0, read);
    }
  }

  /** Returns a data store key using the path info from the request. */
  public static Key getRegistrationKey(HttpServletRequest request) {
    String deviceId = getDeviceId(request);
    if (deviceId != null) {
      return KeyFactory.createKey("Registration", deviceId);
    } else {
      return null;
    }
  }

  private static String getDeviceId(HttpServletRequest request) {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null) {
      return null;
    }

    Matcher matcher = URL_PATTERN.matcher(pathInfo);
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      return null;
    }
  }
}
