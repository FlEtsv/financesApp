package com.finances.main.repository;

import com.finances.main.model.FinancialGoal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de objetivos financieros.
 */
public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    /**
     * Lista objetivos por cuenta.
     */
    @Query("""
        select goal
        from FinancialGoal goal
        join fetch goal.account account
        where lower(account.name) = lower(:accountName)
        order by goal.targetDate asc
        """)
    List<FinancialGoal> findByAccountName(@Param("accountName") String accountName);
}
