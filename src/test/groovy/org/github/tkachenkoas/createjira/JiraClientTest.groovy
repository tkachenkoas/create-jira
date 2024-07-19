package org.github.tkachenkoas.createjira


import org.github.tkachenkoas.createjira.jira.DescriptionJsonUtil
import org.github.tkachenkoas.createjira.jira.JiraClient
import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.mockserver.model.JsonBody

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.mockserver.model.JsonBody.json

@MockServerSettings
class JiraClientTest {
    private final ClientAndServer mockServer

    JiraClientTest(ClientAndServer mockServer) {
        this.mockServer = mockServer
    }

    @Test
    void testGetAvailableProjects() {
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

        def clientParams = new ClientParams(
                jiraUrl: "http://localhost:${mockServer.getPort()}",
                jiraApiToken: 'test-token',
                jiraApiUser: 'test-user',
                githubToken: '',
                githubApiUrl: ''
        )

        def projects = JiraClient.getAvailableProjects(clientParams)

        assertEquals(['PROJ', 'TEST'], projects)
    }

    @Test
    void testCreateJiraTicket() {
        def basicAuth = "Basic " + Base64.getEncoder().encodeToString(
                "test-user:test-token".getBytes()
        )
        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/rest/api/3/issue")
                        .withHeader("Authorization", basicAuth)
                        .withBody(new JsonBody("""
                            {
                               "fields":{
                                  "project":{
                                     "key":"KAN"
                                  },
                                  "summary":"Test Title",
                                  "description":{
                                     "version":1,
                                     "type":"doc",
                                     "content":[
                                        {
                                           "type":"paragraph",
                                           "content":[
                                              {
                                                 "type":"text",
                                                 "text":"Test Description"
                                              },
                                              {
                                                 "type":"hardBreak"
                                              },
                                              {
                                                 "type":"hardBreak"
                                              },
                                              {
                                                 "type":"text",
                                                 "text":"Context",
                                                 "marks":[
                                                    {
                                                       "type":"strong"
                                                    }
                                                 ]
                                              }
                                           ]
                                        },
                                        {
                                           "type":"paragraph",
                                           "content":[
                                              {
                                                 "type":"text",
                                                 "text":"This JIRA ticket was created from a GitHub Pull Request."
                                              },
                                              {
                                                 "type":"hardBreak"
                                              },
                                              {
                                                 "type":"text",
                                                 "text":"Pull Request:",
                                                 "marks":[
                                                    {
                                                       "type":"strong"
                                                    }
                                                 ]
                                              },
                                              {
                                                 "type":"text",
                                                 "text":" "
                                              },
                                              {
                                                 "type":"text",
                                                 "text":"https://github.com/tkachenkoas/create-jira/pull/3",
                                                 "marks":[
                                                    {
                                                       "type":"link",
                                                       "attrs":{
                                                          "href":"https://github.com/tkachenkoas/create-jira/pull/3"
                                                       }
                                                    }
                                                 ]
                                              },
                                              {
                                                 "type":"hardBreak"
                                              },
                                              {
                                                 "type":"text",
                                                 "text":"Comment:",
                                                 "marks":[
                                                    {
                                                       "type":"strong"
                                                    }
                                                 ]
                                              },
                                              {
                                                 "type":"text",
                                                 "text":" "
                                              },
                                              {
                                                 "type":"text",
                                                 "text":"https://github.com/tkachenkoas/create-jira/pull/3#issuecomment-2226908283",
                                                 "marks":[
                                                    {
                                                       "type":"link",
                                                       "attrs":{
                                                          "href":"https://github.com/tkachenkoas/create-jira/pull/3#issuecomment-2226908283"
                                                       }
                                                    }
                                                 ]
                                              }
                                           ]
                                        }
                                     ]
                                  },
                                  "issuetype":{
                                     "name":"Task"
                                  }
                               }
                            }   
                            """))
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([key: "KAN-123"]))
        )

        def clientParams = new ClientParams(
                jiraUrl: "http://localhost:${mockServer.getPort()}",
                jiraApiToken: 'test-token',
                jiraApiUser: 'test-user',
                githubToken: '',
                githubApiUrl: ''
        )

        String description = DescriptionJsonUtil.createDescriptionJson(
                'Test Description',
                'https://github.com/tkachenkoas/create-jira/pull/3',
                'https://github.com/tkachenkoas/create-jira/pull/3#issuecomment-2226908283'
        )

        def issueKey = JiraClient.createJiraTicket(
                clientParams, 'KAN', 'Test Title', description
        )

        assertEquals('KAN-123', issueKey)
    }
}
