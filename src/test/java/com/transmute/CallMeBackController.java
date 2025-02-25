package com.docker.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callback")
public class CallMeBackController {
    private static final Logger logger = LoggerFactory.getLogger(CallMeBackController.class);
    private static final String TOKEN_URL = "https://account-d.docusign.com/oauth/token";
    private static final String CLIENT_ID = "fac0771b-a879-4a59-aff7-b3c528fe0c87";
    private static final String CLIENT_SECRET = "4af2b85c-2ec3-433a-811f-b18392851a5a";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";

    //NOTE : DocuSign OAuth requires user interaction (it’s designed for browsers).

    //These Requests from url will trigger below callback API/
    // https://account-d.docusign.com/oauth/auth?response_type=code&scope=signature&client_id=fac0771b-a879-4a59-aff7-b3c528fe0c87&redirect_uri=http://localhost:8080/callback

    // https://account-d.docusign.com/oauth/auth?response_type=code&scope=signature%20impersonation&client_id=fac0771b-a879-4a59-aff7-b3c528fe0c87&redirect_uri=http://localhost:8080/callback

    // These above Url calls will trigger user actions on browser on their end.
    // After only user interactions is when this BE callback method is triggered.
    @GetMapping
    public String handleCallback(@RequestParam("code") String code) {
        System.out.println("✅ Authorization Code: " + code);

        String accessToken = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(TOKEN_URL);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            String body = "grant_type=authorization_code"
                    + "&code=" + code
                    + "&client_id=" + CLIENT_ID
                    + "&client_secret=" + CLIENT_SECRET
                    + "&redirect_uri=" + REDIRECT_URI;

            post.setEntity(new StringEntity(body));

            try (CloseableHttpResponse response = client.execute(post)) {
                String json = EntityUtils.toString(response.getEntity());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(json);

                accessToken = node.get("access_token").asText();
                System.out.println("Access Token: " + accessToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Access Token :" + accessToken;
    }

}
