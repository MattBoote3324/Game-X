Feature: Internally generated wind

  Scenario: Veering wind
    Given The initial wind speed is 12 knots
    And The initial wind direction is 300 degrees
    When The wind is veering
    Then the wind direction will change and oscillate from this new direction