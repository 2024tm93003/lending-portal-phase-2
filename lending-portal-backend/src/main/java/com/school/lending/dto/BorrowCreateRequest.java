package com.school.lending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class BorrowCreateRequest {

    @NotNull(message = "equipmentId is required")
    public Long equipmentId;

    @NotNull(message = "startDate is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "startDate must be YYYY-MM-DD")
    public String startDate;

    @NotNull(message = "endDate is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "endDate must be YYYY-MM-DD")
    public String endDate;

    @Min(value = 1, message = "qty must be at least 1")
    public Integer qty;
}
