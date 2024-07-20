# Create JIRA Ticket from GitHub Action Comments
This GitHub Action allows you to automatically create JIRA tickets based on comments in GitHub issues or pull requests. This is particularly useful for teams that use JIRA for issue tracking and GitHub for code hosting.

## What It Does
This action listens for comments on GitHub issues or pull requests and creates a JIRA ticket based on the comment content.

When github comment starts with `/create-jira`, the action will parse the comment and create a JIRA ticket
of type "Task" with the provided details.

```
/create-jira PROJ "Fix login issue" "Investigate and fix the login issue that causes a 500 error."
```

A JIRA ticket is created in the PROJ project with the following details:

- Title: Fix login issue
- Description:
```markdown
Investigate and fix the login issue that causes a 500 error.

**Context**

This JIRA ticket was created from a GitHub Pull Request.
**Pull Request**: https://github.com/example/repo/pull/1
**Comment**: https://github.com/example/repo/pull/1#issuecomment-123456789
```

Then, a comment is added to the GitHub issue or pull request indicating the creation of the JIRA ticket:

```
JIRA ticket created: [PROJ-123](https://your-jira-instance/browse/PROJ-123)
```

## Configuration
To use this action, you need to configure following inputs: 

- Inputs
  - jira_url: The URL of your JIRA instance (for example, `https://your-jira-instance.com`)
  - jira_api_token: The API token for authenticating with JIRA (see https://support.atlassian.com/atlassian-account/docs/manage-api-tokens-for-your-atlassian-account/
  - jira_api_user: The user for authenticating with JIRA (the email address associated with your Atlassian account)
 
It's recommended to store the `jira_api_token` in a GitHub secret, while the rest of the inputs can 
be stored in the workflow file. 

## Usage
  Create a `.github/workflows/create-jira-ticket.yml` file in your GitHub repository.
  Add the following content to the file:

```yml
name: Use Create JIRA Action

on:
  issue_comment:
    types: [created]

jobs:
  create-jira-ticket:
    runs-on: ubuntu-latest
    # so that not to run the action on every comment
    if: startsWith(github.event.comment.body, '/create-jira')

    steps:
      - name: Use Create JIRA Action
        uses: tkachenkoas/create-jira@1.0.0
        with:
          jira_url: https://your-jira-domain.atlassian.net
          jira_api_user: your-jira-user-email@example.com
          jira_api_token: ${{ secrets.JIRA_API_TOKEN }}
        env:
          GITHUB_CONTEXT: ${{ toJson(github) }}
```

## Repo configuration
Ensure that you have the following secrets configured in your GitHub repository:

`JIRA_API_TOKEN`: The API token for JIRA.

The action will use the token that github provides in the action 
context to authenticate while leaving the comment in the pull request.
See [Github documentation](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/enabling-features-for-your-repository/managing-github-actions-settings-for-a-repository#configuring-the-default-github_token-permissions)
for details on the permissions of the token.

## Examples
If you want to create a JIRA ticket in the PROJ project with the title Fix login issue and the description Investigate and fix the login issue that causes a 500 error., you would add the following comment to a GitHub issue or pull request:

```
/create-jira PROJ "Fix login issue" "Investigate and fix the login issue that causes a 500 error."
```

If you only want to provide the project and title, you can omit the description:

```/create-jira PROJ "Fix login issue"```

## Feature requests / bug reports / contributions

If you have any feature requests, bug reports, or contributions, 
please open an issue or a pull request. The current version of the action is a
mvp that satisfies the basic requirements, but there are many ways to improve it.