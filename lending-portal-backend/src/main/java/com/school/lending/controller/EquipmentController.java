package com.school.lending.controller;

import com.school.lending.dto.EquipmentInput;
import com.school.lending.model.Equipment;
import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.service.AuthService;
import com.school.lending.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final AuthService authService;

    public EquipmentController(EquipmentService equipmentService, AuthService authService) {
        this.equipmentService = equipmentService;
        this.authService = authService;
    }

    @GetMapping
    public List<Equipment> list(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                @RequestParam(value = "category", required = false) String category,
                                @RequestParam(value = "availableOnly", defaultValue = "false") boolean availableOnly) {
        authService.findUserByToken(token).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "log in first"));
        return equipmentService.listAll(category, availableOnly);
    }

    @PostMapping
    public Equipment create(@RequestHeader("X-Auth-Token") String token,
                            @RequestBody EquipmentInput input) {
        UserAccount user = authService.findUserByToken(token).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "log in first"));
        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not allowed");
        }
        Equipment eq = new Equipment();
        eq.setItemName(input.itemName);
        eq.setCategory(input.category);
        eq.setConditionNote(input.conditionNote);
        eq.setTotalQuantity(input.totalQuantity == null ? 0 : input.totalQuantity);
        eq.setAvailableQuantity(input.availableQuantity == null ? eq.getTotalQuantity() : input.availableQuantity);
        return equipmentService.saveThing(eq);
    }

    @PutMapping("/{id}")
    public Equipment update(@RequestHeader("X-Auth-Token") String token,
                            @PathVariable Long id,
                            @RequestBody EquipmentInput input) {
        UserAccount user = authService.findUserByToken(token).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "log in first"));
        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not allowed");
        }
        Equipment eq = equipmentService.findOne(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "missing"));
        if (input.itemName != null) {
            eq.setItemName(input.itemName);
        }
        if (input.category != null) {
            eq.setCategory(input.category);
        }
        if (input.conditionNote != null) {
            eq.setConditionNote(input.conditionNote);
        }
        if (input.totalQuantity != null) {
            eq.setTotalQuantity(input.totalQuantity);
        }
        if (input.availableQuantity != null) {
            eq.setAvailableQuantity(input.availableQuantity);
        }
        return equipmentService.saveThing(eq);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-Auth-Token") String token,
                       @PathVariable Long id) {
        UserAccount user = authService.findUserByToken(token).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "log in first"));
        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not allowed");
        }
        equipmentService.deleteById(id);
    }
}
