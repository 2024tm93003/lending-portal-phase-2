package com.school.lending.service;

import com.school.lending.model.Equipment;
import com.school.lending.repo.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    EquipmentRepository repo;

    @InjectMocks
    EquipmentService service;

    Equipment eq;

    @BeforeEach
    void setup() {
        eq = new Equipment("cam","photo","ok",5,6);
        eq.setId(10L);
    }

    @Test
    void saveThing_clampsAvailableAboveTotal() {
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        Equipment out = service.saveThing(eq);
        assertTrue(out.getAvailableQuantity() <= out.getTotalQuantity());
    }

    @Test
    void handOut_decreases_and_saves() {
        when(repo.findById(eq.getId())).thenReturn(Optional.of(eq));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.handOut(eq.getId(), 2);
        assertEquals(Math.max(0, 6 - 2), eq.getAvailableQuantity());
        verify(repo).save(eq);
    }

    @Test
    void bringBack_increases_and_caps() {
        eq.setAvailableQuantity(4);
        when(repo.findById(eq.getId())).thenReturn(Optional.of(eq));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.bringBack(eq.getId(), 10);
        assertEquals(eq.getTotalQuantity(), eq.getAvailableQuantity());
        verify(repo).save(eq);
    }
}
