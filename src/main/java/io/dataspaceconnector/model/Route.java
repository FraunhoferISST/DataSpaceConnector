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
package io.dataspaceconnector.model;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 *
 */
@Entity
@Table(name = "route")
@SQLDelete(sql = "UPDATE route SET deleted=true WHERE id=?")
@Where(clause = "deleted = false")
@Getter
@Setter(AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class Route extends AbstractEntity {

    /**
     * Serial version uid.
     **/
    private static final long serialVersionUID = 1L;

    /**
     * The deploy method of the route.
     */
    @Enumerated(EnumType.STRING)
    private DeployMethod deployMethod;

    /**
     * List of subroutes.
     */
    @OneToMany
    private List<Route> subRoutes;

    /**
     * The route configuration.
     */
    private String routeConfiguration;

    /**
     * The possible start endpoint of the route.
     */
    @OneToOne
    private GenericEndpoint startGenericEndpoint;

    /**
     * The possible last endpoint of the route.
     */
    @OneToOne
    private GenericEndpoint endGenericEndpoint;

    /**
     * The possible start endpoint of the route.
     */
    @OneToOne
    private IdsEndpoint startIdsEndpoint;

    /**
     * The possible last endpoint of the route.
     */
    @OneToOne
    private IdsEndpoint endIdsEndpoint;

    /**
     * List of offered resources.
     */
    @OneToMany
    private List<OfferedResource> offeredResources;

}
