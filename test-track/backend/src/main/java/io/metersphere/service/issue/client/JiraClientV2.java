package io.metersphere.service.issue.client;

import org.springframework.stereotype.Component;

@Component
public class JiraClientV2 extends JiraAbstractClient {
    {
        PREFIX = "/rest/api/2";
    }
}
