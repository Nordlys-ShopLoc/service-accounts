package fr.nordlys.shoploc.accounts.infrastructure.persistence.entity;

import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_cards")
@Getter
@Setter
@NoArgsConstructor
public class LoyaltyCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long accountId;

    @Column(name = "qr_code", nullable = false, unique = true, length = 64)
    private String qrCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "issued_in_shop", nullable = false)
    private boolean issuedInShop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @PrePersist
    void onCreate() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
        if (status == null) status = CardStatus.ACTIVE;
    }

    public LoyaltyCard toDomain() {
        return new LoyaltyCard(id, accountId, qrCode, balance,
                issuedInShop, status, issuedAt);
    }

    public static LoyaltyCardEntity fromDomain(LoyaltyCard card) {
        LoyaltyCardEntity entity = new LoyaltyCardEntity();
        entity.id = card.id();
        entity.accountId = card.accountId();
        entity.qrCode = card.qrCode();
        entity.balance = card.balance();
        entity.issuedInShop = card.issuedInShop();
        entity.status = card.status();
        entity.issuedAt = card.issuedAt();
        return entity;
    }
}
