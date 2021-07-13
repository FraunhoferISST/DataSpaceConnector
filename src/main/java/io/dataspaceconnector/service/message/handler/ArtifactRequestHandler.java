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
package io.dataspaceconnector.service.message.handler;


import de.fraunhofer.iais.eis.ArtifactRequestMessageImpl;
import de.fraunhofer.ids.messaging.handler.message.SupportedMessageType;
import io.dataspaceconnector.service.ids.ConnectorService;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

/**
 * This @{@link ArtifactRequestHandler} handles all incoming messages that have a
 * {@link de.fraunhofer.iais.eis.ArtifactRequestMessageImpl} as part one in the multipart message.
 * This header must have the correct '@type' reference as defined in the
 * {@link de.fraunhofer.iais.eis.ArtifactRequestMessageImpl} JsonTypeName annotation.
 */
@Component
@Log4j2
@SupportedMessageType(ArtifactRequestMessageImpl.class)
public class ArtifactRequestHandler extends AbstractMessageHandler<ArtifactRequestMessageImpl> {

    /**
     * Constructs an ArtifactRequestHandler with the required super class parameters.
     *
     * @param template         Template for triggering Camel routes.
     * @param context          Camel context required for constructing the {@link ProducerTemplate}.
     * @param connectorService Service for the current connector configuration.
     */
    public ArtifactRequestHandler(final ProducerTemplate template,
                                  final CamelContext context,
                                  final ConnectorService connectorService) {
        super(template, context, connectorService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHandlerRouteDirect() {
        return "direct:artifactRequestHandler";
    }

}
