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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/chart-data")
public class ChartServlet extends HttpServlet {

  private static final String DATA_FILE = "/WEB-INF/edible_food_2011.csv";
  private static final int COUNTRY_INDEX = 1;
  private static final int RICE_INDEX = 4;

  // Stores data in the form "country_name: rice_produced."
  private LinkedHashMap<String, Double> countryMap = new LinkedHashMap<>();

  @Override
  public void init() {
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(DATA_FILE));

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      if (cells.length < RICE_INDEX + 1) {
        continue;
      } 

      String country = String.valueOf(cells[COUNTRY_INDEX]);
      String riceStr = String.valueOf(cells[RICE_INDEX]);

      if (country.length() == 0 || riceStr.length() == 0) {
        continue;
      }

      Double riceConsump;
      if (riceStr.equals("*") || riceStr.equals("")) {
        riceConsump = 0.0;
      } else {
        riceConsump = Double.valueOf(riceStr);
      }

      countryMap.put(country, riceConsump);
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Gson gson = new Gson();
    String json = gson.toJson(countryMap);
    response.getWriter().println(json);
  }
}