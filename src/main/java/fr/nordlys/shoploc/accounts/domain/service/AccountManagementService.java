package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.exception.AccountNotFoundException;
import fr.nordlys.shoploc.accounts.domain.exception.OperationNotAllowedException;
import fr.nordlys.shoploc.accounts.domain.model.Account;
import fr.nordlys.shoploc.accounts.domain.model.Role;
import fr.nordlys.shoploc.accounts.domain.model.UserStatus;
import fr.nordlys.shoploc.accounts.domain.port.AccountPort;

import java.util.List;

public class AccountManagementService {

    private final AccountPort accountPort;

    public AccountManagementService(AccountPort accountPort) {
        this.accountPort = accountPort;
    }

    public List<Account> listAccounts(Long adminId, Role roleFilter) {
        checkIsActiveAdmin(adminId);
        if (roleFilter == null) {
            return accountPort.findAll();
        }
        return accountPort.findByRole(roleFilter);
    }

    public Account suspendAccount(Long adminId, Long targetAccountId) {
        checkIsActiveAdmin(adminId);

        if (adminId.equals(targetAccountId)) {
            throw new OperationNotAllowedException("An administrator cannot suspend their own account");
        }

        Account target = getModifiableAccount(targetAccountId);
        if (target.status() != UserStatus.ACTIVE) {
            throw new OperationNotAllowedException("Only an active account can be suspended");
        }

        return accountPort.save(target.withStatus(UserStatus.SUSPENDED));
    }

    public Account reactivateAccount(Long adminId, Long targetAccountId) {
        checkIsActiveAdmin(adminId);

        Account target = getModifiableAccount(targetAccountId);
        if (target.status() != UserStatus.SUSPENDED) {
            throw new OperationNotAllowedException("Only a suspended account can be reactivated");
        }

        return accountPort.save(target.withStatus(UserStatus.ACTIVE));
    }

    public Account changeRole(Long adminId, Long targetAccountId, Role newRole) {
        checkIsActiveAdmin(adminId);

        if (newRole == null) {
            throw new IllegalArgumentException("New role is required");
        }

        if (adminId.equals(targetAccountId)) {
            throw new OperationNotAllowedException("An administrator cannot change their own role");
        }

        Account target = getModifiableAccount(targetAccountId);
        return accountPort.save(target.withRole(newRole));
    }

    private void checkIsActiveAdmin(Long adminId) {
        if (adminId == null) {
            throw new OperationNotAllowedException("Only an active administrator can manage accounts");
        }
        Account admin = accountPort.findById(adminId).orElse(null);
        if (admin == null || !admin.isAdmin() || !admin.isActive()) {
            throw new OperationNotAllowedException("Only an active administrator can manage accounts");
        }
    }

    private Account getModifiableAccount(Long targetAccountId) {
        Account target = accountPort.findById(targetAccountId)
                .orElseThrow(() -> new AccountNotFoundException(targetAccountId));

        if (target.isDeleted()) {
            throw new OperationNotAllowedException("A deleted account cannot be modified");
        }
        return target;
    }
}