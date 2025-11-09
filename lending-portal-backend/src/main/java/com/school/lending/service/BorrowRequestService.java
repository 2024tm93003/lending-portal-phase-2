package com.school.lending.service;

import com.school.lending.model.*;
import com.school.lending.repo.BorrowRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowRequestService {

    /**
     * Service containing business logic around borrow requests.
     *
     * <p>Handles creation, conflict detection, approval/issuance/rejection and
     * return operations, coordinating between the {@link BorrowRequestRepository}
     * and {@link EquipmentService}.
     */

    private static final Collection<BorrowStatus> ACTIVE_STATUSES = Arrays.asList(
            BorrowStatus.APPROVED, BorrowStatus.ISSUED);

    private final BorrowRequestRepository borrowRepo;
    private final EquipmentService equipmentService;

    public BorrowRequestService(BorrowRequestRepository borrowRepo, EquipmentService equipmentService) {
        this.borrowRepo = borrowRepo;
        this.equipmentService = equipmentService;
    }

    /**
     * Create and persist a new borrow request in PENDING status.
     *
     * @param requester user who requests the item
     * @param gear      equipment to borrow
     * @param start     start date (inclusive)
     * @param end       end date (inclusive)
     * @param qty       quantity requested
     * @return saved BorrowRequest
     */
    public BorrowRequest createRequest(UserAccount requester, Equipment gear, LocalDate start, LocalDate end, int qty) {
        BorrowRequest newReq = new BorrowRequest(requester, gear, start, end, qty);
        newReq.setStatus(BorrowStatus.PENDING);
        return borrowRepo.save(newReq);
    }

    /**
     * Find a borrow request by its id.
     *
     * @param id request id
     * @return Optional containing the request if found
     */
    public Optional<BorrowRequest> findById(Long id) {
        return borrowRepo.findById(id);
    }

    /**
     * Return all borrow requests in the system.
     *
     * @return list of BorrowRequest
     */
    public List<BorrowRequest> findAll() {
        return borrowRepo.findAll();
    }

    /**
     * Return borrow requests created by a specific user.
     *
     * @param userId requester user id
     * @return list of BorrowRequest for that user
     */
    public List<BorrowRequest> findMine(Long userId) {
        return borrowRepo.findByRequesterId(userId);
    }

    /**
     * Check whether a requested quantity for a date range would conflict with
     * existing approved/issued requests or exceed total inventory.
     *
     * @param equipmentId equipment id
     * @param start       requested start date
     * @param end         requested end date
     * @param qty         requested quantity
     * @param ignoreId    optional request id to ignore (useful during updates)
     * @return true if a conflict exists or equipment is missing
     */
    public boolean isConflicting(Long equipmentId, LocalDate start, LocalDate end, int qty, Long ignoreId) {
        return equipmentService.findOne(equipmentId)
                .map(eq -> {
                    List<BorrowRequest> overlaps = borrowRepo
                            .findByGearIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                    equipmentId, ACTIVE_STATUSES, end, start);
                    int ongoing = overlaps.stream()
                            .filter(req -> ignoreId == null || !req.getId().equals(ignoreId))
                            .mapToInt(BorrowRequest::getQty)
                            .sum();
                    return ongoing + qty > eq.getTotalQuantity();
                }).orElse(true);
    }

    /**
     * Approve a pending request if it does not conflict with other bookings.
     *
     * @param request request to approve
     * @param note    optional decision note
     * @return Optional containing updated BorrowRequest on success
     */
    @Transactional
    public Optional<BorrowRequest> approveRequest(BorrowRequest request, String note) {
        if (request.getStatus() != BorrowStatus.PENDING) {
            return Optional.empty();
        }
        if (isConflicting(request.getGear().getId(), request.getStartDate(), request.getEndDate(),
                request.getQty(), request.getId())) {
            return Optional.empty();
        }
        request.setStatus(BorrowStatus.APPROVED);
        request.setDecisionDate(OffsetDateTime.now());
        request.setDecisionNote(note);
        return Optional.of(borrowRepo.save(request));
    }

    /**
     * Mark an approved request as ISSUED and decrease equipment availability.
     *
     * @param request request to issue
     * @return Optional containing updated BorrowRequest on success
     */
    @Transactional
    public Optional<BorrowRequest> issue(BorrowRequest request) {
        if (request.getStatus() != BorrowStatus.APPROVED) {
            return Optional.empty();
        }
        request.setStatus(BorrowStatus.ISSUED);
        request.setDecisionDate(OffsetDateTime.now());
        borrowRepo.save(request);
        equipmentService.handOut(request.getGear().getId(), request.getQty());
        return Optional.of(request);
    }

    /**
     * Reject a borrow request with an optional note.
     *
     * @param request request to reject
     * @param note    optional rejection note
     * @return Optional containing updated BorrowRequest on success
     */
    @Transactional
    public Optional<BorrowRequest> reject(BorrowRequest request, String note) {
        if (request.getStatus() == BorrowStatus.REJECTED || request.getStatus() == BorrowStatus.RETURNED) {
            return Optional.empty();
        }
        request.setStatus(BorrowStatus.REJECTED);
        request.setDecisionNote(note);
        request.setDecisionDate(OffsetDateTime.now());
        return Optional.of(borrowRepo.save(request));
    }

    /**
     * Mark an issued request as returned and restore equipment quantity.
     *
     * @param request request to mark returned
     * @return Optional containing updated BorrowRequest on success
     */
    @Transactional
    public Optional<BorrowRequest> markReturned(BorrowRequest request) {
        if (request.getStatus() != BorrowStatus.ISSUED) {
            return Optional.empty();
        }
        request.setStatus(BorrowStatus.RETURNED);
        request.setDecisionDate(OffsetDateTime.now());
        borrowRepo.save(request);
        equipmentService.bringBack(request.getGear().getId(), request.getQty());
        return Optional.of(request);
    }
}
