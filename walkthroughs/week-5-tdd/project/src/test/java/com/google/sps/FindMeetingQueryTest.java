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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class FindMeetingQueryTest {
  private static final Collection<Event> NO_EVENTS = Collections.emptySet();
  private static final Collection<String> NO_ATTENDEES = Collections.emptySet();

  // Some people that we can use in our tests.
  private static final String PERSON_A = "Person A";
  private static final String PERSON_B = "Person B";
  private static final String PERSON_C = "Person C";

  // All dates are the first day of the year 2020.
  private static final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 0);
  private static final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
  private static final int TIME_0845AM = TimeRange.getTimeInMinutes(8, 45);
  private static final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 0);
  private static final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
  private static final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 0);
  private static final int TIME_1100AM = TimeRange.getTimeInMinutes(11, 00);

  private static final int DURATION_30_MINUTES = 30;
  private static final int DURATION_90_MINUTES = 90;
  private static final int DURATION_1_HOUR = 60;
  private static final int DURATION_2_HOUR = 120;

  private FindMeetingQuery query;

  @Before
  public void setUp() {
    query = new FindMeetingQuery();
  }

  @Test
  public void optionsForNoAttendees() {
    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_1_HOUR);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noOptionsForTooLongOfARequest() {
    // The duration should be longer than a day. This means there should be no options.
    int duration = TimeRange.WHOLE_DAY.duration() + 1;
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), duration);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void eventSplitsRestriction() {
    // The event should split the day into two options (before and after the event).
    Collection<Event> events = Arrays.asList(new Event("Event 1",
        TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyAttendeeIsConsidered() {
    // Have each person have different events. We should see two options because each person has
    // split the restricted times.
    //
    // Events  :       |--A--|     |--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)));
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false);
    TimeRange option3 = TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2, option3);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void overlappingEvents() {
    // Have an event for each person, but have their events overlap. We should only see two options.
    //
    // Events  :       |--A--|
    //                     |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_1_HOUR),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_1_HOUR),
            Arrays.asList(PERSON_B)));
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void nestedEvents() {
    // Have an event for each person, but have one person's event fully contain another's event. We
    // should see two options.
    //
    // Events  :       |----A----|
    //                   |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_90_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)));
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void doubleBookedPeople() {
    // Have one person, but have them registered to attend two events at the same time.
    //
    // Events  :       |----A----|
    //                     |--A--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_1_HOUR),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)));
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoom() {
    // Have one person, but make it so that there is just enough room at one point in the day to
    // have the meeting.
    //
    // Events  : |--A--|     |----A----|
    // Day     : |---------------------|
    // Options :       |-----|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
            Arrays.asList(PERSON_A)));
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void ignoresPeopleNotAttending() {
    // Add an event, but make the only attendee someone different from the person looking to book
    // a meeting. This event should not affect the booking.
    Collection<Event> events = Arrays.asList(new Event("Event 1",
        TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noConflicts() {
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void notEnoughRoom() {
    // Have one person, but make it so that there is not enough room at any point in the day to
    // have the meeting.
    //
    // Events  : |--A-----| |-----A----|
    // Day     : |---------------------|
    // Options :

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
            Arrays.asList(PERSON_A)));
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_1_HOUR);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void impossibleOptionalAttendee() {
    // Have two people have separate events, as in everyAttendeeIsConsidered(). 
    // Also have person C have an all-day event, and have C added as an optional 
    // attendee to the meeting request. The resulting list of time slots should 
    // not change, as it is impossible to find a meeting time where C is present.
    // 
    //
    // Events  :       |--A--|     |--B--|
    // Events  : |--------------C--------------|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.WHOLE_DAY, Arrays.asList(PERSON_C)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B))
    );
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false);
    TimeRange option3 = TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2, option3);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void possibleOptionalAttendee() {
    // A, B, and C have separate events that do not overlap. C is an optional attendee
    // in the meeting request, while A and B are required. Because it is possible 
    // to find a meeting slot where A, B, and C, are all available, the resulting 
    // list of time slots should be non-empty.
    // 
    //
    // Events  :       |--A--|--C--|--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|                 |--2--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_C))
    );
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoomWithOptionalAttendee() {
    // Have one required attendee A, such that there is just enough room 
    // at one point in the day to have the meeting. B is added an optional
    // attendee, but it is impossible to schedule a meeting with B; thus,
    // B is ignored.
    //
    // Events  : |--A--|-B-| |----A----|
    // Day     : |---------------------|
    // Options :       |-----|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0830AM, TIME_0845AM, false),
            Arrays.asList(PERSON_B)),
        new Event("Event 3", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
            Arrays.asList(PERSON_A))
    );
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void optionalAttendeesWithGaps() {
    // Have each person have different events, and have both A and B be added
    // to the request as optional attendees. We should see three options because 
    // each person has split the restricted times.
    //
    // Events  :       |--A--|     |--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)));
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false);
    TimeRange option3 = TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2, option3);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void optionalAttendeesWithNoGaps() {
    // Have each person have different events. Because A and B are optional attendees 
    // without gaps between their schedules, and there are no required attendees, 
    // there should be no options.
    //
    // Events  : |---A---------||------B-------|
    // Day     : |-----------------------------|
    // Options : 

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0900AM, false),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, false),
            Arrays.asList(PERSON_B)));
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void impossibleOptionalAttendeeMixedOrder() {
    // Have two people have separate events, as in everyAttendeeIsConsidered(). 
    // Also have person C have an all-day event, and have C added as an optional 
    // attendee to the meeting request. The resulting list of time slots should 
    // not change, as it is impossible to find a meeting time where C is present.
    // 
    //
    // Events  :       |--A--|     |--B--|
    // Events  : |--------------C--------------|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_B)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 3", TimeRange.WHOLE_DAY, Arrays.asList(PERSON_C))
    );
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false);
    TimeRange option3 = TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2, option3);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void oneEventSplitsTwoExistingSlots() {
    // First event splits the available all-day slot in two, with a half hour gap, then second event starts
    // before and ends after the gap should affect both of the two slots.
    // 
    //
    // Events  :              |--A--|
    // Events  :          |------B------|
    // Day     : |-------------------------------|
    // Options : |---1---|               |---2---|
  
    Collection<Event> events = Arrays.asList(
        new Event("Event 1", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
            Arrays.asList(PERSON_A)),
        new Event("Event 2", TimeRange.fromStartDuration(TIME_0800AM, DURATION_2_HOUR),
            Arrays.asList(PERSON_B))
    );
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    Collection<TimeRange> actual = query.query(events, request);
    TimeRange option1 = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false);
    TimeRange option2 = TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true);
    Collection<TimeRange> expected = Arrays.asList(option1, option2);
    Assert.assertEquals(expected, actual);
  }
}