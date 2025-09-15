Feature: Documentation & Showcase
    In order to evaluate project architecture
    As a recruiter
    I want to view design docs, ADRs, and requirements

    Scenario: View design document
        Given I am on the documentation page
        When I select the design document
        Then it should be displayed in readable format

    Scenario: View ADRs
        Given ADRs exist in the repo
        When I select an ADR
        Then the details should be displayed with status

    Scenario: Link to user stories
        Given user stories exist
        When I click a user story link
        Then I should be redirected to the canonical Markdown file
