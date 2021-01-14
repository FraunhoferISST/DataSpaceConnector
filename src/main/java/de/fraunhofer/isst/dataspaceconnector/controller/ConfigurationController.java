package de.fraunhofer.isst.dataspaceconnector.controller;

import de.fraunhofer.iais.eis.ConfigurationModel;
import de.fraunhofer.isst.ids.framework.configuration.ConfigurationContainer;
import de.fraunhofer.isst.ids.framework.configuration.ConfigurationUpdateException;
import de.fraunhofer.isst.ids.framework.spring.starter.SerializerProvider;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * This class provides endpoints for connector configurations via a connected config manager.
 */
@RestController
@RequestMapping("/admin/api")
@Tag(name = "Connector Configuration", description = "Endpoints for connector configuration")
public class ConfigurationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationController.class);

    private final ConfigurationContainer configurationContainer;
    private final SerializerProvider serializerProvider;

    /**
     * Constructor for ConfigurationController.
     *
     * @throws IllegalArgumentException - if one of the parameters is null.
     */
    @Autowired
    public ConfigurationController(ConfigurationContainer configurationContainer,
        SerializerProvider serializerProvider) throws IllegalArgumentException {
        if (configurationContainer == null)
            throw new IllegalArgumentException("The ConfigurationContainer cannot be null.");

        if (serializerProvider == null)
            throw new IllegalArgumentException("The SerializerProvider cannot be null.");

        this.configurationContainer = configurationContainer;
        this.serializerProvider = serializerProvider;
    }

    @Hidden
    @RequestMapping(value = "/configuration", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> updateConfiguration(@RequestBody String updatedConfiguration) {
        try {
            final var serializer = serializerProvider.getSerializer();
            if (serializer == null) {
                throw new NullPointerException("No configuration serializer has been set.");
            }

            final var new_configurationModel =
                serializer.deserialize(updatedConfiguration, ConfigurationModel.class);

            configurationContainer.updateConfiguration(new_configurationModel);
            return new ResponseEntity<>("Configuration successfully updated.", HttpStatus.OK);
        } catch (NullPointerException exception) {
            LOGGER.warn("Failed to receive the serializer. [exception=({})]", exception.getMessage());
            return new ResponseEntity<>("Failed to update configuration.",
                HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException exception) {
            LOGGER.warn("Failed to deserialize the configuration. [exception=({})]", exception.getMessage());
            return new ResponseEntity<>("Failed to update configuration.",
                HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ConfigurationUpdateException exception) {
            LOGGER.warn("Failed to update the configuration. [exception=({})]", exception.getMessage());
            return new ResponseEntity<>("Failed to update configuration.",
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Hidden
    @RequestMapping(value = "/configuration", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getConfiguration() {
        final var config = configurationContainer.getConfigModel();
        if (config != null) {
            // Return the config
            return new ResponseEntity<>(config.toRdf(), HttpStatus.OK);
        } else {
            // No configuration configured
            LOGGER.info("No configuration could be found.");
            return new ResponseEntity<>("No configuration found.", HttpStatus.NOT_FOUND);
        }
    }
}
