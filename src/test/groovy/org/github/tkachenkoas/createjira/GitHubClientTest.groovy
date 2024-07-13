package org.github.tkachenkoas.createjira


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.JsonBody.json

class GitHubClientTest {
    private static ClientAndServer mockServer

    @BeforeAll
    static void startServer() {
        mockServer = startClientAndServer(2080)
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
                jiraUrl: 'http://localhost:2080',
                jiraApiToken: 'test-token',
                githubToken: 'test-github-token',
                githubApiUrl: 'http://localhost:2080'
        )

        def commentUrl = 'https://api.github.com/repos/test-owner/test-repo/issues/1/comments'
        GitHubClient.addComment(clientParams, commentUrl, 'PROJ-123')

        mockServer.verify(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/repos/test-owner/test-repo/issues/1/comments")
                        .withBody(json([
                                body: "JIRA ticket created: [PROJ-123](http://localhost:2080/browse/PROJ-123)"
                        ]))
        )
    }
}
