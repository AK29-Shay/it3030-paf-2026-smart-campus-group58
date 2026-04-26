package com.project.smartcampus.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.project.smartcampus.entity.Resource;
import com.project.smartcampus.enums.ResourceType;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends MongoRepository<Resource, Long> {
    List<Resource> findByType(ResourceType type);

    List<Resource> findByTypeIn(List<ResourceType> types);

    List<Resource> findByLocation(String location);

    Optional<Resource> findFirstByNameIgnoreCase(String name);
}