package com.school.lending.service;

import com.school.lending.model.Equipment;
import com.school.lending.repo.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    /**
     * Service for equipment catalog operations: listing, CRUD and stock updates.
     */
    public List<Equipment> listAll(String category, boolean onlyAvailable) {
        if (StringUtils.hasText(category)) {
            return equipmentRepository.findByCategoryIgnoreCase(category);
        }
        if (onlyAvailable) {
            return equipmentRepository.findByAvailableQuantityGreaterThan(0);
        }
        return equipmentRepository.findAll();
    }

    /**
     * List equipment optionally filtered by category or availability.
     *
     * @param category      optional category filter
     * @param onlyAvailable if true, only return items with availableQuantity &gt; 0
     * @return list of matching Equipment
     */

    /**
     * Find an equipment item by id.
     *
     * @param id equipment id
     * @return Optional containing the Equipment if found
     */
    public Optional<Equipment> findOne(Long id) {
        return equipmentRepository.findById(id);
    }

    /**
     * Persist changes to an equipment record, clamping availableQuantity to
     * not exceed totalQuantity.
     *
     * @param equipment equipment entity to save
     * @return saved Equipment
     */
    public Equipment saveThing(Equipment equipment) {
        if (equipment.getAvailableQuantity() > equipment.getTotalQuantity()) {
            equipment.setAvailableQuantity(equipment.getTotalQuantity());
        }
        return equipmentRepository.save(equipment);
    }

    /**
     * Delete an equipment record by id.
     *
     * @param id equipment id to delete
     */
    public void deleteById(Long id) {
        equipmentRepository.deleteById(id);
    }

    /**
     * Decrease available quantity when items are handed out.
     *
     * @param equipmentId id of equipment
     * @param amount      number of items to hand out
     */
    public void handOut(Long equipmentId, int amount) {
        equipmentRepository.findById(equipmentId).ifPresent(eq -> {
            eq.setAvailableQuantity(Math.max(0, eq.getAvailableQuantity() - amount));
            equipmentRepository.save(eq);
        });
    }

    /**
     * Increase available quantity when items are returned, capped at totalQuantity.
     *
     * @param equipmentId id of equipment
     * @param amount      number of items being returned
     */
    public void bringBack(Long equipmentId, int amount) {
        equipmentRepository.findById(equipmentId).ifPresent(eq -> {
            int updated = eq.getAvailableQuantity() + amount;
            if (updated > eq.getTotalQuantity()) {
                updated = eq.getTotalQuantity();
            }
            eq.setAvailableQuantity(updated);
            equipmentRepository.save(eq);
        });
    }
}
