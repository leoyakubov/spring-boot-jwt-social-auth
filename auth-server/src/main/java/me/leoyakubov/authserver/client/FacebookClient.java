package me.leoyakubov.authserver.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import me.leoyakubov.authserver.model.facebook.FacebookUser;

import java.util.HashMap;
import java.util.Map;

@Service
public class FacebookClient {
    private static final String FACEBOOK_GRAPH_API_BASE = "https://graph.facebook.com";

    private final RestTemplate restTemplate;

    public FacebookClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public FacebookUser getUser(String accessToken) {
        var path = "/me?fields={fields}&redirect={redirect}&access_token={access_token}";
        var fields = "email,first_name,last_name,id,picture.width(720).height(720)";
        final Map<String, String> variables = new HashMap<>();
        variables.put("fields", fields);
        variables.put("redirect", "false");
        variables.put("access_token", accessToken);
        return restTemplate.getForObject(FACEBOOK_GRAPH_API_BASE + path, FacebookUser.class, variables);
    }
}
