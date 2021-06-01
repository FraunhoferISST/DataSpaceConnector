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
package io.dataspaceconnector.services.messages.handler;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.NotificationMessageImpl;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.MessageHandler;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.MessagePayload;
import de.fraunhofer.isst.ids.framework.messaging.model.messages.SupportedMessageType;
import de.fraunhofer.isst.ids.framework.messaging.model.responses.BodyResponse;
import de.fraunhofer.isst.ids.framework.messaging.model.responses.ErrorResponse;
import de.fraunhofer.isst.ids.framework.messaging.model.responses.MessageResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This @{@link NotificationMessageHandler} handles all incoming messages that have a
 * {@link de.fraunhofer.iais.eis.NotificationMessageImpl} as part one in the multipart message.
 * This header must have the correct '@type' reference as defined in the
 * {@link de.fraunhofer.iais.eis.NotificationMessageImpl} JsonTypeName annotation.
 */
@Component
@SupportedMessageType(NotificationMessageImpl.class)
@RequiredArgsConstructor
public class NotificationMessageHandler implements MessageHandler<NotificationMessageImpl> {
    private final @NonNull ProducerTemplate template;

    private final @NonNull CamelContext context;

    /**
     * This message implements the logic that is needed to handle the message. As it just returns
     * the input as string the messagePayload-InputStream is converted to a String.
     *
     * @param message The ids notification message as header.
     * @param payload The message notification message's content.
     * @return The response message.
     */
    @Override
    public MessageResponse handleMessage(
            final NotificationMessageImpl message, final MessagePayload payload) {
        final var result = template.send("direct:notificationMsgHandler",
                ExchangeBuilder.anExchange(context)
                        .withBody(new Request<>(message, payload))
                        .build());

        final var response = result.getIn().getBody(Response.class);
        return response == null ? result.getIn().getBody(ErrorResponse.class)
                                : BodyResponse.create(response.getHeader(), response.getBody());
    }
}
