package fr.nordlys.shoploc.accounts.infrastructure.persistence.repository;

import fr.nordlys.shoploc.accounts.infrastructure.persistence.entity.LoyaltyCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyCardJpaRepository extends JpaRepository<LoyaltyCardEntity, Long> {

    Optional<LoyaltyCardEntity> findByAccountId(Long accountId);

    Optional<LoyaltyCardEntity> findByQrCode(String qrCode);
}
