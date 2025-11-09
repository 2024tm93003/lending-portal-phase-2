package com.school.lending.repo;

import com.school.lending.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link com.school.lending.model.Equipment} entities.
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /**
     * Find equipment items by category (case-insensitive).
     *
     * @param category category name
     * @return list of Equipment matching the category
     */
    List<Equipment> findByCategoryIgnoreCase(String category);

    /**
     * Find equipment items with available quantity greater than the given
     * threshold. Useful for listing only items that can be borrowed.
     *
     * @param qty threshold quantity (exclusive)
     * @return list of Equipment with availableQuantity &gt; qty
     */
    List<Equipment> findByAvailableQuantityGreaterThan(int qty);
}
