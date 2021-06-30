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
package io.dataspaceconnector.controller.resources;

import java.net.URI;
import java.util.UUID;

import io.dataspaceconnector.controller.resources.exceptions.MethodNotAllowed;
import io.dataspaceconnector.model.RequestedResource;
import io.dataspaceconnector.model.RequestedResourceDesc;
import io.dataspaceconnector.services.resources.ResourceService;
import io.dataspaceconnector.services.resources.SubscriberNotificationService;
import io.dataspaceconnector.view.RequestedResourceViewAssembler;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PagedResourcesAssembler;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {ResourceControllers.RequestedResourceController.class})
class RequestedResourceControllerTest {
    @MockBean
    private ResourceService<RequestedResource, RequestedResourceDesc> service;

    @MockBean
    private RequestedResourceViewAssembler assembler;

    @MockBean
    private PagedResourcesAssembler<RequestedResource> pagedAssembler;

    @MockBean
    private SubscriberNotificationService subscriberNotificationService;

    @Autowired
    @InjectMocks
    private ResourceControllers.RequestedResourceController controller;

    private final UUID resourceId = UUID.randomUUID();

    private final URI subscriberUrl = URI.create("https://subscriber.com");

    @Test
    public void create_null_returnMethodNotAllowed() {
        /* ARRANGE */
        // Nothing to arrange.

        /* ACT && ASSERT */
        assertThrows(MethodNotAllowed.class, () -> controller.create(null));
    }

    @Test
    public void create_validDesc_returnMethodNotAllowed() {
        /* ARRANGE */
        // Nothing to arrange.

        /* ACT && ASSERT */
        assertThrows(MethodNotAllowed.class, () -> controller.create(new RequestedResourceDesc()));
    }

}
