Feature: User Authentication with Keycloak
  In order to access protected features
  As a visitor
  I want to log in securely via Keycloak

  Scenario: Successful login
    Given I am on the homepage
    When I click the "Login" button
    And I enter valid credentials on the Keycloak page
    Then I should be redirected back to the site
    And I should see my username in the navbar

  Scenario: Failed login
    Given I am on the homepage
    When I click the "Login" button
    And I enter invalid credentials on the Keycloak page
    Then I should see an error message
    And I should not be logged in
