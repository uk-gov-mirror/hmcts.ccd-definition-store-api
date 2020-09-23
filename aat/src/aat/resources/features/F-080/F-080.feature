@F-080 @Ignore
Feature: F-080: Get Case Type details by Jurisdiction

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-080.1
  Scenario: must return case type details for the request
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a valid jurisdiction id]
    And the request [contains a valid caseType id]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    And the response [has the 200 OK code]
    And the response [contains case type details]
    And the response has all other details as expected

  @S-080.2 @Ignore # Response code mismatch, expected: 401, actual: 403 RDM-6628
  Scenario: must return 401 when request does not provide valid authentication credentials
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains an invalid authentication credentials]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 401 unauthorised code]
    And the response has all the details as expected

  @S-080.3 @Ignore # Response code mismatch, expected: 403, actual: 200 Is this valid?
  Scenario: must return 403 when request provides authentic credentials without authorised access to the operation
    Given a user with [an active profile in CCD, and insufficient privilege to the case type]
    When a request is prepared with appropriate values
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 403 forbidden code]
    And the response has all the details as expected

  @S-080.4  @Ignore #RDM-7615 , 200 response code is returned
  Scenario: must return 404 when user provide non-existing JID {jurisdiction references} within the request
    Given a user with [an active profile in CCD]
    When a request is prepared with appropriate values
    And the request [contains a non-existing jurisdiction id]
    And it is submitted to call the [Get Case Type Details] operation of [CCD Definition Store]
    Then a negative response is received
    And the response [contains 404 not found code]
    And the response has all the details as expected
