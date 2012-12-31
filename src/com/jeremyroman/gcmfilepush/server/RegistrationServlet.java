package com.jeremyroman.gcmfilepush.server;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Accepts and stores GCM registrations.
 * This is a pretty thin shim atop the high-reliability datastore.
 *
 * The device ID is assumed to be unique to the client, and randomly generated.
 * It should consist of one or more alphanumeric characters.
 *
 * Requests should be of the form:
 * PUT /registration/DEVICE_ID (request body is the registration ID)
 * DELETE /registration/DEVICE_ID
 */
public class RegistrationServlet extends HttpServlet {
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // Read parameters and check that everything is in order.
    Key key = ServerUtils.getRegistrationKey(request);
    String registrationId = ServerUtils.getRequestBody(request, 4096);
    if (key == null || registrationId == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    // Create or update the entity.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity registration = new Entity(key);
    registration.setProperty("registration_id", registrationId);
    datastore.put(registration);

    // Respond.
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // Create the datastore key.
    Key key = ServerUtils.getRegistrationKey(request);
    if (key == null) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    // Delete the entity.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.delete(key);

    // Respond.
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
