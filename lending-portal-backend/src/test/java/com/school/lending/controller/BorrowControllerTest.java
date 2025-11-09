package com.school.lending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.lending.dto.BorrowCreateRequest;
import com.school.lending.model.BorrowRequest;
import com.school.lending.model.Equipment;
import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.service.AuthService;
import com.school.lending.service.BorrowRequestService;
import com.school.lending.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowController.class)
class BorrowControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AuthService authService;

    @MockBean
    BorrowRequestService borrowService;

    @MockBean
    EquipmentService equipmentService;

    @Autowired
    ObjectMapper mapper;

    @Test
    void newRequest_success() throws Exception {
        UserAccount u = new UserAccount("stu", "p", UserRole.STUDENT, "Stu");
        u.setId(3L);
        when(authService.findUserByToken("t")).thenReturn(Optional.of(u));

        Equipment eq = new Equipment("cam", "photo", "ok", 5,5);
        eq.setId(20L);
        when(equipmentService.findOne(20L)).thenReturn(Optional.of(eq));
        when(borrowService.isConflicting(eq.getId(), LocalDate.parse("2025-11-10"), LocalDate.parse("2025-11-11"), 1, null)).thenReturn(false);

        BorrowRequest created = new BorrowRequest(u, eq, LocalDate.parse("2025-11-10"), LocalDate.parse("2025-11-11"), 1);
        created.setId(99L);
        when(borrowService.createRequest(any(), any(), any(), any(), any())).thenReturn(created);

        BorrowCreateRequest req = new BorrowCreateRequest();
        req.equipmentId = 20L;
        req.startDate = "2025-11-10";
        req.endDate = "2025-11-11";
        req.qty = 1;

        mvc.perform(post("/api/requests").header("X-Auth-Token", "t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    void newRequest_missingEquipment_returns404() throws Exception {
        UserAccount u = new UserAccount("stu", "p", UserRole.STUDENT, "Stu");
        u.setId(3L);
        when(authService.findUserByToken("t")).thenReturn(Optional.of(u));
        when(equipmentService.findOne(1L)).thenReturn(Optional.empty());

        BorrowCreateRequest req = new BorrowCreateRequest();
        req.equipmentId = 1L;
        req.startDate = "2025-01-01";
        req.endDate = "2025-01-02";
        req.qty = 1;

        mvc.perform(post("/api/requests").header("X-Auth-Token", "t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
