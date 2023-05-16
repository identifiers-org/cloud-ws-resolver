package org.identifiers.cloud.ws.resolver.api.controllers;

import org.identifiers.cloud.ws.resolver.api.ApiCentral;
import org.identifiers.cloud.ws.resolver.services.AsyncMatomoCidResolutionService;
import org.identifiers.cloud.ws.resolver.api.models.ResolverApiModel;
import org.identifiers.cloud.ws.resolver.api.responses.ResponseResolvePayload;
import org.identifiers.cloud.ws.resolver.api.responses.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.org.cloud.ws.resolver.controllers
 * Timestamp: 2018-01-15 12:31
 * ---
 */
@RestController
public class ResolverApiController {
    private static final Logger logger = LoggerFactory.getLogger(ResolverApiController.class);

    class ProviderCompactIdTuple {
        String provider;
        String compactIdentifier;

        public String getProvider() {
            return provider;
        }

        public ProviderCompactIdTuple setProvider(String provider) {
            this.provider = provider;
            return this;
        }

        public String getCompactIdentifier() {
            return compactIdentifier;
        }

        public ProviderCompactIdTuple setCompactIdentifier(String compactIdentifier) {
            this.compactIdentifier = compactIdentifier;
            return this;
        }
    }

    @Autowired
    private ResolverApiModel model;

    @Autowired
    private AsyncMatomoCidResolutionService matomoModel;



    // Compact Identifier and provider code helper
    @Deprecated
    private ProviderCompactIdTuple extractProviderAndCompactIdentifier(String resolutionRequest) {
        String provider = null;
        String compactIdentifier = null;
        if (resolutionRequest.contains((":"))) {
            // We divide based on ':' and first '/'
            String[] splitByColon = resolutionRequest.split(":");
            if (splitByColon[0].contains("/")) {
                provider = splitByColon[0].split("/")[0];
                compactIdentifier = resolutionRequest
                        .replaceFirst(provider, "")
                        .replaceFirst("/", "");
            } else {
                compactIdentifier = resolutionRequest;
            }
        } else if (resolutionRequest.contains("/")) {
            // We look for the first '/' to find the provider code
            provider = resolutionRequest.split("/")[0];
            compactIdentifier = compactIdentifier = resolutionRequest
                    .replaceFirst(provider, "")
                    .replaceFirst("/", "");
        } else {
            // This case is very likely to be a not valid Compact Identifier
            compactIdentifier = resolutionRequest;
        }
        logger.info("For resolution request '{}', found provider code '{}' and compact identifier '{}'",
                resolutionRequest, provider, compactIdentifier);
        return new ProviderCompactIdTuple().setCompactIdentifier(compactIdentifier).setProvider(provider);
    }

    // TODO - ADOPT THE APPROACH OF THIN CONTROLLERS IN FUTURE ITERATIONS
    @RequestMapping(value = "/{resolutionRequest}/**", method = RequestMethod.GET)
    public ResponseEntity<?> resolve(@PathVariable String resolutionRequest, HttpServletRequest request) {
        final String path =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        logger.info("Resolution request, PATH '{}'", path);

        //ProviderCompactIdTuple providerAndCompactIdentifier = extractProviderAndCompactIdentifier(path.replaceFirst("/", ""));
        ServiceResponse<ResponseResolvePayload> result = model.resolveRawCompactId(path.replaceFirst("/", ""));

        matomoModel.asyncHandleCidResolution(request, result);

        /*if (providerAndCompactIdentifier.getProvider() != null) {
            result = model.resolveCompactId(providerAndCompactIdentifier.getCompactIdentifier(),
                    providerAndCompactIdentifier.getProvider());
        } else {
            result = model.resolveCompactId(providerAndCompactIdentifier.getCompactIdentifier());
        }*/
        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    @GetMapping(value = "/resolveMirId/{mirId}")
    public ResponseEntity<?> resolve(@PathVariable String mirId) {
        try {
            URI namespaceLocation = model.resolveMirId(mirId);
            if (namespaceLocation != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setLocation(namespaceLocation);
                return new ResponseEntity<>(headers, HttpStatus.FOUND);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch(NumberFormatException e) {
            ServiceResponse response = new ServiceResponse();
            response.setApiVersion(ApiCentral.apiVersion);
            response.setErrorMessage("MIRIDs must be in the format MIR:XXXXXXXX or integers up to 8 digit");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (URISyntaxException e) {
            // Should never actually happen on production.
            // If so, check the WS_RESOLVER_CONFIG_REGISTRY_NAMESPACE_REDIRECT_FORMAT application property
            ServiceResponse response = new ServiceResponse();
            response.setApiVersion(ApiCentral.apiVersion);
            response.setErrorMessage("MIRID resolution format is not setup correctly on server. Please contact support at identifiers-org@ebi.ac.uk");
            logger.error("Invalid URI format for MIRID resolution. {}", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

 /*   //@RequestMapping(value = "{compactId}", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<?> queryByCompactId(@PathVariable("compactId") String compactId) {
        // NOTE - I don't like how this looks, if handling exceptions at controller level I think I should go for
        // @ControllerAdvice, but it depends on where the exception belongs to, I think all exceptions belonging to the
        // business logic should be caught and handled at the model level (the main model associated to the controller),
        // and only request related exceptions should be handled at the controller level, probably via @ControllerAdvice
        // mechanism and error controller, that I need to implement anyway.
        ServiceResponse result = model.resolveCompactId(compactId);
        return new ResponseEntity<>(result, result.getHttpStatus());
    }

    //@RequestMapping(value = "{selector}/{compactId}", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<?> queryBySelectorAndCompactId(@PathVariable("selector") String selector, @PathVariable("compactId") String compactId) {
        // NOTE - I don't like how this looks, if handling exceptions at controller level I think I should go for
        // @ControllerAdvice, but it depends on where the exception belongs to, I think all exceptions belonging to the
        // business logic should be caught and handled at the model level (the main model associated to the controller),
        // and only request related exceptions should be handled at the controller level, probably via @ControllerAdvice
        // mechanism and error controller, that I need to implement anyway.
        ServiceResponse result = model.resolveCompactId(compactId, selector);
        return new ResponseEntity<>(result, result.getHttpStatus());
    }*/
}
