package org.github.tkachenkoas.createjira.jira

class DescriptionJsonUtil {


    static String createDescriptionJson(
            String description,
            String prLink,
            String commentLink
    ) {
        return """
        {
            "version": 1,
            "type": "doc",
            "content": [
                {
                    "type": "paragraph",
                    "content": [
                        {
                            "type": "text",
                            "text": "${description ?: ''}",
                        },
                        {
                            "type": "hardBreak"
                        },
                        {
                            "type": "hardBreak"
                        },
                        {
                            "type": "text",
                            "text": "Context",
                            "marks": [
                                {
                                    "type": "strong"
                                }
                            ]
                        }
                    ]
                },
                {
                    "type": "paragraph",
                    "content": [
                        {
                            "type": "text",
                            "text": "This JIRA ticket was created from a GitHub Pull Request."
                        },
                        {
                            "type": "hardBreak"
                        },
                        {
                            "type": "text",
                            "text": "Pull Request:",
                            "marks": [
                                {
                                    "type": "strong"
                                }
                            ]
                        },
                        {
                            "type": "text",
                            "text": " "
                        },
                        {
                            "type": "text",
                            "text": "${prLink}",
                            "marks": [
                                {
                                    "type": "link",
                                    "attrs": {
                                        "href": "${prLink}"
                                    }
                                }
                            ]
                        },
                        {
                            "type": "hardBreak"
                        },
                        {
                            "type": "text",
                            "text": "Comment:",
                            "marks": [
                                {
                                    "type": "strong"
                                }
                            ]
                        },
                        {
                            "type": "text",
                            "text": " "
                        },
                        {
                            "type": "text",
                            "text": "${commentLink}",
                            "marks": [
                                {
                                    "type": "link",
                                    "attrs": {
                                        "href": "${commentLink}"
                                    }
                                }
                            ]
                        }
                    ]
                }
            ]
        }
        """
    }


}
