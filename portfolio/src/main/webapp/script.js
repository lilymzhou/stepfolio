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

let curSlide = 0; //By default, display first slide.

/*
 * Advance forward by one slide
 */
function forward() {
  updateSlide(curSlide + 1);
}

/*
 * Advance backwards by one slide
 */
function back() {
  updateSlide(curSlide - 1);
}

/*
 * Display slide, and hide all other slide elements.
 */
function updateSlide(slide) {
  let slideArr = document.getElementsByClassName("slide");
  let totalSlides = slideArr.length;

  //Adjust curSlide  value
  if (slide >= totalSlides) {
    slide = 0;
  } else if (slide < 0) {
    slide = totalSlides - 1;
  }
  curSlide = slide;

  let defaultSlide = document.getElementsByClassName("default-slide");
  defaultSlide[0].style.display = "none";

  for (let i = 0; i < totalSlides;i++) {
    if (i != curSlide) {
      slideArr[i].style.display = "none";
    } else {
      slideArr[i].style.display = "block";
    }
  }
}