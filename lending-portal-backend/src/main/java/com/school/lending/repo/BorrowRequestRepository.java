package com.school.lending.repo;

import com.school.lending.model.BorrowRequest;
import com.school.lending.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    List<BorrowRequest> findByRequesterId(Long requesterId);

    List<BorrowRequest> findByStatus(BorrowStatus status);

    List<BorrowRequest> findByGearId(Long gearId);

    List<BorrowRequest> findByGearIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long gearId, Collection<BorrowStatus> statuses, LocalDate endDate, LocalDate startDate);
}
