package org.github.tkachenkoas.createjira

import groovy.json.JsonSlurper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.HttpResponse
import org.mockserver.model.RequestDefinition

import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.JsonBody.json

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

    void initWithBody(
            String command
    ) {
        def map = [
                'INPUT_JIRA_URL'      : "http://localhost:${mockServer.getPort()}",
                'GITHUB_API_URL'      : "http://localhost:${mockServer.getPort()}",
                'INPUT_JIRA_USER'     : 'test-user',
                'INPUT_JIRA_API_TOKEN': 'test-token'
        ]
        def context = new File(
                'src/test/resources/sample-github-context.json'
        ).text

        def adjusted = context.replace(
                'TEMPLATE_COMMAND',
                command
        )

        map.put('GITHUB_CONTEXT', adjusted)

        setTestSystemProps(map)
    }


    @Test
    void testMainWithAllParameters() {
        String jiraBasicAuth = "Basic " + Base64.getEncoder().encodeToString(
                "test-user:test-token".getBytes()
        )

        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/rest/api/3/project")
                        .withHeader("Authorization", jiraBasicAuth)
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(json([
                                [key: "PROJ"],
                                [key: "TEST"]
                        ]))
        )

        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/rest/api/3/issue")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([key: "PROJ-123"]))
        )

        mockServer.when(
                request()
                        .withMethod("POST")
                        .withPath("/repos/tkachenkoas/create-jira/issues/3/comments")
                        .withHeader("Authorization", "Bearer the-github-token")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([id: 1]))
        )

        initWithBody("/create-jira PROJ \\\"Test title\\\" \\\"Test Description\\\"")

        CreateJiraAction.main(null)

        RequestDefinition jiraRequest = mockServer.retrieveRecordedRequests(
                request().withMethod("POST")
                        .withPath("/rest/api/3/issue")
        )[0]

        def receivedBody = jiraRequest.body.toString()
        def bodyAsJson = new JsonSlurper().parseText(receivedBody)
        assert bodyAsJson.fields.project.key == "PROJ"
        assert bodyAsJson.fields.summary == "Test title"

        def actualDescription = bodyAsJson.fields.description as String
        assert actualDescription.contains("Test Description")
        assert actualDescription.contains("**Context:**")
        assert actualDescription.contains("This JIRA ticket was created from a GitHub Pull Request.")
        assert actualDescription.contains("**Pull Request:** [#3](https://github.com/tkachenkoas/create-jira/pull/3)")
        assert actualDescription.contains("**Comment:** [View comment](https://github.com/tkachenkoas/create-jira/pull/3#issuecomment-2226908283)")
        assert actualDescription.contains("**Author:** @tkachenkoas")

        assert bodyAsJson.fields.issuetype.name == "Task"

        def githubRequest = mockServer.retrieveRecordedRequests(request()
                .withMethod("POST")
                .withPath("/repos/tkachenkoas/create-jira/issues/3/comments")
        )
        String expectedBody = "JIRA ticket created: [PROJ-123](http://localhost:${mockServer.getPort()}/browse/PROJ-123)"
        def actualBody = githubRequest.body.toString()
        assert actualBody.contains(expectedBody)
    }

    private static void setTestSystemProps(Map<String, String> env) {
        env.entrySet().forEach {
            System.setProperty(it.key, it.value)
        }
    }
}
