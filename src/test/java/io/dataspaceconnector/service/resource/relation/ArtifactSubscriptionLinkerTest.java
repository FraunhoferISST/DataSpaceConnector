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
package io.dataspaceconnector.service.resource.relation;

import io.dataspaceconnector.model.artifact.Artifact;
import io.dataspaceconnector.model.artifact.ArtifactImpl;
import io.dataspaceconnector.model.subscription.Subscription;
import io.dataspaceconnector.service.resource.type.ArtifactService;
import io.dataspaceconnector.service.resource.type.SubscriptionService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {ArtifactSubscriptionLinker.class})
class ArtifactSubscriptionLinkerTest {

    @MockBean
    ArtifactService artifactService;

    @MockBean
    SubscriptionService subscriptionService;

    @Autowired
    @InjectMocks
    ArtifactSubscriptionLinker linker;

    Artifact artifact = getArtifact();
    Subscription subscription = getSubscription();

    /***********************************************************************************************
     * getInternal                                                                                 *
     **********************************************************************************************/

    @Test
    public void getInternal_null_throwsNullPointerException() {
        /* ARRANGE */
        // Nothing to arrange here.

        /* ACT && ASSERT */
        assertThrows(NullPointerException.class, () -> linker.getInternal(null));
    }

    @Test
    public void getInternal_Valid_returnOfferedResources() {
        /* ARRANGE */
        artifact.getSubscriptions().add(subscription);

        /* ACT */
        final var subscriptions = linker.getInternal(artifact);

        /* ASSERT */
        final var expected = List.of(subscription);
        assertEquals(expected, subscriptions);
    }

    /***********************************************************************************************
     * Utilities.                                                                                  *
     **********************************************************************************************/

    @SneakyThrows
    private ArtifactImpl getArtifact() {
        final var constructor = ArtifactImpl.class.getConstructor();
        constructor.setAccessible(true);

        final var artifact = constructor.newInstance();
        ReflectionTestUtils.setField(artifact, "title", "Artifact");
        ReflectionTestUtils.setField(artifact, "subscriptions", new ArrayList<Subscription>());
        ReflectionTestUtils.setField(artifact, "id", UUID.fromString("554ed409-03e9-4b41-a45a-4b7a8c0aa499"));

        return artifact;
    }

    @SneakyThrows
    private Subscription getSubscription() {
        final var constructor = Subscription.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        final var subscription = constructor.newInstance();
        ReflectionTestUtils.setField(subscription, "title", "Subscription");
        ReflectionTestUtils.setField(subscription, "id", UUID.fromString("a1ed9763-e8c4-441b-bd94-d06996fced9e"));

        return subscription;
    }
}
