package fr.nordlys.shoploc.accounts.domain.port;

import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import java.util.List;
import java.util.Optional;

/** Persistence port for accounts. Implemented by an adapter (hexagonal). */
public interface AccountPort {

    Optional<Account> findById(Long id);

    Optional<Account> findByEmail(String email);

    List<Account> findAll();

    List<Account> findByRole(Role role);

    Account save(Account account);

    boolean existsByEmail(String email);

    Optional<String> findPasswordHash(Long accountId);

    void savePasswordHash(Long accountId, String passwordHash);
}
