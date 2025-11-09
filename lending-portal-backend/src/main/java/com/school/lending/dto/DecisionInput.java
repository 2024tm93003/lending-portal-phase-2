package com.school.lending.dto;

import jakarta.validation.constraints.Size;

public class DecisionInput {

    @Size(max = 1024, message = "message too long")
    public String message;
}
