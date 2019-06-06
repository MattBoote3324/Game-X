Feature: Delay before race starts

  Scenario: Having a delay before the actual race begins
    Given The race prep phase is 3 seconds
    When The race is begun
    Then the active race will begin 3 seconds later