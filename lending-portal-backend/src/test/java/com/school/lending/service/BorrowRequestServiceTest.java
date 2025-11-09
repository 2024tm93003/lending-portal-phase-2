package com.school.lending.service;

import com.school.lending.model.BorrowRequest;
import com.school.lending.model.BorrowStatus;
import com.school.lending.model.Equipment;
import com.school.lending.model.UserAccount;
import com.school.lending.repo.BorrowRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowRequestServiceTest {

    @Mock
    BorrowRequestRepository borrowRepo;

    @Mock
    EquipmentService equipmentService;

    @InjectMocks
    BorrowRequestService service;

    UserAccount u;
    Equipment eq;

    @BeforeEach
    void setup() {
        u = new UserAccount("u","p", null, "U");
        u.setId(2L);
        eq = new Equipment("x","c","n",5,5);
        eq.setId(3L);
    }

    @Test
    void createRequest_saves() {
        BorrowRequest saved = new BorrowRequest(u, eq, LocalDate.now(), LocalDate.now().plusDays(1), 1);
        saved.setId(12L);
        when(borrowRepo.save(any())).thenReturn(saved);

        BorrowRequest out = service.createRequest(u, eq, LocalDate.now(), LocalDate.now().plusDays(1), 1);
        assertNotNull(out);
        assertEquals(12L, out.getId());
    }

    @Test
    void isConflicting_whenEquipmentMissing_returnsTrue() {
        when(equipmentService.findOne(99L)).thenReturn(Optional.empty());
        boolean c = service.isConflicting(99L, LocalDate.now(), LocalDate.now().plusDays(1), 1, null);
        assertTrue(c);
    }

    @Test
    void isConflicting_withOverlappingBookings() {
        when(equipmentService.findOne(eq.getId())).thenReturn(Optional.of(eq));
        BorrowRequest existing = new BorrowRequest(u, eq, LocalDate.now(), LocalDate.now().plusDays(1), 4);
        when(borrowRepo.findByGearIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any(), any(), any()))
                .thenReturn(List.of(existing));

        boolean c = service.isConflicting(eq.getId(), LocalDate.now(), LocalDate.now().plusDays(1), 2, null);
        assertTrue(c);
    }
}
