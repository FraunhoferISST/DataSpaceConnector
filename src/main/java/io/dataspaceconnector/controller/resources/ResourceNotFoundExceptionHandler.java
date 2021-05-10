package io.dataspaceconnector.controller.resources;

import io.dataspaceconnector.exceptions.ResourceNotFoundException;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Controller for handling {@link ResourceNotFoundException}.
 */
@ControllerAdvice
@Log4j2
@Order(1)
public final class ResourceNotFoundExceptionHandler {
    /**
     * Handle {@link ResourceNotFoundException}.
     *
     * @param exception The thrown exception.
     * @return Response entity with code 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<JSONObject> handleResourceNotFoundException(
            final ResourceNotFoundException exception) {
        if (log.isDebugEnabled()) {
            log.debug("Resource not found. [exception=({})]", exception == null ? ""
                    : exception.getMessage(), exception);
        }

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var body = new JSONObject();
        body.put("message", "Resource not found.");

        return new ResponseEntity<>(body, headers, HttpStatus.NOT_FOUND);
    }
}
