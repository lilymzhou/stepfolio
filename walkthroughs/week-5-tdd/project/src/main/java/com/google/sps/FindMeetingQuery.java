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

package com.google.sps;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> ranges = new ArrayList<TimeRange>();
    
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return ranges;
    }

    ranges.add(TimeRange.WHOLE_DAY);
    for (Event event : events) {
      TimeRange eventTime = event.getWhen();
      if (!hasRequestAttendees(event, request)) {
        continue;
      }
      for (TimeRange range : ranges) {
        TimeRange before = TimeRange.fromStartEnd(range.start(), eventTime.start(), false);
        TimeRange after = TimeRange.fromStartEnd(eventTime.end(), range.end() - 1, true);
        if (range.contains(eventTime.start()) && range.contains(eventTime.end())) {
          if (before.duration() >= request.getDuration()) {
            ranges.add(before);
          }
          if (after.duration() >= request.getDuration()) {
            ranges.add(after);
          }
          ranges.remove(range);
          break;
        } else if (range.contains(eventTime.end())) {
          if (after.duration() >= request.getDuration()) {
            ranges.add(after);
          }
          ranges.remove(range);
          break;
        } else if (range.contains(eventTime.start())) {
          if (before.duration() >= request.getDuration()) {
            ranges.add(before);
          }
          ranges.remove(range);
          break;
        }
      }
    }
    return ranges;
  }

  /*
   * Return whether attendees for event overlap with attendees for request
   * (i.e. at least one attendee for request is an attendee for event).
   */
  private boolean hasRequestAttendees(Event event, MeetingRequest request) {
    for (String reqAttendee : request.getAttendees()) {
      if (event.getAttendees().contains(reqAttendee)) {
          return true;
      }
    }
    return false;
  }
}
