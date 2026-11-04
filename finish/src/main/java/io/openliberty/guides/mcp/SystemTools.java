// tag::copyright[]
// Copyright (c) 2026 IBM Corporation and others.
// Licensed under Creative Commons Attribution-NoDerivatives
// 4.0 International (CC BY-ND 4.0)
//   https://creativecommons.org/licenses/by-nd/4.0/
//
// Contributors:
//     IBM Corporation
// end::copyright[]
package io.openliberty.guides.mcp;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

// tag::rolesAllowedImport[]
import jakarta.annotation.security.RolesAllowed;
// end::rolesAllowedImport[]
import jakarta.enterprise.context.ApplicationScoped;

// tag::mcpImports[]
import org.mcpjava.server.tools.Tool;
import org.mcpjava.server.tools.ToolArg;
import org.mcpjava.server.tools.ToolResponse;
// end::mcpImports[]

// tag::appScoped[]
@ApplicationScoped
// end::appScoped[]
public class SystemTools {

    // tag::getSystemProperties[]
    // tag::toolAnnotation[]
    @Tool(description = "Returns all JVM system properties as key-value pairs.")
    // end::toolAnnotation[]
    // tag::rolesAllowed[]
    @RolesAllowed("admin")
    // end::rolesAllowed[]
    public ToolResponse getSystemProperties() {
        Map<String, String> sorted = new TreeMap<>();
        for (String key : System.getProperties().stringPropertyNames()) {
            sorted.put(key, System.getProperty(key));
        }
        return ToolResponse.ofText(sorted.toString());
    }
    // end::getSystemProperties[]

    // tag::getCurrentDateTime[]
    @Tool(description = "Returns the current date and time in the given IANA "
            + "time zone, for example America/New_York. Defaults to UTC.")
    public ToolResponse getCurrentDateTime(
            // tag::toolArg[]
            @ToolArg(name = "timeZone",
                     description = "IANA time zone ID, for example Europe/London.",
                     required = false,
                     defaultValue = "UTC")
            String timeZone) {
        // end::toolArg[]
        // tag::zoneValidation[]
        if (!ZoneId.getAvailableZoneIds().contains(timeZone)) {
            return ToolResponse.ofError("Unknown time zone: '" + timeZone + "'. "
                    + "Use an IANA zone ID such as 'America/New_York'.");
        }
        // end::zoneValidation[]
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone));
        return ToolResponse.ofText("Current date and time in " + timeZone + ": "
                + now.format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }
    // end::getCurrentDateTime[]
}
