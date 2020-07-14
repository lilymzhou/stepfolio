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
  private static final String JSON_TYPE = "application/json";
  private static final String DATA_FILE = "/WEB-INF/edible_food_2011.csv";
  private static final String COUNTRY_FILE = "/WEB-INF/country_name_map.csv";
  private static final int CSV_INDEX = 0;
  private static final int GEO_INDEX = 1;
  private static final String UNDEFINED_DATA = "*";
  private static final String FILE_DELIM = ",";
  private static final int COUNTRY_INDEX = 1;
  private static final int RICE_INDEX = 4;

  // Stores data in the form "geochart_country_name: rice_supplies."
  private LinkedHashMap<String, Double> countryMap = new LinkedHashMap<>();

  // Stores data in the form "csv_country_name : geochart_country_name."
  private LinkedHashMap<String, String> countryNameMap = new LinkedHashMap<>();

  @Override
  public void init() {
    fillCountryNameMap();

    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(DATA_FILE));

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(FILE_DELIM);
      if (cells.length < RICE_INDEX + 1) {
        continue;
      }

      String country = String.valueOf(cells[COUNTRY_INDEX]);
      String riceStr = String.valueOf(cells[RICE_INDEX]);
      if (country.isEmpty() || riceStr.isEmpty()) {
        continue;
      }

      Double riceConsump = riceStr.equals(UNDEFINED_DATA) ? 0.0 : Double.valueOf(riceStr);

      String countryName =
          countryNameMap.containsKey(country) ? countryNameMap.get(country) : country;
      countryMap.put(countryName, riceConsump);
    }
    scanner.close();
  }

  private void fillCountryNameMap() {
    Scanner countryScanner = new Scanner(getServletContext().getResourceAsStream(COUNTRY_FILE));

    while (countryScanner.hasNextLine()) {
      String line = countryScanner.nextLine();
      String[] cells = line.split(FILE_DELIM);
      countryNameMap.put(cells[CSV_INDEX], cells[GEO_INDEX]);
    }
    countryScanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType(JSON_TYPE);
    Gson gson = new Gson();
    String json = gson.toJson(countryMap);
    response.getWriter().println(json);
  }
}