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
import com.google.sps.data.Bigfoot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/bigfoot-data")
public class BigfootServlet extends HttpServlet {
  private static final String JSON_TYPE = "application/json";
  private static final String DATA_FILE = "/WEB-INF/bfro_reports_geocoded.csv";
  private static final String FILE_DELIM = ",";

  private static final int DESCR_INDEX = 0;
  private static final int LOC_INDEX = 1;
  private static final int TITLE_INDEX = 5;
  private static final int LAT_INDEX = 6;
  private static final int LNG_INDEX = 7;
  private static final int DATE_INDEX = 8;

  // Stores latitude, longitude, and title of each Bigfoot sighting in csv file.
  private ArrayList<Bigfoot> bigfootArr = new ArrayList<>();

  @Override
  public void init() {
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream(DATA_FILE));

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(FILE_DELIM);
      if (cells.length < DATE_INDEX + 1) {
        continue;
      }

      String description = String.valueOf(cells[DESCR_INDEX]);
      String location = String.valueOf(cells[LOC_INDEX]);
      String title = String.valueOf(cells[TITLE_INDEX]);
      String latStr = String.valueOf(cells[LAT_INDEX]);
      String lngStr = String.valueOf(cells[LNG_INDEX]);
      String date = String.valueOf(cells[DATE_INDEX]);
      if (description.isEmpty() || location.isEmpty() || title.isEmpty() || latStr.isEmpty()
          || lngStr.isEmpty() || date.isEmpty()) {
        continue;
      }

      Double lat;
      Double lng;
      try {
        lat = Double.valueOf(latStr);
        lng = Double.valueOf(lngStr);
      } catch (NumberFormatException e) {
        continue;
      }

      Bigfoot newSighting = new Bigfoot(lat, lng, title, description, location, date);
      bigfootArr.add(newSighting);
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType(JSON_TYPE);
    Gson gson = new Gson();
    String json = gson.toJson(bigfootArr);
    response.getWriter().println(json);
  }
}