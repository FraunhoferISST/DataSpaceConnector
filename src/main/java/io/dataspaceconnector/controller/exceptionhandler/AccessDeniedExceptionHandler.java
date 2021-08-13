/*
 * Copyright 2020 Fraunhofer Institute for Software and Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dataspaceconnector.controller.exceptionhandler;

import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * This class handles exception of type {@link AccessDeniedException}.
 */
@RestControllerAdvice
@Log4j2
@Order(1)
public class AccessDeniedExceptionHandler {

    /**
     * Handles thrown {@link AccessDeniedException}.
     *
     * @param exception The thrown exception.
     * @return A http response.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<JSONObject> handleAccessDeniedException(
            final AccessDeniedException exception) {
        if (log.isWarnEnabled()) {
            log.warn("Invalid DAT. [exception=({})]", exception == null ? ""
                    : exception.getMessage());
        }

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final var body = new JSONObject();
        body.put("message", "Unable to retrieve valid DAT.");

        return new ResponseEntity<>(body, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
