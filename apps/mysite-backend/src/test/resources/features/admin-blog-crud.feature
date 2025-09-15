Feature: Admin Blog CRUD Operations
    In order to manage blog content
    As an admin
    I want to create, edit, and delete blog posts

    Scenario: Create blog post
        Given I am logged in as admin
        When I create a new blog post
        Then the post should appear in the blog list

    Scenario: Edit blog post
        Given a blog post exists
        When I edit the blog post
        Then changes should be saved

    Scenario: Delete blog post
        Given a blog post exists
        When I delete the blog post
        Then it should no longer appear in the list
