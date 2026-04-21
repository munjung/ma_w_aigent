package com.mahub.core.repository;

import com.mahub.core.domain.InterfaceDefinition;
import com.mahub.core.domain.enums.DefinitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterfaceDefinitionRepository extends JpaRepository<InterfaceDefinition, Long> {
    List<InterfaceDefinition> findByServiceName(String serviceName);
    List<InterfaceDefinition> findByStatus(DefinitionStatus status);
}
