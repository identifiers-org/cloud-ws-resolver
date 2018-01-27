package org.identifiers.cloud.ws.resolver.controllers;

import com.sun.javafx.binding.StringFormatter;
import org.identifiers.cloud.ws.resolver.models.ResolverApiModel;
import org.springframework.web.bind.annotation.*;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.org.cloud.ws.resolver.controllers
 * Timestamp: 2018-01-15 12:31
 * ---
 */
@RestController
public class ResolverApiController {

    private ResolverApiModel resolverApiModel = new ResolverApiModel();

    @RequestMapping(value = "{compactId}", method = RequestMethod.GET)
    public @ResponseBody String queryByCompactId(@PathVariable("compactId") String compactId) {
        // TODO
        String result = resolverApiModel.resolveCompactId(compactId);
        return "[QUERY_BY_COMPACT_ID] Compact ID parameter ---> " + compactId;
    }

    @RequestMapping(value = "{selector}/{compactId}", method = RequestMethod.GET)
    public @ResponseBody String queryBySelectorAndCompactId(@PathVariable("selector") String selector, @PathVariable("compactId") String compactId) {
        // TODO
        return StringFormatter.format("[EXTENDED QUERY] With selector '%s' and compact ID '%s'", selector, compactId).getValue();
    }
}
