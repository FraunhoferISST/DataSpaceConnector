package de.fraunhofer.isst.dataspaceconnector.services.messages.handler;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageImpl;
import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.ConnectorConfigurationException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.RequestFormatException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.UUIDFormatException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.contract.ContractAgreementNotFoundException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.contract.ContractException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.resource.InvalidResourceException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.resource.ResourceException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.resource.ResourceNotFoundException;
import de.fraunhofer.isst.dataspaceconnector.model.ResourceContract;
import de.fraunhofer.isst.dataspaceconnector.services.messages.NegotiationService;
import de.fraunhofer.isst.dataspaceconnector.services.messages.request.ArtifactRequestService;
import de.fraunhofer.isst.dataspaceconnector.services.messages.response.ArtifactResponseService;
import de.fraunhofer.isst.dataspaceconnector.services.resources.ContractAgreementService;
import de.fraunhofer.isst.dataspaceconnector.services.resources.OfferedResourceServiceImpl;
import de.fraunhofer.isst.dataspaceconnector.services.resources.ResourceService;
import de.fraunhofer.isst.dataspaceconnector.services.usagecontrol.PolicyHandler;
import de.fraunhofer.isst.dataspaceconnector.services.utils.UUIDUtils;
import de.fraunhofer.isst.ids.framework.configuration.ConfigurationContainer;
import de.fraunhofer.isst.ids.framework.messaging.core.handler.api.MessageHandler;
import de.fraunhofer.isst.ids.framework.messaging.core.handler.api.SupportedMessageType;
import de.fraunhofer.isst.ids.framework.messaging.core.handler.api.model.BodyResponse;
import de.fraunhofer.isst.ids.framework.messaging.core.handler.api.model.ErrorResponse;
import de.fraunhofer.isst.ids.framework.messaging.core.handler.api.model.MessagePayload;
import de.fraunhofer.isst.ids.framework.messaging.core.handler.api.model.MessageResponse;
import java.net.URI;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This @{@link ArtifactMessageHandler} handles all
 * incoming messages that have a {@link de.fraunhofer.iais.eis.ArtifactRequestMessageImpl} as part
 * one in the multipart message. This header must have the correct '@type' reference as defined in
 * the {@link de.fraunhofer.iais.eis.ArtifactRequestMessageImpl} JsonTypeName annotation.
 */
@Component
@SupportedMessageType(ArtifactRequestMessageImpl.class)
public class ArtifactMessageHandler implements MessageHandler<ArtifactRequestMessageImpl> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ArtifactMessageHandler.class);

    private final ResourceService resourceService;
    private final PolicyHandler policyHandler;
    private final ArtifactResponseService responseService;
    private final ArtifactRequestService requestService;
    private final NegotiationService negotiationService;
    private final ContractAgreementService contractAgreementService;
    private final ConfigurationContainer configurationContainer;

    /**
     * Constructor for ArtifactMessageHandler.
     *
     * @throws IllegalArgumentException if one of the passed parameters is null
     */
    @Autowired
    public ArtifactMessageHandler(OfferedResourceServiceImpl offeredResourceService,
        PolicyHandler policyHandler, NegotiationService negotiationService,
        ArtifactResponseService responseService, ArtifactRequestService requestService,
        ContractAgreementService contractAgreementService,
        ConfigurationContainer configurationContainer)
        throws IllegalArgumentException {
        if (offeredResourceService == null)
            throw new IllegalArgumentException("The OfferedResourceService cannot be null.");

        if (policyHandler == null)
            throw new IllegalArgumentException("The PolicyHandler cannot be null.");

        if (configurationContainer == null)
            throw new IllegalArgumentException("The ConfigurationContainer cannot be null.");

        if (responseService == null)
            throw new IllegalArgumentException("The ArtifactResponseService cannot be null.");

        if (requestService == null)
            throw new IllegalArgumentException("The ArtifactRequestService cannot be null.");

        if (negotiationService == null)
            throw new IllegalArgumentException("The NegotiationService cannot be null.");

        if (contractAgreementService == null)
            throw new IllegalArgumentException("The ContractAgreementRepository cannot be null.");

        this.resourceService = offeredResourceService;
        this.policyHandler = policyHandler;
        this.responseService = responseService;
        this.requestService = requestService;
        this.negotiationService = negotiationService;
        this.contractAgreementService = contractAgreementService;
        this.configurationContainer = configurationContainer;
    }

    /**
     * This message implements the logic that is needed to handle the message. As it returns the
     * input as string the messagePayload-InputStream is converted to a String.
     *
     * @throws ConnectorConfigurationException - if no connector is configurated.
     * @throws RuntimeException                - if the response body failed to be build.
     */
    @Override
    // NOTE: Make runtime exception more concrete and add ConnectorConfigurationException, ResourceTypeException
    public MessageResponse handleMessage(ArtifactRequestMessageImpl requestMessage,
        MessagePayload messagePayload) throws RuntimeException {
        if (requestMessage == null) {
            LOGGER.warn("Cannot respond when there is no request.");
            throw new IllegalArgumentException("The requestMessage cannot be null.");
        }

        // Get a local copy of the current connector.
        var connector = configurationContainer.getConnector();

        // Check if version is supported.
        if (!responseService.versionSupported(requestMessage.getModelVersion())) {
            LOGGER.warn("Information Model version of requesting connector is not supported.");
            return ErrorResponse.withDefaultHeader(
                RejectionReason.VERSION_NOT_SUPPORTED,
                "Information model version not supported.",
                connector.getId(), connector.getOutboundModelVersion());
        }

        try {
            // Find artifact and matching resource.
            final var artifactId = extractArtifactIdFromRequest(requestMessage);
            final var requestedResource = requestService.findResourceFromArtifactId(artifactId);

            // Check if the transferred contract matches the requested artifact.
            if (!checkTransferContract(requestMessage.getTransferContract(),
                requestMessage.getRequestedArtifact())) {
                LOGGER.debug("Contract agreement could not be found. [id=({}), contractId=({})]",
                    requestMessage.getId(), requestMessage.getTransferContract());

                return ErrorResponse.withDefaultHeader(
                    RejectionReason.BAD_PARAMETERS,
                    "Missing transfer contract or wrong contract.",
                    connector.getId(), connector.getOutboundModelVersion());
            }

            if (requestedResource == null) {
                // The resource was not found, reject and inform the requester.
                LOGGER.debug("Resource could not be found. [id=({}), artifactId=({})]",
                    requestMessage.getId(), artifactId);

                return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                    "An artifact with the given uuid is not known to the "
                        + "connector.",
                    connector.getId(), connector.getOutboundModelVersion());
            }

            try {
                // Find the requested resource and its metadata.
                final var resourceId = UUIDUtils.uuidFromUri(requestedResource.getId());
                final var resourceMetadata = resourceService.getMetadata(resourceId);

                try {
                    // Check if the policy allows data access. TODO: Change to contract agreement. (later)
                    if (policyHandler.onDataProvision(resourceMetadata.getPolicy())) {
                        String data;
                        try {
                            // Get the data from source.
                            data = resourceService.getDataByRepresentation(resourceId, artifactId);
                        } catch (ResourceNotFoundException exception) {
                            LOGGER.debug("Resource could not be found. "
                                    + "[id=({}), resourceId=({}), artifactId=({}), exception=({})]",
                                requestMessage.getId(), resourceId, artifactId,
                                exception.getMessage());
                            return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                                "Resource not found.", connector.getId(),
                                connector.getOutboundModelVersion());
                        } catch (InvalidResourceException exception) {
                            LOGGER.debug("Resource is not in a valid format. "
                                    + "[id=({}), resourceId=({}), artifactId=({}), exception=({})]",
                                requestMessage.getId(), resourceId, artifactId,
                                exception.getMessage());
                            return ErrorResponse.withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR,
                                "Something went wrong.", connector.getId(),
                                connector.getOutboundModelVersion());
                        } catch (ResourceException exception) {
                            LOGGER.warn("Resource could not be received. "
                                    + "[id=({}), resourceId=({}), artifactId=({}), exception=({})]",
                                requestMessage.getId(), resourceId, artifactId,
                                exception.getMessage());
                            return ErrorResponse
                                .withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR,
                                    "Something went wrong.", connector.getId(),
                                    connector.getOutboundModelVersion());
                        }

                        // Build artifact response.
                        responseService.setParameter(
                            requestMessage.getIssuerConnector(),
                            requestMessage.getTransferContract(),
                            requestMessage.getId());

                        return BodyResponse.create(responseService.buildHeader(), data);
                    } else {
                        // The conditions for reading this resource have not been met.
                        LOGGER.debug("Request policy restriction detected for request."
                                + "[id=({}), pattern=({})]",
                            requestMessage.getId(),
                            policyHandler.getPattern(resourceMetadata.getPolicy()));
                        return ErrorResponse.withDefaultHeader(RejectionReason.NOT_AUTHORIZED,
                            "Policy restriction detected: You are not authorized to receive this data.",
                            connector.getId(),
                            connector.getOutboundModelVersion());
                    }
                } catch (ConstraintViolationException exception) {
                    // The response could not be constructed.
                    throw new RuntimeException("Failed to construct the response message.",
                        exception);
                } catch (IllegalArgumentException exception) {
                    LOGGER.warn("Could not deserialize contract. "
                            + "[id=({}), resourceId=({}), artifactId=({}), exception=({})]",
                        requestMessage.getId(), resourceId, artifactId, exception.getMessage());
                    return ErrorResponse.withDefaultHeader(
                        RejectionReason.INTERNAL_RECIPIENT_ERROR,
                        "Policy check failed.",
                        connector.getId(), connector.getOutboundModelVersion());
                }
            } catch (UUIDFormatException exception) {
                // The resource from the database is not identified via uuids.
                LOGGER.debug(
                    "The resource is not valid. The uuid is not valid. [id=({}), exception=({})]",
                    requestMessage.getId(), exception.getMessage());
                return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                    "Resource not found.", connector.getId(),
                    connector.getOutboundModelVersion());
            } catch (ResourceNotFoundException exception) {
                // The resource could be not be found.
                LOGGER.debug("The resource could not be found. [id=({}), exception=({})]",
                    requestMessage.getId(), exception.getMessage());
                return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                    "Resource not found.", connector.getId(),
                    connector.getOutboundModelVersion());
            } catch (InvalidResourceException exception) {
                // The resource could be not be found.
                LOGGER.debug("The resource is not valid. [id=({}), exception=({})]",
                    requestMessage.getId(), exception.getMessage());
                return ErrorResponse.withDefaultHeader(RejectionReason.NOT_FOUND,
                    "Resource not found.", connector.getId(),
                    connector.getOutboundModelVersion());
            }
        } catch (UUIDFormatException exception) {
            // No resource uuid could be found in the request, reject the message.
            LOGGER.debug(
                "Resource has no valid uuid. [id=({}), artifactUri=({}), exception=({})]",
                requestMessage.getId(), requestMessage.getRequestedArtifact(),
                exception.getMessage());
            return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS,
                "No valid resource id found.",
                connector.getId(),
                connector.getOutboundModelVersion());
        } catch (ContractAgreementNotFoundException exception) {
            LOGGER.warn("Could not load contract from database. "
                    + "[id=({}), contractId=({}),exception=({})]",
                requestMessage.getId(), requestMessage.getTransferContract(), exception.getMessage());
            return ErrorResponse.withDefaultHeader(
                RejectionReason.BAD_PARAMETERS,
                "Invalid transfer contract id.",
                connector.getId(), connector.getOutboundModelVersion());
        } catch (ContractException exception) {
            LOGGER.warn("Could not deserialize contract. "
                    + "[id=({}), contractId=({}),exception=({})]",
                requestMessage.getId(), requestMessage.getTransferContract(), exception.getMessage());
            return ErrorResponse.withDefaultHeader(
                RejectionReason.INTERNAL_RECIPIENT_ERROR,
                "Something went wrong.",
                connector.getId(), connector.getOutboundModelVersion());
        }
    }

    /**
     * Extract the artifact id.
     *
     * @throws RequestFormatException - if uuid could not be extracted.
     */
    private UUID extractArtifactIdFromRequest(ArtifactRequestMessage requestMessage)
        throws RequestFormatException {
        try {
            return UUIDUtils.uuidFromUri(requestMessage.getRequestedArtifact());
        } catch (UUIDFormatException exception) {
            throw new RequestFormatException(
                "The uuid could not extracted from request" + requestMessage.getId(),
                exception);
        }
    }

    /**
     * Check if the transfer contract is not null and valid.
     *
     * @return True if everything's fine.
     */
    private boolean checkTransferContract(URI contractId, URI artifactId) throws ContractException {
        if (negotiationService.isStatus()) {
            if (contractId == null) {
                return false;
            } else {
                String contractToString;
                try {
                    UUID uuid = UUIDUtils.uuidFromUri(contractId);
                    // Get contract agreement from database.
                    ResourceContract contract = contractAgreementService.getContract(uuid);
                    // Get contract from database entry.
                    contractToString = contract.getContract();
                } catch (Exception e) {
                    throw new ContractAgreementNotFoundException("Contract could not be loaded "
                        + "from database.");
                }

                Contract agreement;
                try {
                    agreement = policyHandler.validateContract(contractToString);
                } catch (RequestFormatException exception) {
                    throw new ContractException("Could not deserialize contract.");
                }

                URI extractedId = requestService.getArtifactIdFromContract(agreement);
                return extractedId.equals(artifactId);
            }
        } else {
            return true;
        }
    }
}
