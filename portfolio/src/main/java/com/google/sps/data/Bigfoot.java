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

package com.google.sps.data;

/*
 * Represents one sighting of Bigfoot, with information on the location (coordinates),
 * date, and the witness's description of the sighting.
 */
public class Bigfoot {
  // Coordinates of Bigfoot sighting.
  private double lat;
  private double lng;

  private String title;
  private String description;
  private String date;
  // Description in words of sighting location.
  private String location;

  public Bigfoot(
      double lat, double lng, String title, String description, String location, String date) {
    this.lat = lat;
    this.lng = lng;
    this.title = title;
    this.description = description;
    this.location = location;
    this.date = date;
  }
}