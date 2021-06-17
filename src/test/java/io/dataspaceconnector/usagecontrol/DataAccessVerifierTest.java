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
package io.dataspaceconnector.usagecontrol;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementBuilder;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.isst.ids.framework.util.IDSUtils;
import io.dataspaceconnector.common.usagecontrol.VerificationResult;
import io.dataspaceconnector.usagecontrol.config.ConnectorConfiguration;
import io.dataspaceconnector.common.exceptions.PolicyRestrictionException;
import io.dataspaceconnector.model.core.Artifact;
import io.dataspaceconnector.model.core.ArtifactImpl;
import io.dataspaceconnector.ids.EntityResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = { DataAccessVerifier.class})
public class DataAccessVerifierTest {

    @MockBean
    private RuleValidator ruleValidator;

    @MockBean
    private ConnectorConfiguration connectorConfig;

    @MockBean
    private EntityResolver entityResolver;

    @Autowired
    private DataAccessVerifier verifier;

    private final URI remoteId = URI.create("https://target.com");

    private final UUID artifactId = UUID.randomUUID();

    @Test
    public void verify_accessAllowed_allowAccess() {
        /* ARRANGE */
        final var artifact = getArtifact();
        final var agreement = getContractAgreement();

        when(entityResolver.getContractAgreementsByTarget(any())).thenReturn(List.of(agreement));
        doNothing().when(ruleValidator).validatePolicy(any(), any(), any(), any());

        /* ACT */
        final var result = verifier.verify(artifact);

        /* ASSERT */
        assertEquals(VerificationResult.ALLOWED, result);
    }

    @Test
    public void verify_accessNotAllowed_denyAccess() {
        /* ARRANGE */
        final var artifact = getArtifact();
        final var agreement = getContractAgreement();

        when(entityResolver.getContractAgreementsByTarget(any())).thenReturn(List.of(agreement));
        doThrow(PolicyRestrictionException.class)
                .when(ruleValidator).validatePolicy(any(), any(), any(), any());
        when(connectorConfig.isAllowUnsupported()).thenReturn(false);

        /* ACT */
        final var result = verifier.verify(artifact);

        /* ASSERT */
        assertEquals(VerificationResult.DENIED, result);
    }

    /**************************************************************************
     * Utilities.
     *************************************************************************/

    private ContractAgreement getContractAgreement() {
        return new ContractAgreementBuilder(URI.create("https://agreement.com"))
                ._contractStart_(IDSUtils.getGregorianNow())
                ._contractEnd_(IDSUtils.getGregorianNow())
                ._permission_(Util.asList(getPermission()))
//                ._prohibition_(new ArrayList<>())
//                ._obligation_(new ArrayList<>())
                .build();
    }

    private Permission getPermission() {
        return new PermissionBuilder()
                ._title_(Util.asList(new TypedLiteral("Example Usage Policy")))
                ._description_(Util.asList(new TypedLiteral("usage-during-interval")))
                ._action_(Util.asList(Action.USE))
                ._constraint_(Util.asList(new ConstraintBuilder()
                        ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                        ._operator_(BinaryOperator.AFTER)
                        ._rightOperand_(new RdfResource("2020-07-11T00:00:00Z",
                                URI.create("xsd:dateTimeStamp")))
                        .build(), new ConstraintBuilder()
                        ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                        ._operator_(BinaryOperator.BEFORE)
                        ._rightOperand_(new RdfResource("2020-07-11T00:00:00Z",
                                URI.create("xsd:dateTimeStamp")))
                        .build()))
                ._target_(remoteId)
                .build();
    }

    private Artifact getArtifact() {
        final var artifact = new ArtifactImpl();
        ReflectionTestUtils.setField(artifact, "id", artifactId);
        ReflectionTestUtils.setField(artifact, "remoteId", remoteId);
        return artifact;
    }

}
