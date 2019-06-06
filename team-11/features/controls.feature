Feature: Control Specification

Background:
  Given A race is in progress
  Given Gemma is in control of the boat
  Given The boat in control has its sails out

  Scenario: Gemma presses the Sails in key
    When Gemma presses the "sails in" key
    Then the sails should go in

  Scenario: Gemma presses the Sails out key
    When Gemma presses the "sails out" key
    Then the boat state should not change


  Scenario: Gunner is viewing the race and presses a boat control key
    Given Gunner is viewing the race
    When Gunner presses any boat control keys
    Then the boat state should not change


  Scenario: Gemma presses the Tack/Gybe key
    When Gemma presses the "Tack/Gybe" key
    Then her boat should tack or gybe

  Scenario: Gemma presses the Autopilot key
    When Gemma presses the "Autopilot" key
    Then her boat should autopilot itself



  Scenario: Gemma presses the Upwind key
    Given that the boat is not directly heading upwind
    When Gemma presses the "Upwind" key
    Then her boat heading should move towards upwind

  Scenario: Gemma presses the Upwind key
    Given that the boat is directly heading upwind
    When Gemma presses the "Upwind" key
    Then her boat heading should not change

  Scenario: Gemma presses the Downwind key
    Given that the boat is not directly heading downwind
    When Gemma presses the "Downwind" key
    Then her boat heading should move towards downwind

  Scenario: Gemma presses the Downwind key
    Given that the boat is directly heading downwind
    When Gemma presses the "Downwind" key
    Then her boat heading should not change




