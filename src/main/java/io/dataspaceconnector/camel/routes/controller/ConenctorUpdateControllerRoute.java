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
package io.dataspaceconnector.camel.routes.controller;

import de.fraunhofer.ids.messaging.core.config.ConfigUpdateException;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ConenctorUpdateControllerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        onException(ConfigUpdateException.class)
                .to("direct:handleConfigUpdateException");

        from("direct:connectorUpdateSender")
                .routeId("connectorUpdateSender")
                .process("ConfigurationUpdater")
                .process("ConnectorUnavailableMessageBuilder")
                .process("RequestWithConnectorPayloadPreparer")
                .toD("idscp2client://${exchangeProperty.recipient}?awaitResponse=true&sslContextParameters=#serverSslContext&useIdsMessages=true")
                .process("ResponseToDtoConverter");
    }

}
