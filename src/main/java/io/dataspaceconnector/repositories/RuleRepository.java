package io.dataspaceconnector.repositories;

import io.dataspaceconnector.model.ContractRule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * The repository containing all objects of type {@link ContractRule}.
 */
@Repository
public interface RuleRepository extends BaseEntityRepository<ContractRule> {
    /**
     * Finds all rules in a specific contract.
     *
     * @param contractId ID of the contract
     * @return list of all rules in the contract
     */
    @Query("SELECT r "
            + "FROM ContractRule r INNER JOIN Contract c ON r MEMBER OF c.rules "
            + "WHERE c.id = :contractId "
            + "AND r.deleted = false "
            + "AND c.deleted = false")
    List<ContractRule> findAllByContract(UUID contractId);
}
