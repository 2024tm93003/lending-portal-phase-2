package com.school.lending.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.lending.dto.EquipmentInput;
import com.school.lending.model.Equipment;
import com.school.lending.model.UserAccount;
import com.school.lending.model.UserRole;
import com.school.lending.service.AuthService;
import com.school.lending.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EquipmentController.class)
class EquipmentControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EquipmentService equipmentService;

    @MockBean
    AuthService authService;

    @Autowired
    ObjectMapper mapper;

    @Test
    void list_requiresAuthentication() throws Exception {
        UserAccount u = new UserAccount("sam", "p", UserRole.STUDENT, "Sam");
        u.setId(10L);
        when(authService.findUserByToken("t")).thenReturn(Optional.of(u));
        when(equipmentService.listAll(null, false)).thenReturn(List.of(new Equipment("cam", "photo", "ok", 2,2)));

        mvc.perform(get("/api/equipment").header("X-Auth-Token", "t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].itemName").value("cam"));
    }

    @Test
    void create_adminAllowed() throws Exception {
        UserAccount admin = new UserAccount("admin", "p", UserRole.ADMIN, "Admin");
        admin.setId(5L);
        when(authService.findUserByToken("t1")).thenReturn(Optional.of(admin));

        EquipmentInput in = new EquipmentInput();
        in.itemName = "proj";
        in.category = "av";
        in.conditionNote = "new";
        in.totalQuantity = 3;

        Equipment saved = new Equipment("proj", "av", "new", 3, 3);
        saved.setId(11L);
        when(equipmentService.saveThing(any())).thenReturn(saved);

        mvc.perform(post("/api/equipment").header("X-Auth-Token", "t1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.itemName").value("proj"));
    }

    @Test
    void create_forbiddenForStudent() throws Exception {
        UserAccount student = new UserAccount("stu", "p", UserRole.STUDENT, "Stu");
        student.setId(6L);
        when(authService.findUserByToken("t2")).thenReturn(Optional.of(student));

        EquipmentInput in = new EquipmentInput();
        in.itemName = "x";

        mvc.perform(post("/api/equipment").header("X-Auth-Token", "t2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isForbidden());
    }
}
