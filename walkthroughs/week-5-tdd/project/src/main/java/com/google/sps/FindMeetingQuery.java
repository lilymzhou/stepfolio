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
    
    ArrayList<Event> eventsArr = sortEventsByStart(events);

    if (request.hasOptionalAttendees()) {
      ArrayList<TimeRange> optionalSlots = findTimeSlots(eventsArr, request, 
        /* includeOptionalAttendees = */ true);
      if (optionalSlots.isEmpty() && request.hasAttendees()) {
        return findTimeSlots(eventsArr, request, /* includeOptionalAttendees = */ false);
      }
      return optionalSlots;
    } else {
      return findTimeSlots(eventsArr, request, /* includeOptionalAttendees = */ false);
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
   * Finds all the possible time ranges throughout the day that the requested
   * meeting can take place, given already-scheduled events.
   * 
   * @param events: List of already-scheduled events that may conflict with the times
   * in which the request can be scheduled.
   * @param request: Requested meeting to be scheduled into the day.
   * @param includeOptionalAttendees: Find time ranges that work for both required
   * and optional attendees (as opposed to only required attendees).
   * @return List of all possible time ranges that the meeting request can take place within.
   */
  private ArrayList<TimeRange> findTimeSlots(Collection<Event> events, MeetingRequest request,
    boolean includeOptionalAttendees) {
    /*
     * Slots stores the possible time ranges for the meeting request
     * to take place. By default, this stores the entire day, and is gradually
     * cut down into fragments if conflicts arise with events.
     */
    ArrayList<TimeRange> slots = new ArrayList<TimeRange>();
    slots.add(TimeRange.WHOLE_DAY);

    /*
     * A copy of slots that stores new changes to slots without interferring
     * with the for loop below.
     */
    ArrayList<TimeRange> curSlots = new ArrayList<>(slots);

    for (Event event : events) {
      TimeRange eventTime = event.getWhen();
      slots = curSlots;

      /*
       * Finds whether there is any overlap between event's attendees and request's attendees.
       * If there is no overlap, then this event is skipped.
       */
      if (hasNothingInCommon(event.getAttendees(), request.getAttendees())) {
        if (!includeOptionalAttendees || 
          hasNothingInCommon(event.getAttendees(), request.getOptionalAttendees())) {
          continue;
        }
      }

      for (TimeRange slot : slots) {
        if (slot.contains(eventTime)) { // Event is a subset of slot.
          splitSlotAroundEvent(curSlots, request.getDuration(), slot, eventTime,
          /* addBeginningSlot = */ true, /* addEndSlot = */ true);
          break;
        } else if (slot.contains(eventTime.end())) { // End of event is part of slot.
          splitSlotAroundEvent(curSlots, request.getDuration(), slot, eventTime,
          /* addBeginningSlot = */ false, /* addEndSlot = */ true);
        } else if (slot.contains(eventTime.start())) { // Beginning of event is part of slot.
          splitSlotAroundEvent(curSlots, request.getDuration(), slot, eventTime,
          /* addBeginningSlot = */ true, /* addEndSlot = */ false);
        }
      }
    }
    return curSlots;
  }

  /*
   * Split current time slot into 0-2 smaller time slots, given that event at least 
   * partially overlaps slot. If event is a subset of slot, then both the remaining 
   * beginning and end slots can potentially be added in. If event's end is part of 
   * slot, only the difference (end of event - end of slot) can potentially be added in. 
   * If event's beginning is part of slot, only the difference (beginning of slot - 
   * beginning of event) can potentially be added in.
   *
   * @param curSlots: Current list of time ranges that the meeting request can take 
   * place within. This is modified over the course of the function.
   * @param reqDuration: duration (in minutes) of the requested meeting.
   * @param slot: current member of curSlots to be sliced into smaller fragments (before 
   * and after)
   * @param eventTime: time range that the event currently be considered takes place in.
   * @param addBeginningSlot: whether the time range (beginning of slot - beginning of
   * event) should be considered as a valid time slot for the requested meeting.
   * @param addEndSlot: whether the time range (end of event - end of slot) should
   * be considered as a valid time slot for the requested meeting.
   */
  private void splitSlotAroundEvent(ArrayList<TimeRange> curSlots, long reqDuration,
    TimeRange slot, TimeRange eventTime, boolean addBeginningSlot, boolean addEndSlot) {

    TimeRange before = TimeRange.fromStartEnd(slot.start(), eventTime.start(), false);
    TimeRange after = TimeRange.fromStartEnd(eventTime.end(), slot.end() - 1, true);

    if (addBeginningSlot && before.duration() >= reqDuration) {
      curSlots.add(before);
    }
    if (addEndSlot && after.duration() >= reqDuration) {
      curSlots.add(after);
    }
    curSlots.remove(slot);
  }

  /*
   * @groupA, groupB: generalized lists of strings to be compared.
   * @return whether there is any overlap between groupA and groupB (i.e. a String
   * appears in both groupA and groupB.
   */
  private boolean hasNothingInCommon(Collection<String> groupA, Collection<String> groupB) {
    for (String b : groupB) {
      if (groupA.contains(b)) {
        return false;
      }
    }
    return true;
  }
}
