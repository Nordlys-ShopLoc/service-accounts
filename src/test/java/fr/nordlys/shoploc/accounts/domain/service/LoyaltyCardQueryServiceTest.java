
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
}
