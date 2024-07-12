package org.github.tkachenkoas.createjira

class GitHubContext {
    Repo repo
    Issue issue
    PullRequest pull_request
    Payload payload

    static class Repo {
        String owner
        String repo
    }

    static class Issue {
        int number
        String title
        String body
        String html_url
    }

    static class PullRequest {
        Head head

        static class Head {
            String ref
        }
    }

    static class Payload {
        Comment comment

        static class Comment {
            String body
            User user
            String html_url

            static class User {
                String login
            }
        }
    }
}

