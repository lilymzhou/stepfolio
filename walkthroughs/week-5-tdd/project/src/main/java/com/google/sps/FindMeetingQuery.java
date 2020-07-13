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
import java.util.Collection;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> slots = new ArrayList<TimeRange>();
    
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return slots;
    }

    slots.add(TimeRange.WHOLE_DAY);

    if (!request.getOptionalAttendees().isEmpty()) {
      ArrayList<TimeRange> optionalSlots = findTimeSlots(events, request, slots, true);
      if (optionalSlots.isEmpty() && !request.getAttendees().isEmpty()) {
        return findTimeSlots(events, request, slots, false);
      }
      return optionalSlots;
    } else {
      return findTimeSlots(events, request, slots, false);
    }
  }

  /*
   * Returns a list with all the available time slots when the requested meeting
   * can take place.
   */
  private ArrayList<TimeRange> findTimeSlots(Collection<Event> events, MeetingRequest request,
    ArrayList<TimeRange> slots, boolean hasOptionalAttendees) {

    // A copy of slots that stores new changes to slots without interferring
    // with the for loop below.
    ArrayList<TimeRange> curSlots = new ArrayList<>(slots);

    for (Event event : events) {
      TimeRange eventTime = event.getWhen();
      slots = curSlots;

      // Determine whether event's attendees overlaps with request's attendees.
      if (hasOptionalAttendees && !hasAllAttendees(event, request)) {
        continue;
      }
      if (!hasOptionalAttendees && !hasRequiredAttendees(event, request)) {
        continue;
      }

      for (TimeRange slot : slots) {
        TimeRange before = TimeRange.fromStartEnd(slot.start(), eventTime.start(), false);
        TimeRange after = TimeRange.fromStartEnd(eventTime.end(), slot.end() - 1, true);

        if (slot.contains(eventTime)) {
          removeFromMiddle(curSlots, request.getDuration(), before, after, slot);
          break;
        } else if (slot.contains(eventTime.end())) {
          removeFromEnd(curSlots, request.getDuration(), after, slot);
          break;
        } else if (slot.contains(eventTime.start())) {
          removeFromStart(curSlots, request.getDuration(), before, slot);
          break;
        }
      }
    }
    return curSlots;
  }

  /*
   * Replaces slot with two new slots, with a gap in the middle corresponding
   * to the space that the event takes (and thus the meeting cannot take place in).
   */
  private void removeFromMiddle(ArrayList<TimeRange> curSlots, long reqDuration,
    TimeRange before, TimeRange after, TimeRange slot) {
    if (before.duration() >= reqDuration) {
      curSlots.add(before);
    }
    if (after.duration() >= reqDuration) {
      curSlots.add(after);
    }
    curSlots.remove(slot);
  }

  /*
   * Removes everything in the slot before TimeRange after, corresponding to the space
   * that the event takes (and thus the meeting cnanot take place in).
   */
  private void removeFromEnd(ArrayList<TimeRange> curSlots, long reqDuration,
    TimeRange after, TimeRange slot) {
    if (after.duration() >= reqDuration) {
      curSlots.add(after);
    }
    curSlots.remove(slot);
  }

  /*
   * Removes everything in the slot after TimeRange before, corresponding to the space
   * that the event takes (and thus the meeting cnanot take place in).
   */
  private void removeFromStart(ArrayList<TimeRange> curSlots, long reqDuration,
    TimeRange before, TimeRange slot) {
    if (before.duration() >= reqDuration) {
      curSlots.add(before);
    }
    curSlots.remove(slot);
  }

  /*
   * Return whether attendees for event overlap with required attendees for request
   * (i.e. at least one attendee for request is an attendee for event).
   */
  private boolean hasRequiredAttendees(Event event, MeetingRequest request) {
    for (String reqAttendee : request.getAttendees()) {
      if (event.getAttendees().contains(reqAttendee)) {
          return true;
      }
    }
    return false;
  }

  /*
   * Return whether attendees for event overlap with all (required 
   * and optional) attendees for request (i.e. at least one attendee 
   * for request is an attendee for event).
   */
  private boolean hasAllAttendees(Event event, MeetingRequest request) {
    if (hasRequiredAttendees(event, request)) {
      return true;
    }
    for (String opAttendee : request.getOptionalAttendees()) {
      if (event.getAttendees().contains(opAttendee)) {
        return true;
      }
    }
    return false;
  }
}
