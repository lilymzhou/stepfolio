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

var curSlide = 0; //by default, display first slide
const totalSlides = 3; //total number of slides

/*
 * Advance forward by one slide
 */
function forward() {
  if (curSlide < totalSlides - 1) {
    updateSlide(curSlide + 1);
  } else { //curSlide = totalSlides
    updateSlide(0);
  }
}

/*
 * Advance backwards by one slide
 */
function back() {
  if (curSlide == 0) {
    updateSlide(totalSlides - 1);
  } else {
    updateSlide(curSlide - 1);
  }
}

function updateSlide(slide) {
  curSlide = slide;
  var slideArr = document.getElementsByClassName("slide");
  var defaultSlide = document.getElementsByClassName("default-slide");
  defaultSlide[0].style.display = "none";
  for (var i = 0; i < slideArr.length; i++) {
    if (i != curSlide) {
      slideArr[i].style.display = "none";
    } else {
      slideArr[i].style.display = "block";
    }
  }
}