package org.github.tkachenkoas.createjira

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.RequestDefinition

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import static org.mockserver.model.JsonBody.json
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals

@MockServerSettings
class CreateJiraActionTest {
    private final ClientAndServer mockServer

    CreateJiraActionTest(ClientAndServer mockServer) {
        this.mockServer = mockServer
    }

    @AfterEach
    void afterEach() {
        mockServer.reset()
    }

    @Test
    void testMainWithAllParameters() {
        mockServer.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/rest/api/3/project")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(json([
                                [key: "PROJ"],
                                [key: "TEST"]
                        ]))
        )

        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/rest/api/3/issue")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([key: "PROJ-123"]))
        )

        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/repos/test-owner/test-repo/issues/1/comments")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([id: 1]))
        )

        Map<String, String> testEnvironment = [
                'INPUT_JIRA_URL'      : "http://localhost:${mockServer.getPort()}",
                'INPUT_JIRA_API_TOKEN': 'test-token',
                'INPUT_GITHUB_TOKEN'  : 'test-github-token',
                'INPUT_COMMAND'       : '/create-jira',
                'GITHUB_API_URL'      : "http://localhost:${mockServer.getPort()}",
                'GITHUB_CONTEXT'      : '''
            {
                "repo": {
                    "owner": "test-owner",
                    "repo": "test-repo"
                },
                "issue": {
                    "number": 1,
                    "title": "Test PR",
                    "body": "This is a test PR",
                    "html_url": "http://github.com/test-owner/test-repo/pull/1"
                },
                "payload": {
                    "comment": {
                        "body": "/create-jira project=\\"PROJ\\" title=\\"Test Title\\" description=\\"Test Description\\"",
                        "user": {
                            "login": "test-user"
                        },
                        "html_url": "http://github.com/test-owner/test-repo/pull/1#issuecomment-1"
                    },
                    "pull_request": {
                        "head": {
                            "ref": "feature/TEST-123-add-new-feature"
                        }
                    }
                }
            }
            '''
        ]

        def contextJson = testEnvironment.get('GITHUB_CONTEXT')
        GitHubContext gitHubContext = new ObjectMapper()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(contextJson, GitHubContext.class)


        def clientParams = new ClientParams(
                jiraUrl: testEnvironment.get('INPUT_JIRA_URL'),
                jiraApiToken: testEnvironment.get('INPUT_JIRA_API_TOKEN'),
                githubToken: testEnvironment.get('INPUT_GITHUB_TOKEN'),
                githubApiUrl: testEnvironment.get('GITHUB_API_URL') ?: "https://api.github.com"
        )
        def command = testEnvironment.get('INPUT_COMMAND') ?: '/create-jira'
        CreateJiraAction.handleCreateJiraCommand(gitHubContext, command, clientParams)

        RequestDefinition jiraRequest = mockServer.retrieveRecordedRequests(HttpRequest.request().withMethod("POST").withPath("/rest/api/3/issue"))[0]
        def expectedJiraBody = '''
        {
            "fields": {
                "project": { "key": "PROJ" },
                "summary": "Test Title",
                "description": "Test Description\\n\\n**Context:**\\n\\nThis JIRA ticket was created from a GitHub Pull Request.\\n\\n- **Pull Request:** [#1](http://github.com/test-owner/test-repo/pull/1)\\n- **Comment:** [View comment](http://github.com/test-owner/test-repo/pull/1#issuecomment-1)\\n- **Author:** @test-user",
                "issuetype": { "name": "Task" },
                "assignee": { "name": "test-user" }
            }
        }
        '''
        assertEquals(expectedJiraBody, jiraRequest.body.toString(), true)

        def githubRequest = mockServer.retrieveRecordedRequests(HttpRequest.request().withMethod("POST").withPath("/repos/test-owner/test-repo/issues/1/comments"))[0]
        String expectedBody = "JIRA ticket created: [PROJ-123](http://localhost:${mockServer.getPort()}/browse/PROJ-123)"
        def expectedGitHubJsomBody = "{\"body\":\"${expectedBody}\"}"
        assertEquals(expectedGitHubJsomBody, githubRequest.body.toString(), true)
    }

    @Test
    void testMainWithoutProject() {
        mockServer.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/rest/api/3/project")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(json([
                                [key: "PROJ"],
                                [key: "TEST"]
                        ]))
        )

        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/rest/api/3/issue")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([key: "TEST-123"]))
        )

        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/repos/test-owner/test-repo/issues/1/comments")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([id: 1]))
        )

        Map<String, String> testEnvironment = [
                'INPUT_JIRA_URL'      : "http://localhost:${mockServer.getPort()}",
                'GITHUB_API_URL'      : "http://localhost:${mockServer.getPort()}",
                'INPUT_JIRA_API_TOKEN': 'test-token',
                'INPUT_GITHUB_TOKEN'  : 'test-github-token',
                'INPUT_COMMAND'       : '/create-jira',
                'GITHUB_CONTEXT'      : '''
            {
                "repo": {
                    "owner": "test-owner",
                    "repo": "test-repo"
                },
                "issue": {
                    "number": 1,
                    "title": "TEST-123 Add new feature",
                    "body": "This is a test PR",
                    "html_url": "http://github.com/test-owner/test-repo/pull/1"
                },
                "payload": {
                    "comment": {
                        "body": "/create-jira title=\\"Test Title\\" description=\\"Test Description\\"",
                        "user": {
                            "login": "test-user"
                        },
                        "html_url": "http://github.com/test-owner/test-repo/pull/1#issuecomment-1"
                    },
                    "pull_request": {
                        "head": {
                            "ref": "feature/TEST-123-add-new-feature"
                        }
                    }
                }
            }
            '''
        ]


        def contextJson = testEnvironment.get('GITHUB_CONTEXT')
        GitHubContext gitHubContext = new ObjectMapper()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(contextJson, GitHubContext.class)

        def clientParams = new ClientParams(
                jiraUrl: testEnvironment.get('INPUT_JIRA_URL'),
                jiraApiToken: testEnvironment.get('INPUT_JIRA_API_TOKEN'),
                githubToken: testEnvironment.get('INPUT_GITHUB_TOKEN'),
                githubApiUrl: testEnvironment.get('GITHUB_API_URL') ?: "https://api.github.com"
        )
        def command = testEnvironment.get('INPUT_COMMAND') ?: '/create-jira'
        CreateJiraAction.handleCreateJiraCommand(gitHubContext, command, clientParams)

        def jiraRequest = mockServer.retrieveRecordedRequests(HttpRequest.request().withMethod("POST").withPath("/rest/api/3/issue"))[0]
        def expectedJiraBody = '''
        {
            "fields": {
                "project": { "key": "TEST" },
                "summary": "Test Title",
                "description": "Test Description\\n\\n**Context:**\\n\\nThis JIRA ticket was created from a GitHub Pull Request.\\n\\n- **Pull Request:** [#1](http://github.com/test-owner/test-repo/pull/1)\\n- **Comment:** [View comment](http://github.com/test-owner/test-repo/pull/1#issuecomment-1)\\n- **Author:** @test-user",
                "issuetype": { "name": "Task" },
                "assignee": { "name": "test-user" }
            }
        }
        '''
        assertEquals(expectedJiraBody, jiraRequest.body.toString(), true)

        def githubRequest = mockServer.retrieveRecordedRequests(HttpRequest.request().withMethod("POST").withPath("/repos/test-owner/test-repo/issues/1/comments"))[0]
        def expectedGitHubBody = "{\"body\":\"JIRA ticket created: [TEST-123](http://localhost:${mockServer.getPort()}/browse/TEST-123)\"}"
        assertEquals(expectedGitHubBody, githubRequest.body.toString(), true)
    }

    @Test
    void testMainWithoutTitle() {
        mockServer.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/rest/api/3/project")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(json([
                                [key: "PROJ"],
                                [key: "TEST"]
                        ]))
        )

        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/rest/api/3/issue")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([key: "PROJ-123"]))
        )

        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/repos/test-owner/test-repo/issues/1/comments")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([id: 1]))
        )

        Map<String, String> testEnvironment = [
                'INPUT_JIRA_URL'      : "http://localhost:${mockServer.getPort()}",
                'GITHUB_API_URL'      : "http://localhost:${mockServer.getPort()}",
                'INPUT_JIRA_API_TOKEN': 'test-token',
                'INPUT_GITHUB_TOKEN'  : 'test-github-token',
                'INPUT_COMMAND'       : '/create-jira',
                'GITHUB_CONTEXT'      : '''
            {
                "repo": {
                    "owner": "test-owner",
                    "repo": "test-repo"
                },
                "issue": {
                    "number": 1,
                    "title": "Test PR",
                    "body": "This is a test PR",
                    "html_url": "http://github.com/test-owner/test-repo/pull/1"
                },
                "payload": {
                    "comment": {
                        "body": "/create-jira project=\\"PROJ\\" description=\\"Test Description\\"",
                        "user": {
                            "login": "test-user"
                        },
                        "html_url": "http://github.com/test-owner/test-repo/pull/1#issuecomment-1"
                    },
                    "pull_request": {
                        "head": {
                            "ref": "feature/TEST-123-add-new-feature"
                        }
                    }
                }
            }
            '''
        ]

        def contextJson = testEnvironment.get('GITHUB_CONTEXT')
        GitHubContext gitHubContext = new ObjectMapper()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(contextJson, GitHubContext.class)
        def clientParams = new ClientParams(
                jiraUrl: testEnvironment.get('INPUT_JIRA_URL'),
                jiraApiToken: testEnvironment.get('INPUT_JIRA_API_TOKEN'),
                githubToken: testEnvironment.get('INPUT_GITHUB_TOKEN'),
                githubApiUrl: testEnvironment.get('GITHUB_API_URL') ?: "https://api.github.com"
        )
        def command = testEnvironment.get('INPUT_COMMAND') ?: '/create-jira'
        CreateJiraAction.handleCreateJiraCommand(gitHubContext, command, clientParams)

        def jiraRequest = mockServer.retrieveRecordedRequests(HttpRequest.request().withMethod("POST").withPath("/rest/api/3/issue"))[0]
        def expectedJiraBody = '''
        {
            "fields": {
                "project": { "key": "PROJ" },
                "summary": "GRTKT: Task from Pull Request comments",
                "description": "Test Description\\n\\n**Context:**\\n\\nThis JIRA ticket was created from a GitHub Pull Request.\\n\\n- **Pull Request:** [#1](http://github.com/test-owner/test-repo/pull/1)\\n- **Comment:** [View comment](http://github.com/test-owner/test-repo/pull/1#issuecomment-1)\\n- **Author:** @test-user",
                "issuetype": { "name": "Task" },
                "assignee": { "name": "test-user" }
            }
        }
        '''
        assertEquals(expectedJiraBody, jiraRequest.body.toString(), true)

        def githubRequest = mockServer.retrieveRecordedRequests(HttpRequest.request().withMethod("POST").withPath("/repos/test-owner/test-repo/issues/1/comments"))[0]
        def expectedGitHubBody = "{\"body\":\"JIRA ticket created: [PROJ-123](http://localhost:${mockServer.getPort()}/browse/PROJ-123)\"}"
        assertEquals(expectedGitHubBody, githubRequest.body.toString(), true)
    }
}
