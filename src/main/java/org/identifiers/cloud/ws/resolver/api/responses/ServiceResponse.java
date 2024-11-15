package org.identifiers.cloud.ws.resolver.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: libapi
 * Package: org.identifiers.cloud.ws.resolver.models.api.responses
 * Timestamp: 2018-03-06 11:32
 * ---
 */
@JsonIgnoreProperties(value = {"httpStatus"})
public class ServiceResponse<T> implements Serializable {
    private String apiVersion;
    private String errorMessage;
    private HttpStatus httpStatus = HttpStatus.OK;
    // payload
    private T payload;

    public String getApiVersion() {
        return apiVersion;
    }

    public ServiceResponse<T> setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ServiceResponse<T> setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public ServiceResponse<T> setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public T getPayload() {
        return payload;
    }

    public ServiceResponse<T> setPayload(T payload) {
        this.payload = payload;
        return this;
    }
}
