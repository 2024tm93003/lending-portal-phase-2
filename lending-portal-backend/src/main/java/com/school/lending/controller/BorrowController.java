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

        /**
         * Controller for creating and managing borrow requests.
         *
         * <p>Endpoints allow students to create requests and authorized staff/admin
         * users to approve, issue, reject or mark requests as returned.
         */

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
        /**
         * Browse borrow requests. Students only see their own requests; staff/admin
         * can see all requests. The optional "mine" query parameter forces
         * filtering to the requesting user's records.
         *
         * @param token    X-Auth-Token header
         * @param mineOnly whether to return only the caller's requests
         * @return list of matching BorrowRequest
         */
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
        /**
         * Create a new borrow request for a specific equipment item and date range.
         * Validates quantity and checks for scheduling/stock conflicts.
         *
         * @param token   X-Auth-Token header of the requester
         * @param payload borrow creation details (equipmentId, dates, qty)
         * @return created BorrowRequest
         */
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
        /**
         * Approve a pending borrow request. Only staff/admin may approve.
         *
         * @param token approver's X-Auth-Token
         * @param id    id of the borrow request to approve
         * @param note  optional decision note
         * @return the updated BorrowRequest if approval succeeded
         */
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
        /**
         * Issue (hand out) an approved borrow request. Only staff/admin may issue.
         *
         * @param token issuer's X-Auth-Token
         * @param id    id of the borrow request to issue
         * @return the updated BorrowRequest if issue succeeded
         */
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
        /**
         * Reject a borrow request. Only staff/admin may reject.
         *
         * @param token approver's X-Auth-Token
         * @param id    id of the borrow request to reject
         * @param note  optional reason for rejection
         * @return the updated BorrowRequest if rejection succeeded
         */
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
        /**
         * Mark an issued borrow request as returned and restore equipment
         * availability. Only staff/admin may perform this action.
         *
         * @param token X-Auth-Token of the caller
         * @param id    id of the borrow request to mark returned
         * @return the updated BorrowRequest if operation succeeded
         */
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
