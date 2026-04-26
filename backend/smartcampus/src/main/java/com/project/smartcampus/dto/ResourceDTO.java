package com.project.smartcampus.dto;

import com.project.smartcampus.enums.ResourceCategory;
import com.project.smartcampus.enums.ResourceStatus;
import com.project.smartcampus.enums.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ResourceDTO {

    private Long id;
    @NotBlank(message = "Name cannot be empty")
    private String name;
    @NotNull(message = "Resource type is required")
    private ResourceType type;
    @NotNull(message = "Resource category is required")
    private ResourceCategory category;
    @Min(value = 0, message = "Capacity must be 0 or more")
    private Integer capacity;
    @NotBlank(message = "Location cannot be empty")
    private String location;
    private LocalTime availabilityStart;
    private LocalTime availabilityEnd;
    private String description;
    @NotNull(message = "Resource status is required")
    private ResourceStatus status;
}
