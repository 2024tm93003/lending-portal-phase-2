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

    /**
     * Controller managing equipment catalog CRUD operations.
     *
     * <p>Requires authentication for all operations; creation, update and
     * deletion require ADMIN privileges.
     */

    private final EquipmentService equipmentService;
    private final AuthService authService;

    public EquipmentController(EquipmentService equipmentService, AuthService authService) {
        this.equipmentService = equipmentService;
        this.authService = authService;
    }

    @GetMapping
    /**
     * List equipment items. Optionally filter by category or availability.
     *
     * @param token         optional X-Auth-Token header to validate caller
     * @param category      optional category to filter by (case-insensitive)
     * @param availableOnly when true, only items with available quantity > 0 are returned
     * @return list of equipment matching filters
     */
    public List<Equipment> list(@RequestHeader(value = "X-Auth-Token", required = false) String token,
                                @RequestParam(value = "category", required = false) String category,
                                @RequestParam(value = "availableOnly", defaultValue = "false") boolean availableOnly) {
        authService.findUserByToken(token).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "log in first"));
        return equipmentService.listAll(category, availableOnly);
    }

    @PostMapping
    /**
     * Create a new equipment record. Only ADMIN users may create items.
     *
     * @param token X-Auth-Token header of the caller
     * @param input equipment payload containing itemName, category and quantities
     * @return saved Equipment entity
     */
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
    /**
     * Update an existing equipment record. Only ADMIN users may update.
     *
     * @param token X-Auth-Token header of the caller
     * @param id    id of the equipment to update
     * @param input fields to update (partial updates allowed)
     * @return updated Equipment entity
     */
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
    /**
     * Delete an equipment record by id. Only ADMIN users may delete.
     *
     * @param token X-Auth-Token header of the caller
     * @param id    id of the equipment to delete
     */
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
