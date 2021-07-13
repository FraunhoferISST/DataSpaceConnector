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
package io.configmanager.extensions.routes;

import io.configmanager.extensions.routes.api.RoutesApi;
import io.configmanager.extensions.routes.api.controller.RoutesController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the configmanager RoutesController class.
 */
@SpringBootTest(classes = {RoutesController.class, RoutesApi.class})
@AutoConfigureMockMvc
class RoutesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthorizedGetRouteErrors() throws Exception {
        mockMvc.perform(get("/api/configmanager/route/error")).andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    @WithMockUser("ADMIN")
    void setRouteErrors() throws Exception {
        mockMvc.perform(
                post("/api/configmanager/route/error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("route error")
                        .with(csrf())
                        .param("body", "test"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @WithMockUser("ADMIN")
    void getRouteErrors() throws Exception {
        mockMvc.perform(get("/api/configmanager/route/error")).andExpect(status().isOk()).andReturn();
    }
}
