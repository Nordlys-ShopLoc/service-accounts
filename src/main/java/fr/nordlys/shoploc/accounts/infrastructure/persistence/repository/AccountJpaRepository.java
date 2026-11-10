package fr.nordlys.shoploc.accounts.infrastructure.persistence.repository;

import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.AccountEntity;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByEmail(String email);

    List<AccountEntity> findByRole(Role role);

    boolean existsByEmail(String email);
}
