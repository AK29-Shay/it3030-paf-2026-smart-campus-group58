package com.project.smartcampus.entity;

import com.project.smartcampus.enums.ResourceCategory;
import com.project.smartcampus.enums.ResourceStatus;
import com.project.smartcampus.enums.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;

@Document(collection = "resources")
@Data
public class Resource {

    @Id
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotNull
    private ResourceType type;

    @NotNull
    private ResourceCategory category;

    @Min(value = 0, message = "Capacity must be 0 or more")
    private Integer capacity;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    private LocalTime availabilityStart;

    private LocalTime availabilityEnd;

    private String description;

    @NotNull
    private ResourceStatus status;
}