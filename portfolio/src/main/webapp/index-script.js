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

const COMMENTS_ID = 'comments-container';
const MAX_ID = 'max-input';
const DATA_URL = '/data?max-input=';
const DELETE_URL = '/delete-data';

/*
 * Fetches message from /data and displays it on the DOM.
 */
function getComments() {
  let num = document.getElementById(MAX_ID).value;
  fetch(DATA_URL + num).then(response => response.json()).then((mssg) => {
    const mssgElem = document.getElementById(COMMENTS_ID);
    mssg.forEach((line) => {
      mssgElem.appendChild(createLine(
        line.propertyMap.name + ": " + line.propertyMap.content 
        + " (score: " + line.propertyMap.sentiment + ")"
      ));
    });
  });
}

/*
 * Creates a new paragraph element from text.
 */
function createLine(text) {
  const newLine = document.createElement('p');
  newLine.innerText = text;
  return newLine;
}

/*
 * Remove comments from /data.
 */
function removeComments() {
  const response = fetch(DELETE_URL, {method: 'POST'});
  response.then(refresh);
}

/*
 * Refresh comments displayed.
 */
function refresh() {
  document.getElementById(COMMENTS_ID).innerHTML = '';
  getComments();
}