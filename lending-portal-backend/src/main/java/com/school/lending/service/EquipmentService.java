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

    public List<Equipment> listAll(String category, boolean onlyAvailable) {
        if (StringUtils.hasText(category)) {
            return equipmentRepository.findByCategoryIgnoreCase(category);
        }
        if (onlyAvailable) {
            return equipmentRepository.findByAvailableQuantityGreaterThan(0);
        }
        return equipmentRepository.findAll();
    }

    public Optional<Equipment> findOne(Long id) {
        return equipmentRepository.findById(id);
    }

    public Equipment saveThing(Equipment equipment) {
        if (equipment.getAvailableQuantity() > equipment.getTotalQuantity()) {
            equipment.setAvailableQuantity(equipment.getTotalQuantity());
        }
        return equipmentRepository.save(equipment);
    }

    public void deleteById(Long id) {
        equipmentRepository.deleteById(id);
    }

    public void handOut(Long equipmentId, int amount) {
        equipmentRepository.findById(equipmentId).ifPresent(eq -> {
            eq.setAvailableQuantity(Math.max(0, eq.getAvailableQuantity() - amount));
            equipmentRepository.save(eq);
        });
    }

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
