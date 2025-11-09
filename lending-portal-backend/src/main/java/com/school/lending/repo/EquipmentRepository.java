package com.school.lending.repo;

import com.school.lending.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByCategoryIgnoreCase(String category);

    List<Equipment> findByAvailableQuantityGreaterThan(int qty);
}
