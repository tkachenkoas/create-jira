package org.github.tkachenkoas.createjira

class GitHubContext {
    String token
    String job
    String ref
    String sha
    String repository
    String repository_owner
    String repository_owner_id
    String repositoryUrl
    String run_id
    String run_number
    String retention_days
    String run_attempt
    String artifact_cache_size_limit
    String repository_visibility
    boolean repo_self_hosted_runners_disabled
    String enterprise_managed_business_id
    String repository_id
    String actor_id
    String actor
    String triggering_actor
    String workflow
    String head_ref
    String base_ref
    String event_name
    Event event
    String server_url
    String api_url
    String graphql_url
    String ref_name
    boolean ref_protected
    String ref_type
    String secret_source
    String workflow_ref
    String workflow_sha
    String workspace
    String action
    String event_path
    String action_repository
    String action_ref
    String path
    String env
    String step_summary
    String state
    String output
}

class Event {
    String action
    Comment comment
    Issue issue
    Repository repository
    User sender
}

class Comment {
    String author_association
    String body
    String created_at
    String html_url
    long id
    String issue_url
    String node_id
    String performed_via_github_app
    Reactions reactions
    String updated_at
    String url
    User user
}

class Reactions {
    int plus_one
    int minus_one
    int confused
    int eyes
    int heart
    int hooray
    int laugh
    int rocket
    int total_count
    String url
}

class Issue {
    String active_lock_reason
    User assignee
    List<User> assignees
    String author_association
    String body
    String closed_at
    int comments
    String comments_url
    String created_at
    boolean draft
    String events_url
    String html_url
    long id
    List<String> labels
    String labels_url
    boolean locked
    String milestone
    String node_id
    int number
    PullRequest pull_request
    Reactions reactions
    String repository_url
    String state
    String state_reason
    String timeline_url
    String title
    String updated_at
    String url
    User user
}

class PullRequest {
    String diff_url
    String html_url
    String merged_at
    String patch_url
    String url
}

class Repository {
    boolean allow_forking
    String archive_url
    boolean archived
    String assignees_url
    String blobs_url
    String branches_url
    String clone_url
    String collaborators_url
    String comments_url
    String commits_url
    String compare_url
    String contents_url
    String contributors_url
    String created_at
    String default_branch
    String deployments_url
    String description
    boolean disabled
    String downloads_url
    String events_url
    boolean fork
    int forks
    int forks_count
    String forks_url
    String full_name
    String git_commits_url
    String git_refs_url
    String git_tags_url
    String git_url
    boolean has_discussions
    boolean has_downloads
    boolean has_issues
    boolean has_pages
    boolean has_projects
    boolean has_wiki
    String homepage
    String hooks_url
    String html_url
    long id
    boolean is_template
    String issue_comment_url
    String issue_events_url
    String issues_url
    String keys_url
    String labels_url
    String language
    String languages_url
    String license
    String merges_url
    String milestones_url
    String mirror_url
    String name
    String node_id
    String notifications_url
    int open_issues
    int open_issues_count
    User owner
    boolean isPrivate
    String pulls_url
    String pushed_at
    String releases_url
    int size
    String ssh_url
    int stargazers_count
    String stargazers_url
    String statuses_url
    String subscribers_url
    String subscription_url
    String svn_url
    String tags_url
    String teams_url
    List<String> topics
    String trees_url
    String updated_at
    String url
    int watchers
    int watchers_count
    boolean web_commit_signoff_required
}

class User {
    String avatar_url
    String events_url
    String followers_url
    String following_url
    String gists_url
    String gravatar_id
    String html_url
    long id
    String login
    String node_id
    String organizations_url
    String received_events_url
    String repos_url
    boolean site_admin
    String starred_url
    String subscriptions_url
    String type
    String url
}
