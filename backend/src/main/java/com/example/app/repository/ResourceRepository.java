package com.example.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.app.entity.Resource;
import com.example.app.util.ResourceType;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByType(ResourceType type);

    List<Resource> findByTypeIn(List<ResourceType> types);

    List<Resource> findByLocation(String location);

    Optional<Resource> findFirstByNameIgnoreCase(String name);
}