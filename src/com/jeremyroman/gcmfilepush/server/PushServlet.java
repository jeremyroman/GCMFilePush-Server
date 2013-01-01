package com.jeremyroman.gcmfilepush.server;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

/**
 * Issues GCM messages to push a URL.
 *
 * The device ID is assumed to be unique to the client, and randomly generated.
 * It should consist of one or more alphanumeric characters.
 *
 * Requests should be of the form:
 * POST /push/DEVICE_ID (request body is form-encoded with url and filename)
 */
public class PushServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // Read parameters and check that everything is in order.
    Key key = ServerUtils.getRegistrationKey(request);
    if (key == null || request.getParameter("url") == null ||
        request.getParameter("filename") == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    // Fetch the registration.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String registrationId = null;
    try {
      Entity registration = datastore.get(key);
      registrationId = (String) registration.getProperty("registration_id");
    } catch (EntityNotFoundException e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    // Ensure the registration ID is present.
    if (registrationId == null || registrationId.isEmpty()) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    // Dispatch to GCM.
    String senderKey = System.getProperty("com.jeremyroman.gcmfilepush.server.SENDER_KEY");
    if (senderKey == null || senderKey.isEmpty()) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    Sender sender = new Sender(senderKey);
    Message.Builder message = new Message.Builder();
    for (String parameter : request.getParameterNames()) {
      message.addData(parameter, request.getParameter(parameter));
    }
    Result result = sender.send(message.build(), registrationId, 5 /* retries */);

    // Check for an error in the GCM response.
    if (result.getMessageId() == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    // If necessary, update canonical registration ID.
    if (result.getCanonicalRegistrationId() != null) {
      Entity registration = new Entity(key);
      registration.setProperty("registration_id", result.getCanonicalRegistrationId());
      datastore.put(registration);
    }

    // Respond.
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
