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

/*
 * Fetches message from /data and displays it on the DOM.
 */
function getComments() {
  fetch('/data').then(response => response.json()).then((mssg) => {
    const mssgElem = document.getElementById('comments-container');
    mssg.forEach((line) => {
      mssgElem.appendChild(createLine(line.propertyMap.name + ": " + line.propertyMap.content));
    });
  });
}

/*
 * Removes all comments from Datastore.
 */
function removeComments() {
  fetch('/delete-data', {method: 'POST'});
}

/*
 * Creates a new paragraph element from text.
 */
function createLine(text) {
  const newLine = document.createElement('p');
  newLine.innerText = text;
  return newLine;
}