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
package io.dataspaceconnector.controller.message;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.ids.messaging.broker.IDSBrokerService;
import de.fraunhofer.ids.messaging.core.config.ConfigUpdateException;
import de.fraunhofer.ids.messaging.protocol.http.SendMessageException;
import de.fraunhofer.ids.messaging.protocol.multipart.parser.MultipartParseException;
import de.fraunhofer.ids.messaging.requests.MessageContainer;
import io.dataspaceconnector.service.ids.ConnectorService;
import io.dataspaceconnector.util.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ConnectorUnavailableMessageControllerTest {

    @MockBean
    private IDSBrokerService brokerService;

    @MockBean
    private ConnectorService connectorService;

    @Autowired
    private MockMvc mockMvc;

    private final String recipient = "https://someURL";
    private final DynamicAttributeToken token = new DynamicAttributeTokenBuilder()
            ._tokenValue_("token")
            ._tokenFormat_(TokenFormat.JWT)
            .build();

    @Test
    @WithMockUser("ADMIN")
    public void sendConnectorUpdateMessage_noRecipient_throws400() throws Exception {
        /* ARRANGE */
        // Nothing to arrange here.

        /* ACT */
        final var result = mockMvc.perform(post("/api/ids/connector/unavailable"))
                .andReturn();

        /* ASSERT */
        assertTrue(result.getResponse().getContentAsString().isEmpty());
    }

    @Test
    @WithMockUser("ADMIN")
    public void sendConnectorUpdateMessage_failUpdateConfigModel_throws500() throws Exception {
        /* ARRANGE */
        Mockito.doReturn(token).when(connectorService).getCurrentDat();
        Mockito.doThrow(ConfigUpdateException.class).when(connectorService).updateConfigModel();

        /* ACT */
        final var result = mockMvc.perform(post("/api/ids/connector/unavailable")
                .param("recipient", recipient)).andReturn();

        /* ASSERT */
        assertEquals("Failed to update configuration.", result.getResponse().getContentAsString());
        assertEquals(500, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser("ADMIN")
    public void sendConnectorUpdateMessage_failUpdateAtBroker_throws500() throws Exception {
        /* ARRANGE */
        Mockito.doReturn(token).when(connectorService).getCurrentDat();
        Mockito.doThrow(IOException.class).when(brokerService).unregisterAtBroker(Mockito.eq(URI.create(recipient)));

        /* ACT */
        final var result = mockMvc.perform(post("/api/ids/connector/unavailable")
                .param("recipient", recipient)).andReturn();

        /* ASSERT */
        assertEquals(500, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser("ADMIN")
    public void sendConnectorUpdateMessage_brokerEmptyResponseBody_throws500() throws Exception {
        /* ARRANGE */
        Mockito.doReturn(token).when(connectorService).getCurrentDat();
        Mockito.doThrow(IOException.class).when(brokerService).unregisterAtBroker(Mockito.eq(URI.create(recipient)));

        /* ACT */
        final var result = mockMvc.perform(post("/api/ids/connector/unavailable")
                .param("recipient", recipient)).andReturn();

        /* ASSERT */
        assertEquals(500, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser("ADMIN")
    public void sendConnectorUpdateMessage_throwSocketTimeoutException_throws504() throws Exception {
        /* ARRANGE */
        Mockito.doReturn(token).when(connectorService).getCurrentDat();
        Mockito.doThrow(SocketTimeoutException.class).when(brokerService).unregisterAtBroker(Mockito.eq(URI.create(recipient)));

        /* ACT */
        final var result = mockMvc.perform(post("/api/ids/connector/unavailable")
                .param("recipient", recipient)).andReturn();

        /* ASSERT */
        assertEquals(504, result.getResponse().getStatus());
    }

    @Test
    @WithMockUser("ADMIN")
    public void sendConnectorUpdateMessage_throwMultipartParseException_throws502() throws Exception {
        /* ARRANGE */
        Mockito.doReturn(token).when(connectorService).getCurrentDat();
        Mockito.doThrow(MultipartParseException.class).when(brokerService).unregisterAtBroker(Mockito.eq(URI.create(recipient)));

        /* ACT */
        final var result = mockMvc.perform(post("/api/ids/connector/unavailable")
                .param("recipient", recipient)).andReturn();

        /* ASSERT */
        assertEquals(502, result.getResponse().getStatus());
        final var msg = ErrorMessages.INVALID_MESSAGE.toString();
        assertEquals(msg, result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser("ADMIN")
    public void sendConnectorUpdateMessage_throwSendMessageException_throws500() throws Exception {
        /* ARRANGE */
        Mockito.doReturn(token).when(connectorService).getCurrentDat();
        Mockito.doThrow(SendMessageException.class).when(brokerService).unregisterAtBroker(Mockito.eq(URI.create(recipient)));

        /* ACT */
        final var result = mockMvc.perform(post("/api/ids/connector/unavailable")
                .param("recipient", recipient)).andReturn();

        /* ASSERT */
        assertEquals(500, result.getResponse().getStatus());
        final var msg = ErrorMessages.MESSAGE_SENDING_FAILED.toString();
        assertEquals(msg, result.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser("ADMIN")
    public void sendConnectorUpdateMessage_validRequest_returnsResponse() throws Exception {
        /* ARRANGE */
        final var message = new MessageProcessedNotificationMessageBuilder()
                ._issuerConnector_(new URI("https://url"))
                ._correlationMessage_(new URI("https://cormessage"))
                ._issued_(DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar("2009-05-07T17:05:45.678Z"))
                ._senderAgent_(new URI("https://sender"))
                ._modelVersion_("4.0.0")
                ._securityToken_(token)
                .build();

        final var response = new MessageContainer<>(message, "EMPTY");

        Mockito.doReturn(token).when(connectorService).getCurrentDat();
        Mockito.doReturn(response).when(brokerService).unregisterAtBroker(Mockito.eq(URI.create(recipient)));

        /* ACT */
        final var result = mockMvc.perform(post("/api/ids/connector/unavailable")
                .param("recipient", "https://someURL")).andReturn();

        /* ASSERT */
        assertEquals("EMPTY", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }
}
