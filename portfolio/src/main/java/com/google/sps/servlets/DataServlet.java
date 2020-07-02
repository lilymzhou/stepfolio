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

  private static final String COMMENT_PARAMETER = "comment-input";
  private static final String NAME_PARAMETER = "name-input";
  private static final String MAX_PARAMETER = "max-input";
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final String COMMENT_ENTITY = "Comment";
  private static final String COMMENT_CONTENT = "content";
  private static final String COMMENT_NAME = "name";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String name = request.getParameter(NAME_PARAMETER);
    String comment = request.getParameter(COMMENT_PARAMETER);

    response.setContentType("text/html;");
    response.getWriter().println(name + ": " + comment);

    // Store comment in Datastore.
    Entity commEntity = new Entity(COMMENT_ENTITY);
    commEntity.setProperty(COMMENT_CONTENT, comment);
    commEntity.setProperty(COMMENT_NAME, name);
    datastore.put(commEntity);

    // Redirect back to main page.
    response.sendRedirect("/index.html");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query(COMMENT_ENTITY);
    PreparedQuery results = datastore.prepare(query);

    // Process user-selected maximum number of comments.
    String numCommentsStr = request.getParameter(MAX_PARAMETER);
    int numComments;
    try {
      numComments = Integer.parseInt(numCommentsStr);
    } catch (Exception e) {
      numComments = -1;
    }

    FetchOptions fetchOptions = numComments > 0 ? FetchOptions.Builder.withLimit(numComments) : FetchOptions.Builder.withDefaults();
    List<Entity> messages = results.asList(fetchOptions);

    String json = new Gson().toJson(messages);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}