package org.identifiers.cloud.ws.resolver.periodictasks.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.daemons.models
 * Timestamp: 2018-01-18 10:06
 * ---
 */
//@Component
//@Profile("disabled")
@Deprecated
public class ResolverDataSourcerFromSampleFile implements ResolverDataSourcer {
    private Logger logger = LoggerFactory.getLogger(ResolverDataSourcerFromSampleFile.class);

    @Value("${org.identifiers.cloud.ws.resolver.data.source.file.local.path}")
    private String sampleDataFileLocalPath;

    @Override
    public List<Namespace> getResolverData() throws ResolverDataSourcerException {
        List<Namespace> result = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.readValue(new ClassPathResource(sampleDataFileLocalPath).getInputStream(),
                    new TypeReference<>() {});
        } catch (IOException e) {
            throw new ResolverDataSourcerException(String
                    .format("There was a problem reading the file at '%s' -> '%s'",
                            sampleDataFileLocalPath, e.getMessage()));
        }
        return result;
    }
}
