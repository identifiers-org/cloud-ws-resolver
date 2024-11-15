package org.identifiers.cloud.ws.resolver.models;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-01-26 15:11
 * ---
 *
 * Unit tests for Compact ID parsing
 */
class CompactIdTest {
    @ParameterizedTest
    @MethodSource("testingValues")
    void wellFormedCompactId(String compactIdStr, String expectedPrefix, String expectedId, String testDescription) {
        CompactId compactId = new CompactId(compactIdStr);
        assertTrue((compactId.getPrefix() == null ? expectedPrefix == null : compactId.getPrefix().equals(expectedPrefix)), testDescription);
        assertTrue((compactId.getId() == null ? expectedId == null : compactId.getId().equals(expectedId)), testDescription);
    }

    private static Stream<Arguments> testingValues() {
        return Stream.of(
            Arguments.of("GO:9876345", "go", "9876345", "Normal use case for Compact ID"),
            Arguments.of("Q9876345", null, "Q9876345", "Compact ID with no prefix"),
            Arguments.of("prefix:IDpart1:IDpart2:Q9876345", "prefix", "IDpart1:IDpart2:Q9876345", "Compact ID using ':' within the ID part")
        );
    }
}