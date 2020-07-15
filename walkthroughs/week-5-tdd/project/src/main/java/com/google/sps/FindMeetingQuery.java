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
import java.util.Collections;
import java.util.Comparator;

public final class FindMeetingQuery {

  /*
   * Given previously-scheduled events, query() finds a list of all the possible time slots
   * throughout a single day that the requested meeting can take place. If the request contains
   * optional attendees, query() will try to find if there is at least one time slot
   * that both required and optional attendees can attend. If a time slot cannot be found,
   * only time slots that required attendees can attend will be returned.
   *
   * @param events: List of already-scheduled events that may conflict with meeting request.
   * @param request: Meeting request (duration and a list of attendees) to be scheduled 
   * into the day
   * @return a list of the possible time ranges for the meeting request to take place.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    /*
     * Request duration is longer than a day, which is impossible to find an appropriate
     * time range for. Returns an empty list.
     */
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return new ArrayList<TimeRange>();
    }
    
    ArrayList<TimeRange> slots = new ArrayList<TimeRange>();
    ArrayList<Event> eventsArr = sortEventsByStart(events);

    slots.add(TimeRange.WHOLE_DAY);

    if (!request.hasOptionalAttendees()) {
      ArrayList<TimeRange> optionalSlots = findTimeSlots(eventsArr, request, 
        slots, /* includeOptionalAttendees = */ true);
      if (optionalSlots.isEmpty() && !request.hasAttendees()) {
        return findTimeSlots(eventsArr, request, 
          slots, /* includeOptionalAttendees = */ false);
      }
      return optionalSlots;
    } else {
      return findTimeSlots(eventsArr, request, slots, /* includeOptionalAttendees = */ false);
    }
  }

  /*
   * Sort events by the start of their TimeRanges.
   */
  private ArrayList<Event> sortEventsByStart(Collection<Event> events) {
    ArrayList<Event> eventsArr = new ArrayList<>(events);
    Collections.sort(eventsArr, new Comparator<Event>() {
      @Override
      public int compare(Event a, Event b) {
        return Long.compare(a.getWhen().start(), b.getWhen().start());
      }
    });
    return eventsArr;
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
