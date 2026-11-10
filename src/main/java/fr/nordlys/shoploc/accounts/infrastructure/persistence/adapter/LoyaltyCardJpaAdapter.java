package fr.nordlys.shoploc.accounts.infrastructure.persistence.adapter;

import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.LoyaltyCardEntity;
import fr.nordlys.shoploc.accounts.infrastructure.persistence.repository.LoyaltyCardJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Adapter: binds the domain port to JPA persistence. */
@Repository
public class LoyaltyCardJpaAdapter implements LoyaltyCardPort {

    private final LoyaltyCardJpaRepository jpaRepository;

    public LoyaltyCardJpaAdapter(LoyaltyCardJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<LoyaltyCard> findByAccountId(Long accountId) {
        return jpaRepository.findByAccountId(accountId).map(LoyaltyCardEntity::toDomain);
    }

    @Override
    public Optional<LoyaltyCard> findByQrCode(String qrCode) {
        return jpaRepository.findByQrCode(qrCode).map(LoyaltyCardEntity::toDomain);
    }

    @Override
    public LoyaltyCard save(LoyaltyCard card) {
        return jpaRepository.save(LoyaltyCardEntity.fromDomain(card)).toDomain();
    }
}
