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
