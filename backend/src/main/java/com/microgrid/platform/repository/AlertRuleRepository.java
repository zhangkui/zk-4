package com.microgrid.platform.repository;

import com.microgrid.platform.entity.AlertRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    Optional<AlertRule> findByRuleCode(String ruleCode);

    boolean existsByRuleCode(String ruleCode);

    List<AlertRule> findByStatus(Integer status);

    @Modifying
    @Query("UPDATE AlertRule r SET r.status = :status WHERE r.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Query("SELECT r FROM AlertRule r WHERE " +
            "(:ruleType IS NULL OR r.ruleType = :ruleType) AND " +
            "(:alertLevel IS NULL OR r.alertLevel = :alertLevel) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:keyword IS NULL OR r.ruleName LIKE %:keyword% OR r.ruleCode LIKE %:keyword%)")
    Page<AlertRule> searchRules(@Param("ruleType") String ruleType,
                                @Param("alertLevel") String alertLevel,
                                @Param("status") Integer status,
                                @Param("keyword") String keyword,
                                Pageable pageable);

    @Query("SELECT r FROM AlertRule r WHERE r.status = 1")
    List<AlertRule> findAllEnabledRules();
}
