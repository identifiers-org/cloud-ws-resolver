package org.identifiers.cloud.ws.resolver.models;

import java.util.Arrays;
import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-01-26 12:26
 * ---
 */
public class CompactId {
    public static final String COMPACT_ID_PREFIX_AND_ID_SEPARATOR = ":";

    private final String original;
    private String prefix = null;
    private String id = null;

    // Helper to get a string representation of a compact ID by its parts
    public static String getCompactIdString(String prefix, String id) {
        return String.format("%s%s%s", prefix, COMPACT_ID_PREFIX_AND_ID_SEPARATOR, id);
    }

    // Helper
    // TODO - Some identifiers may use ':', and within the prefix you can only have a '.' at the moment
    private void workoutPrefixAndId(String compactId) throws CompactIdException {
        List<String> compactIdParts = Arrays.asList(compactId.split(COMPACT_ID_PREFIX_AND_ID_SEPARATOR));
//        if (compactIdParts.length > 2) {
//            throw new CompactIdException(StringFormatter.format("Invalid compact ID '{}'", compactId).getValue());
//        }
        // Apparently, I no longer have reasons to throw exceptions on parsing a compact ID
        int index = 0;
        if (compactIdParts.size() > 1) {
            // We always use the prefix in lower case
            prefix = compactIdParts.get(index).toLowerCase();
            index++;
        }
        // TODO -- I may need to revisit this for refining the way the compact IDs are formatted and processed
        id = String.join(":", compactIdParts.subList(index, compactIdParts.size()));
    }

    public CompactId(String original) {
        this.original = original;
        // Workout the prefix and ID parts
        workoutPrefixAndId(original);
    }

    public String getOriginal() {
        return original;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getId() {
        return id;
    }
}
