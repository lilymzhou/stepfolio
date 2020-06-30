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

  private List<String> messages;
  private static final String COMMENT = "comment-input";
  private static final String NAME = "name-input";
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void init() {
    messages = new ArrayList<>();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Construct comment from user request.
    String fullComment = request.getParameter(NAME) + ": " + request.getParameter(COMMENT);
    messages.add(fullComment);

    response.setContentType("text/html;");
    response.getWriter().println(fullComment);

    Entity commEntity = new Entity("Comment");
    commEntity.setProperty("content", fullComment);
    datastore.put(commEntity);

    // Redirect back to main page.
    response.sendRedirect("/index.html");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment");
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
      String curContent = (String) entity.getProperty("content");
      messages.add(curContent);
    }
    String json = convertToJson(messages);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  private String convertToJson(List<String> messageList) {
    String json = "{\"history\": [";
    for (int i = 0; i < messageList.size(); i++) {
      json += "\"" + messages.get(i) + "\"";
      if (i != messageList.size() - 1) {
        json += ", ";
      }
    }
    json += "]}";
    return json;
  }
}