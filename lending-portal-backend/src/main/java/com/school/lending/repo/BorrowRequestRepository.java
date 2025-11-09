package com.school.lending.repo;

import com.school.lending.model.BorrowRequest;
import com.school.lending.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Repository for {@link BorrowRequest} entities.
 *
 * <p>Provides helper finder methods used by service layer to query requests
 * by requester, status or equipment and to detect overlapping bookings.
 */
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    /**
     * Find all borrow requests created by a specific requester.
     *
     * @param requesterId id of the requester
     * @return list of BorrowRequest
     */
    List<BorrowRequest> findByRequesterId(Long requesterId);

    /**
     * Find all borrow requests with the given status.
     *
     * @param status desired BorrowStatus
     * @return list of BorrowRequest
     */
    List<BorrowRequest> findByStatus(BorrowStatus status);

    /**
     * Find borrow requests for a specific gear item.
     *
     * @param gearId equipment id
     * @return list of BorrowRequest
     */
    List<BorrowRequest> findByGearId(Long gearId);

    /**
     * Find overlapping borrow requests for the given gear and date range.
     *
     * <p>Used to detect conflicts: the method returns requests for the gear
     * whose dates overlap the supplied start/end and whose status is in the
     * supplied collection.
     *
     * @param gearId   equipment id
     * @param statuses collection of BorrowStatus to include (e.g. APPROVED, ISSUED)
     * @param endDate  end boundary for overlap check
     * @param startDate start boundary for overlap check
     * @return list of overlapping BorrowRequest entries
     */
    List<BorrowRequest> findByGearIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long gearId, Collection<BorrowStatus> statuses, LocalDate endDate, LocalDate startDate);
}
