package io.metersphere.track.issue.client;

import org.springframework.stereotype.Component;

@Component
public class JiraClientV2 extends JiraAbstractClient {
    {
        PREFIX = "/rest/api/2";
    }
}
