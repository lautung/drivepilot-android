package com.lautung.phonecar.backend.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lautung.phonecar.backend.support.BackendIntegrationSupport;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpenApiContractTest extends BackendIntegrationSupport {
    private static final Path SNAPSHOT = Path.of("..", "..", "contracts", "openapi", "openapi.json")
            .toAbsolutePath().normalize();

    @Test
    void generatedOpenApi_matchesCommittedSnapshot() throws Exception {
        String response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode actual = sorted(objectMapper.readTree(response));

        if (Boolean.getBoolean("phonecar.updateOpenApiSnapshot")) {
            Files.createDirectories(SNAPSHOT.getParent());
            Files.writeString(SNAPSHOT,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual) + System.lineSeparator(),
                    StandardCharsets.UTF_8);
        }

        assertTrue(Files.isRegularFile(SNAPSHOT),
                "OpenAPI snapshot is missing; run tests with -Dphonecar.updateOpenApiSnapshot=true");
        JsonNode expected = objectMapper.readTree(Files.readString(SNAPSHOT, StandardCharsets.UTF_8));
        assertEquals(expected, actual,
                "OpenAPI contract changed; review it and explicitly update the snapshot");
    }

    private JsonNode sorted(JsonNode node) {
        if (node.isObject()) {
            ObjectNode result = objectMapper.createObjectNode();
            List<String> names = new ArrayList<>();
            node.fieldNames().forEachRemaining(names::add);
            names.sort(Comparator.naturalOrder());
            names.forEach(name -> result.set(name, sorted(node.get(name))));
            return result;
        }
        if (node.isArray()) {
            ArrayNode result = objectMapper.createArrayNode();
            node.forEach(value -> result.add(sorted(value)));
            return result;
        }
        return node;
    }
}
