package com.mahub.core.repository;

import com.mahub.core.domain.InterfaceDefinition;
import com.mahub.core.domain.enums.DefinitionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InterfaceDefinitionRepositoryTest {

    @Autowired
    private InterfaceDefinitionRepository repository;

    private InterfaceDefinition buildDefinition(String serviceName, DefinitionStatus status) {
        return InterfaceDefinition.builder()
                .serviceName(serviceName)
                .fileName(serviceName + ".md")
                .content("# Service: " + serviceName)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_andFindById_returnsDefinition() {
        InterfaceDefinition saved = repository.save(buildDefinition("user-service", DefinitionStatus.VALID));

        Optional<InterfaceDefinition> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getServiceName()).isEqualTo("user-service");
    }

    @Test
    void findByServiceName_returnsMatchingDefinitions() {
        repository.save(buildDefinition("user-service", DefinitionStatus.VALID));
        repository.save(buildDefinition("order-service", DefinitionStatus.VALID));

        List<InterfaceDefinition> result = repository.findByServiceName("user-service");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServiceName()).isEqualTo("user-service");
    }

    @Test
    void findByStatus_returnsOnlyMatchingStatus() {
        repository.save(buildDefinition("user-service", DefinitionStatus.VALID));
        repository.save(buildDefinition("bad-service", DefinitionStatus.INVALID));

        List<InterfaceDefinition> validDefs = repository.findByStatus(DefinitionStatus.VALID);
        List<InterfaceDefinition> invalidDefs = repository.findByStatus(DefinitionStatus.INVALID);

        assertThat(validDefs).hasSize(1);
        assertThat(invalidDefs).hasSize(1);
    }

    @Test
    void findAll_returnsAllSavedDefinitions() {
        repository.save(buildDefinition("svc-a", DefinitionStatus.VALID));
        repository.save(buildDefinition("svc-b", DefinitionStatus.VALID));
        repository.save(buildDefinition("svc-c", DefinitionStatus.INVALID));

        List<InterfaceDefinition> all = repository.findAll();

        assertThat(all).hasSize(3);
    }

    @Test
    void delete_removesDefinition() {
        InterfaceDefinition saved = repository.save(buildDefinition("temp-service", DefinitionStatus.VALID));
        Long id = saved.getId();

        repository.deleteById(id);

        assertThat(repository.findById(id)).isEmpty();
    }
}
