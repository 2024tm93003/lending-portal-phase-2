package com.school.lending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class EquipmentInput {

    @NotBlank(message = "itemName is required")
    public String itemName;

    public String category;

    public String conditionNote;

    @Min(value = 0, message = "totalQuantity must be >= 0")
    public Integer totalQuantity;

    @Min(value = 0, message = "availableQuantity must be >= 0")
    public Integer availableQuantity;
}
