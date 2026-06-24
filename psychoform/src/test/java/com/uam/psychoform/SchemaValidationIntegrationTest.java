package com.uam.psychoform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SchemaValidationIntegrationTest {

    @Test
    void validatesTheExistingBfaSchema() {
        // Hibernate performs schema validation while the Spring context starts.
    }
}
