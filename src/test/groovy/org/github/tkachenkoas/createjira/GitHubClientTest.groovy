package org.github.tkachenkoas.createjira


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

import static org.mockserver.model.JsonBody.json

@MockServerSettings
class GitHubClientTest {
    private final ClientAndServer mockServer

    GitHubClientTest(ClientAndServer mockServer) {
        this.mockServer = mockServer
    }

    @BeforeEach
    void resetServer() {
        mockServer.reset()
    }

    @Test
    void testAddComment() {
        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/repos/test-owner/test-repo/issues/1/comments")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([id: 1]))
        )

        def clientParams = new ClientParams(
                jiraUrl: "http://localhost:${mockServer.getPort()}",
                jiraApiToken: 'test-token',
                githubToken: 'test-github-token',
                githubApiUrl: "http://localhost:${mockServer.getPort()}"
        )

        def commentUrl = 'https://api.github.com/repos/test-owner/test-repo/issues/1/comments'
        GitHubClient.addComment(clientParams, commentUrl, 'PROJ-123')

        def expectedBody =  "JIRA ticket created: [PROJ-123](http://localhost:${mockServer.getPort()}/browse/PROJ-123)".toString()

        mockServer.verify(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/repos/test-owner/test-repo/issues/1/comments")
                        .withBody(json([
                                body: expectedBody
                        ]))
        )
    }
}
