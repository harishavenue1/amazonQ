@Generate
Feature: API Testing with REST Assured
  As a test developer
  I want to validate API endpoints
  So that I can ensure the API works correctly

  Scenario: Get Single User Details
    Given I have a base URI
    When I send a GET request to "/users?page=2"
    Then the response status code should be 200
    And the response should contain "michael.lawson@reqres.in"
    And the response should have field "data.first_name" at index 0 with value "Michael"
    And the response should have 6 items in data array

  Scenario: Get List of Users
    Given I have a base URI
    When I send a GET request to "/users?page=2"
    Then the response status code should be 200
    And the response should contain "michael.lawson@reqres.in"
    And the response should have field "page" with value "2"

  Scenario: Create New User
    Given I have a base URI
    And I have the following request body:
      """
      {
        "name": "John Doe",
        "job": "Software Engineer"
      }
      """
    When I send a POST request to "/users"
    Then the response status code should be 201
    And the response should contain "John Doe"

  Scenario: Update User Details
    Given I have a base URI
    And I have the following request body:
      """
      {
        "name": "Jane Smith",
        "job": "QA Engineer"
      }
      """
    When I send a PUT request to "/users/2"
    Then the response status code should be 200
    And the response should contain "Jane Smith"

  Scenario: Delete User
    Given I have a base URI
    When I send a DELETE request to "/users/2"
    Then the response status code should be 204

  Scenario: Register User Successfully
    Given I have a base URI
    And I have the following request body:
      """
      {
        "email": "eve.holt@reqres.in",
        "password": "pistol"
      }
      """
    When I send a POST request to "/register"
    Then the response status code should be 200
    And the response should contain "token"
