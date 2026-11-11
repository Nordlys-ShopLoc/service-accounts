
package fr.nordlys.shoploc.accounts.domain.service;

import fr.nordlys.shoploc.accounts.domain.model.CardStatus;
import fr.nordlys.shoploc.accounts.domain.model.LoyaltyCard;
import fr.nordlys.shoploc.accounts.domain.port.LoyaltyCardPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoyaltyCardQueryServiceTest {

    private final LoyaltyCardPort cardPort = mock(LoyaltyCardPort.class);
    private final LoyaltyCardQueryService service = new LoyaltyCardQueryService(cardPort);

    // verifie que le client peut recuperer sa carte active
    @Test
    void shouldReturnActiveCard() {
        LoyaltyCard card = new LoyaltyCard(1L, 10L, "QR123", BigDecimal.ZERO, false, CardStatus.ACTIVE, null);

        when(cardPort.findByAccountId(10L)).thenReturn(Optional.of(card));

        LoyaltyCard result = service.getCardByAccountId(10L);

        assertEquals(card, result);
        assertEquals("QR123", result.qrCode());
    }


// verifie qu'une carte bloquee ne peut pas etre presentee
    @Test
    void shouldRejectBlockedCard() {
        LoyaltyCard card = new LoyaltyCard(1L, 10L, "QR123", BigDecimal.ZERO, false, CardStatus.BLOCKED, null);

        when(cardPort.findByAccountId(10L)).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class, () -> service.getCardByAccountId(10L));
    }

 // verifie qu'une erreur est retournee si le client n'a pas de carte
    @Test
    void shouldRejectMissingCard() {
        when(cardPort.findByAccountId(10L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getCardByAccountId(10L));
    }

    // verifie qu'une carte active peut etre retrouvee avec son qr code
    @Test
    void shouldReturnCardByQrCode() {
        LoyaltyCard card = new LoyaltyCard(1L, 10L, "QR123", BigDecimal.ZERO, false, CardStatus.ACTIVE, null);

        when(cardPort.findByQrCode("QR123")).thenReturn(Optional.of(card));

        LoyaltyCard result = service.getCardByQrCode("QR123");

        assertEquals(card, result);
        assertEquals(10L, result.accountId());
    }

    // verifie qu'une carte bloquee est refusee
    @Test
    void shouldRejectBlockedQrCode() {
        LoyaltyCard card = new LoyaltyCard(1L, 10L, "QR123", BigDecimal.ZERO, false, CardStatus.BLOCKED, null);

        when(cardPort.findByQrCode("QR123")).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class, () -> service.getCardByQrCode("QR123"));
    }

    // verifie qu'un qr code inconnu est refuse
    @Test
    void shouldRejectUnknownQrCode() {
        when(cardPort.findByQrCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getCardByQrCode("UNKNOWN"));
    }

}
