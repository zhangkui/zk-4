package com.microgrid.platform.repository;

import com.microgrid.platform.entity.Park;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkRepository extends JpaRepository<Park, Long> {

    Optional<Park> findByParkCode(String parkCode);

    boolean existsByParkCode(String parkCode);

    List<Park> findByStatus(Integer status);

    @Query("SELECT p FROM Park p WHERE p.parkCode LIKE %:keyword% OR p.parkName LIKE %:keyword%")
    List<Park> searchByKeyword(@Param("keyword") String keyword);

    @Modifying
    @Query("UPDATE Park p SET p.status = :status WHERE p.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
