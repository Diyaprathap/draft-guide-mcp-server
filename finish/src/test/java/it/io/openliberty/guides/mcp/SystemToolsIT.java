// tag::copyright[]
// Copyright (c) 2026 IBM Corporation and others.
// Licensed under Creative Commons Attribution-NoDerivatives
// 4.0 International (CC BY-ND 4.0)
//   https://creativecommons.org/licenses/by-nd/4.0/
//
// Contributors:
//     IBM Corporation
// end::copyright[]
package it.io.openliberty.guides.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SystemToolsIT {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static String url;
    private static String adminAuth;
    private static String userAuth;

    @BeforeAll
    public static void setUp() {
        String port = System.getProperty("http.port");
        String contextRoot = System.getProperty("context.root");
        url = "http://localhost:" + port + "/" + contextRoot + "/mcp";
        adminAuth = basicAuth("BobTheAdmin", System.getProperty("admin.password"));
        userAuth = basicAuth("testuser", System.getProperty("user.password"));
    }

    // tag::testToolsListAdmin[]
    @Test
    public void testToolsListForAdmin() throws Exception {
        String sessionId = initialize(adminAuth);
        HttpResponse<String> response = post(adminAuth, sessionId,
            "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}");

        int expected = 200;
        int actual = response.statusCode();
        assertEquals(expected, actual, "Unexpected response code");
        assertTrue(response.body().contains("getCurrentDateTime"),
            "The getCurrentDateTime tool is missing");
        assertTrue(response.body().contains("getSystemProperties"),
            "The getSystemProperties tool is missing for an admin");
    }
    // end::testToolsListAdmin[]

    // tag::testToolsListUser[]
    @Test
    public void testToolsListHidesAdminToolFromUser() throws Exception {
        String sessionId = initialize(userAuth);
        HttpResponse<String> response = post(userAuth, sessionId,
            "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}");

        assertTrue(response.body().contains("getCurrentDateTime"),
            "The getCurrentDateTime tool is missing");
        assertFalse(response.body().contains("getSystemProperties"),
            "The getSystemProperties tool must be hidden from non-admin users");
    }
    // end::testToolsListUser[]

    // tag::testDateTimeDefault[]
    @Test
    public void testGetCurrentDateTimeDefaultsToUtc() throws Exception {
        String sessionId = initialize(null);
        HttpResponse<String> response = callTool(null, sessionId,
            "getCurrentDateTime", "{}");

        assertTrue(response.body().contains("Current date and time in UTC"),
            "The time zone should default to UTC");
        assertTrue(response.body().contains("\"isError\":false"),
            "The call should not be an error");
    }
    // end::testDateTimeDefault[]

    // tag::testDateTimeZone[]
    @Test
    public void testGetCurrentDateTimeWithTimeZone() throws Exception {
        String sessionId = initialize(null);
        HttpResponse<String> response = callTool(null, sessionId,
            "getCurrentDateTime", "{\"timeZone\":\"Asia/Tokyo\"}");

        assertTrue(response.body().contains("Current date and time in Asia/Tokyo"),
            "The response should use the requested time zone");
    }
    // end::testDateTimeZone[]

    // tag::testDateTimeInvalid[]
    @Test
    public void testGetCurrentDateTimeWithInvalidTimeZone() throws Exception {
        String sessionId = initialize(null);
        HttpResponse<String> response = callTool(null, sessionId,
            "getCurrentDateTime", "{\"timeZone\":\"Not/AZone\"}");

        assertTrue(response.body().contains("\"isError\":true"),
            "An unknown time zone should return an error result");
        assertTrue(response.body().contains("Unknown time zone"),
            "The error message should explain the problem");
    }
    // end::testDateTimeInvalid[]

    // tag::testPropertiesAdmin[]
    @Test
    public void testGetSystemPropertiesAsAdmin() throws Exception {
        String sessionId = initialize(adminAuth);
        HttpResponse<String> response = callTool(adminAuth, sessionId,
            "getSystemProperties", "{}");

        int expected = 200;
        int actual = response.statusCode();
        assertEquals(expected, actual, "Unexpected response code");
        assertTrue(response.body().contains("java.version"),
            "The response should contain the JVM system properties");
    }
    // end::testPropertiesAdmin[]

    // tag::testPropertiesAnonymous[]
    @Test
    public void testGetSystemPropertiesWithoutCredentials() throws Exception {
        String sessionId = initialize(null);
        HttpResponse<String> response = callTool(null, sessionId,
            "getSystemProperties", "{}");

        int expected = 401;
        int actual = response.statusCode();
        assertEquals(expected, actual, "Unexpected response code");
    }
    // end::testPropertiesAnonymous[]

    // tag::testPropertiesUser[]
    @Test
    public void testGetSystemPropertiesAsNonAdmin() throws Exception {
        String sessionId = initialize(userAuth);
        HttpResponse<String> response = callTool(userAuth, sessionId,
            "getSystemProperties", "{}");

        int expected = 403;
        int actual = response.statusCode();
        assertEquals(expected, actual, "Unexpected response code");
    }
    // end::testPropertiesUser[]

    // tag::initialize[]
    private static String initialize(String auth) throws Exception {
        HttpResponse<String> response = post(auth, null,
            "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\","
            + "\"params\":{\"protocolVersion\":\"2025-06-18\","
            + "\"capabilities\":{},"
            + "\"clientInfo\":{\"name\":\"SystemToolsIT\",\"version\":\"1.0\"}}}");
        String sessionId = response.headers().firstValue("Mcp-Session-Id")
                                   .orElseThrow();
        post(auth, sessionId,
            "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        return sessionId;
    }
    // end::initialize[]

    private static HttpResponse<String> callTool(String auth, String sessionId,
            String name, String arguments) throws Exception {
        return post(auth, sessionId,
            "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\","
            + "\"params\":{\"name\":\"" + name + "\","
            + "\"arguments\":" + arguments + "}}");
    }

    // tag::post[]
    private static HttpResponse<String> post(String auth, String sessionId,
            String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json, text/event-stream")
            .POST(HttpRequest.BodyPublishers.ofString(body));
        if (auth != null) {
            request.header("Authorization", auth);
        }
        if (sessionId != null) {
            request.header("Mcp-Session-Id", sessionId);
        }
        return CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    // end::post[]

    private static String basicAuth(String user, String password) {
        String credentials = user + ":" + password;
        return "Basic " + Base64.getEncoder()
            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
