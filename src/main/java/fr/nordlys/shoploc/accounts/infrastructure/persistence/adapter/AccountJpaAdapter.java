package fr.nordlys.shoploc.accounts.infrastructure.persistence.adapter;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.AccountEntity;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountJpaAdapter implements AccountPort {

    private final AccountJpaRepository repository;

    public AccountJpaAdapter(AccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return repository.findById(id).map(AccountEntity::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return repository.findByEmail(email).map(AccountEntity::toDomain);
    }

    @Override
    public Optional<String> findPasswordHash(Long accountId) {
        return repository.findById(accountId).map(AccountEntity::getPasswordHash);
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll().stream().map(AccountEntity::toDomain).toList();
    }

    @Override
    public List<Account> findByRole(Role role) {
        return repository.findByRole(role).stream().map(AccountEntity::toDomain).toList();
    }

    @Override
    public Account save(Account account) {
        // Logique sécurisée : on récupère l'entité pour ne pas écraser le mot de passe
        AccountEntity entity = (account.id() != null)
                ? repository.findById(account.id()).orElseGet(AccountEntity::new)
                : new AccountEntity();

        entity.setEmail(account.email());
        entity.setFirstName(account.firstName());
        entity.setLastName(account.lastName());
        entity.setPhone(account.phone());
        entity.setLicensePlate(account.licensePlate());
        entity.setRole(account.role());
        entity.setStatus(account.status());

        return repository.save(entity).toDomain();
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public void savePasswordHash(Long accountId, String hash) {
        repository.findById(accountId).ifPresent(entity -> {
            entity.setPasswordHash(hash);
            repository.save(entity);
        });
    }
}