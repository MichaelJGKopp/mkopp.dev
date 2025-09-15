Feature: Public Blog Access
    In order to read blog posts
    As a visitor
    I want to browse and search blog content

    Scenario: View blog posts
        Given the site has published blog posts
        When I visit the blog page
        Then I should see all blog posts in reverse chronological order

    Scenario: Filter blog posts
        Given the site has tagged blog posts
        When I filter by tag
        Then only posts with that tag should be shown
