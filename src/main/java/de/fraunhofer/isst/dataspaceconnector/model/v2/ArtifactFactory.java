package de.fraunhofer.isst.dataspaceconnector.model.v2;

import de.fraunhofer.isst.dataspaceconnector.services.utils.MetaDataUtils;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * Creates and updates an artifact.
 */
@Component
public final class ArtifactFactory implements BaseFactory<Artifact, ArtifactDesc> {
    /**
     * Default constructor.
     */
    private ArtifactFactory() {
        // This constructor is intentionally empty. Nothing to do here.
    }

    /**
     * Create a new artifact.
     *
     * @param desc The description of the new artifact.
     * @return The new artifact.
     */
    @Override
    public Artifact create(final ArtifactDesc desc) {
        final var artifact = new Artifact();
        update(artifact, desc);

        return artifact;
    }

    /**
     * Update an artifact.
     *
     * @param artifact The artifact to be updated.
     * @param desc     The new artifact description.
     * @return True if the artifact has been modified.
     */
    @Override
    public boolean update(final Artifact artifact, final ArtifactDesc desc) {
        final var hasUpdatedTitle = updateTitle(artifact, desc.getTitle());
        final var hasUpdatedData = updateData(artifact, desc);

        return hasUpdatedTitle || hasUpdatedData;
    }

    private boolean updateTitle(final Artifact artifact, final String title) {
        final var newTitle = MetaDataUtils.updateString(artifact.getTitle(), title, "");
        newTitle.ifPresent(artifact::setTitle);

        return newTitle.isPresent();
    }

    private boolean updateData(final Artifact artifact, final ArtifactDesc desc) {
        if (isRemoteData(desc)) {
            return updateRemoteData(
                    artifact, desc.getAccessUrl(), desc.getUsername(), desc.getUsername());
        } else {
            return updateLocalData(artifact, desc.getValue());
        }
    }

    private static boolean isRemoteData(final ArtifactDesc desc) {
        return desc.getAccessUrl() != null && desc.getAccessUrl().toString().length() > 0;
    }

    private boolean updateLocalData(final Artifact artifact, final String value) {
        final var newData = new LocalData();
        newData.setValue(value == null ? "" : value);

        final var oldData = artifact.getData();
        if (oldData instanceof LocalData) {
            if (!oldData.equals(newData)) {
                artifact.setData(newData);
                return true;
            }
        } else {
            artifact.setData(newData);
            return true;
        }

        return false;
    }

    private boolean updateRemoteData(final Artifact artifact, final URL accessUrl,
            final String username, final String password) {
        final var newData = new RemoteData();
        newData.setAccessUrl(accessUrl);
        newData.setUsername(username);
        newData.setPassword(password);

        final var oldData = artifact.getData();
        if (oldData instanceof RemoteData) {
            if (!oldData.equals(newData)) {
                artifact.setData(newData);
                return true;
            }
        } else {
            artifact.setData(newData);
            return true;
        }

        return false;
    }
}
