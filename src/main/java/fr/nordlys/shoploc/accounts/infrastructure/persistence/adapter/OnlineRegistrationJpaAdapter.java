package fr.nordlys.shoploc.accounts.infrastructure.persistence.adapter;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.port.OnlineRegistrationPort;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.AccountEntity;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import org.springframework.stereotype.Repository;

// enregistre le compte et son hash dans la table users
@Repository
public class OnlineRegistrationJpaAdapter implements OnlineRegistrationPort {

    private final AccountJpaRepository repository;

    public OnlineRegistrationJpaAdapter(AccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Account saveWithPassword(Account account, String passwordHash) {
        AccountEntity entity = new AccountEntity();
        entity.setEmail(account.email());
        entity.setFirstName(account.firstName());
        entity.setLastName(account.lastName());
        entity.setPhone(account.phone());
        entity.setLicensePlate(account.licensePlate());
        entity.setRole(account.role());
        entity.setStatus(account.status());
        entity.setPasswordHash(passwordHash);
        return repository.save(entity).toDomain();
    }
}
