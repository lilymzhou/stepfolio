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
import java.util.List;

public final class FindMeetingQuery {

  /*
   * Given previously-scheduled {@code events}, query() finds a list of all the 
   * possible time slots throughout a single day that the {@code request} can take 
   * place. If the request contains optional attendees, query() will try to find if 
   * there is at least one time slot that both required and optional attendees can 
   * attend. If a time slot cannot be found, only time slots that required attendees 
   * can attend will be returned.
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
      return new ArrayList<>();
    }

    if (request.hasOptionalAttendees()) {
      ArrayList<TimeRange> optionalSlots = findTimeSlots(events, request, 
        /* includeOptionalAttendees = */ true);
      if (!optionalSlots.isEmpty() || !request.hasAttendees()) {
        return optionalSlots;
      }
    }
    return findTimeSlots(events, request, /* includeOptionalAttendees = */ false);
  }

  /*
   * Sort a list by the start of their TimeRanges.
   */
  private ArrayList<TimeRange> sortArrByStart(Collection<TimeRange> list) {
    ArrayList<TimeRange> newList = new ArrayList<>(list);
    Collections.sort(newList, new Comparator<TimeRange>() {
      @Override
      public int compare(TimeRange a, TimeRange b) {
        return Long.compare(a.start(), b.start());
      }
    });
    return newList;
  }

  /*
   * Finds all the possible time ranges throughout the day that the {@code request}
   * can take place, given already-scheduled {@code events}.
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
     * slots stores the possible time ranges for the meeting request
     * to take place. By default, this stores the entire day, and is gradually
     * cut down into fragments if conflicts arise with events.
     */
    ArrayList<TimeRange> slots = new ArrayList<TimeRange>();
    slots.add(TimeRange.WHOLE_DAY);

    for (Event event : events) {
      TimeRange eventTime = event.getWhen();

      /*
       * Finds whether there is any overlap between {@code event}'s attendees and 
       * {@code request}'s attendees. If there is no overlap, then this event is skipped.
       */
      if ((Collections.disjoint(event.getAttendees(), request.getAttendees())) &&
        (!includeOptionalAttendees ||  
        Collections.disjoint(event.getAttendees(), request.getOptionalAttendees()))){
        continue;
      }

      ArrayList<TimeRange> newSlots = new ArrayList<TimeRange>();
      ArrayList<TimeRange> removeSlots = new ArrayList<TimeRange>();
      for (TimeRange slot : slots) {
        if (slot.overlaps(eventTime)) {
          splitSlotAroundEvent(newSlots, removeSlots, request.getDuration(), slot, eventTime);
        }
      }
      slots.removeAll(removeSlots);
      slots.addAll(newSlots);
    }
    return sortArrByStart(slots);
  }

  /*
   * Split {@code slot} into 0-2 smaller time slots, given that {@code event} at least 
   * partially overlaps slot. If the remaining fragments (beginning of slot - beginning
   * of event) or (end of event - end of slot) are at least as long as {@code reqDuration}
   * they are kept in {@code curSlots} as possible meeting times.
   *
   * @param newSlots: List of Time Ranges to be eventually added to slots, representing
   * possible meeting times for the request.
   * @param removeSlots: List of Time Ranges to be eventually removed from slots, representing
   * time slots that the request can't be held within.
   * @param reqDuration: duration (in minutes) of the requested meeting.
   * @param slot: current member of curSlots to be sliced into smaller fragments (before 
   * and after)
   * @param eventTime: time range that the event currently be considered takes place in.
   */
  private void splitSlotAroundEvent(ArrayList<TimeRange> newSlots, 
    ArrayList<TimeRange> removeSlots, long reqDuration, TimeRange slot, TimeRange eventTime) {

    TimeRange before = TimeRange.fromStartEnd(slot.start(), eventTime.start(), /* inclusive = */ false);
    TimeRange after = TimeRange.fromStartEnd(eventTime.end(), slot.end(), /* inclusive = */ false);

    if (before.duration() >= reqDuration) {
      newSlots.add(before);
    }
    if (after.duration() >= reqDuration) {
      newSlots.add(after);
    }
    removeSlots.add(slot);
  }
}