package fr.nordlys.shoploc.accounts.infrastructure.persistence.adapter;

import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.AccountEntity;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adapter: binds the domain port to JPA persistence. */
@Repository
public class AccountJpaAdapter implements AccountPort {

    private final AccountJpaRepository jpaRepository;

    public AccountJpaAdapter(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findById(id).map(AccountEntity::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(AccountEntity::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return jpaRepository.findAll().stream().map(AccountEntity::toDomain).toList();
    }

    @Override
    public List<Account> findByRole(Role role) {
        return jpaRepository.findByRole(role).stream().map(AccountEntity::toDomain).toList();
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = account.id() == null
                ? new AccountEntity()
                : jpaRepository.findById(account.id()).orElseGet(AccountEntity::new);
        entity.setEmail(account.email());
        entity.setFirstName(account.firstName());
        entity.setLastName(account.lastName());
        entity.setPhone(account.phone());
        entity.setLicensePlate(account.licensePlate());
        entity.setRole(account.role());
        entity.setStatus(account.status());
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<String> findPasswordHash(Long accountId) {
        return jpaRepository.findById(accountId).map(AccountEntity::getPasswordHash);
    }

    @Override
    public void savePasswordHash(Long accountId, String passwordHash) {
        AccountEntity entity = jpaRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown account: " + accountId));
        entity.setPasswordHash(passwordHash);
        jpaRepository.save(entity);
    }
}
