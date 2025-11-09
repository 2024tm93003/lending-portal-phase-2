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

    private static final Collection<BorrowStatus> ACTIVE_STATUSES = Arrays.asList(
            BorrowStatus.APPROVED, BorrowStatus.ISSUED);

    private final BorrowRequestRepository borrowRepo;
    private final EquipmentService equipmentService;

    public BorrowRequestService(BorrowRequestRepository borrowRepo, EquipmentService equipmentService) {
        this.borrowRepo = borrowRepo;
        this.equipmentService = equipmentService;
    }

    public BorrowRequest createRequest(UserAccount requester, Equipment gear, LocalDate start, LocalDate end, int qty) {
        BorrowRequest newReq = new BorrowRequest(requester, gear, start, end, qty);
        newReq.setStatus(BorrowStatus.PENDING);
        return borrowRepo.save(newReq);
    }

    public Optional<BorrowRequest> findById(Long id) {
        return borrowRepo.findById(id);
    }

    public List<BorrowRequest> findAll() {
        return borrowRepo.findAll();
    }

    public List<BorrowRequest> findMine(Long userId) {
        return borrowRepo.findByRequesterId(userId);
    }

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
