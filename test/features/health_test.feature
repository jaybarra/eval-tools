Feature: Health Check

  Eval Tools has a health check endpoint to show system status.

  Background:
    Given The server is running

  Scenario: Fetching the health check endpoint
    When I send a "GET" request to "/api/health"
    Then I get a response with "application/json"

