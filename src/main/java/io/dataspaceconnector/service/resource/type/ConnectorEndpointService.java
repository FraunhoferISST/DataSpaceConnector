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
package io.dataspaceconnector.service.resource.type;

import io.dataspaceconnector.model.endpoint.ConnectorEndpoint;
import io.dataspaceconnector.model.endpoint.ConnectorEndpointDesc;
import io.dataspaceconnector.repository.RouteRepository;
import io.dataspaceconnector.service.routing.RouteHelper;
import org.springframework.stereotype.Service;

/**
 * Service class for connector endpoints.
 */
@Service
public class ConnectorEndpointService
        extends EndpointService<ConnectorEndpoint, ConnectorEndpointDesc> {
    /**
     * Constructor for injection.
     * @param routeRepository  the service for managing routes.
     * @param camelRouteHelper The helper class for Camel routes.
     */
    public ConnectorEndpointService(final RouteRepository routeRepository,
                                    final RouteHelper camelRouteHelper) {
        super(routeRepository, camelRouteHelper);
    }
}
