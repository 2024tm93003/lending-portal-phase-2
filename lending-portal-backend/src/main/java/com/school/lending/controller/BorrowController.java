package com.school.lending.controller;

import com.school.lending.dto.BorrowCreateRequest;
import com.school.lending.dto.DecisionInput;
import com.school.lending.model.BorrowRequest;
import com.school.lending.model.Equipment;
import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.service.AuthService;
import com.school.lending.service.BorrowRequestService;
import com.school.lending.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class BorrowController {

    private final AuthService authService;
    private final BorrowRequestService borrowService;
    private final EquipmentService equipmentService;

    public BorrowController(AuthService authService,
                            BorrowRequestService borrowService,
                            EquipmentService equipmentService) {
        this.authService = authService;
        this.borrowService = borrowService;
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public List<BorrowRequest> browse(@RequestHeader("X-Auth-Token") String token,
                                      @RequestParam(value = "mine", defaultValue = "false") boolean mineOnly) {
        UserAccount account = authService.findUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token"));
        if (mineOnly || account.getRole() == UserRole.STUDENT) {
            return borrowService.findMine(account.getId());
        }
        if (account.getRole() == UserRole.STAFF || account.getRole() == UserRole.ADMIN) {
            return borrowService.findAll();
        }
        return borrowService.findMine(account.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowRequest newRequest(@RequestHeader("X-Auth-Token") String token,
                                    @RequestBody BorrowCreateRequest payload) {
        UserAccount account = authService.findUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token"));
        if (payload == null || payload.equipmentId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing gear info");
        }
        Equipment equipment = equipmentService.findOne(payload.equipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "equipment missing"));
        LocalDate start = LocalDate.parse(payload.startDate);
        LocalDate end = LocalDate.parse(payload.endDate);
        int qty = payload.qty == null ? 1 : payload.qty;
        if (qty <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
        }
        if (borrowService.isConflicting(equipment.getId(), start, end, qty, null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "schedule clash / out of stock");
        }
        return borrowService.createRequest(account, equipment, start, end, qty);
    }

    @PostMapping("/{id}/approve")
    public BorrowRequest approve(@RequestHeader("X-Auth-Token") String token,
                                 @PathVariable Long id,
                                 @RequestBody(required = false) DecisionInput note) {
        UserAccount approver = authService.findUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token"));
        if (approver.getRole() == UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "students can't approve");
        }
        BorrowRequest request = borrowService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "request missing"));
        return borrowService.approveRequest(request, note == null ? null : note.message)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "cannot approve"));
    }

    @PostMapping("/{id}/issue")
    public BorrowRequest issue(@RequestHeader("X-Auth-Token") String token,
                               @PathVariable Long id) {
        UserAccount issuer = authService.findUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token"));
        if (issuer.getRole() == UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "students can't issue");
        }
        BorrowRequest request = borrowService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "request missing"));
        return borrowService.issue(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "cannot issue now"));
    }

    @PostMapping("/{id}/reject")
    public BorrowRequest reject(@RequestHeader("X-Auth-Token") String token,
                                @PathVariable Long id,
                                @RequestBody(required = false) DecisionInput note) {
        UserAccount decider = authService.findUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token"));
        if (decider.getRole() == UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "students can't reject");
        }
        BorrowRequest request = borrowService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "request missing"));
        return borrowService.reject(request, note == null ? null : note.message)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "cannot reject now"));
    }

    @PostMapping("/{id}/return")
    public BorrowRequest markReturned(@RequestHeader("X-Auth-Token") String token,
                                      @PathVariable Long id) {
        UserAccount decider = authService.findUserByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token"));
        if (decider.getRole() == UserRole.STUDENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "students can't close tickets");
        }
        BorrowRequest request = borrowService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "request missing"));
        return borrowService.markReturned(request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "cannot return now"));
    }
}
