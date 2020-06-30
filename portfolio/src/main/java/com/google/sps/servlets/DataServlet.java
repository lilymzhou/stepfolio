// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.FetchOptions;
import java.io.IOException;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final String COMMENT = "comment-input";
  private static final String NAME = "name-input";
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final int MAX_RESULTS = 10;
  private static final String COMM_TYPE = "Comment";
  private static final String COMM_CONTENT = "content";
  private static final String COMM_NAME = "name";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String name = request.getParameter(NAME);
    String comment = request.getParameter(COMMENT);

    response.setContentType("text/html;");
    response.getWriter().println(name + ": " + comment);

    // Store comment in Datastore.
    Entity commEntity = new Entity(COMM_TYPE);
    commEntity.setProperty(COMM_CONTENT, comment);
    commEntity.setProperty(COMM_NAME, name);
    datastore.put(commEntity);

    // Redirect back to main page.
    response.sendRedirect("/index.html");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(COMM_TYPE);
    PreparedQuery results = datastore.prepare(query);
    List<Entity> messages = results.asList(FetchOptions.Builder.withLimit(MAX_RESULTS));

    String json = new Gson().toJson(messages);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}