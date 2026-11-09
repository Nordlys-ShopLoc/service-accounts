package fr.nordlys.accounts.repository.jpa;

import fr.nordlys.accounts.domain.Account;
import fr.nordlys.accounts.domain.AccountPort;
import fr.nordlys.accounts.domain.Role;
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
}
