package ao.istm.biblioteca.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class DomainRulesTest {
    @Test
    void bookShouldValidateRequiredFieldsAndTrackAvailability() {
        assertThrows(IllegalArgumentException.class, () -> new Book(null, "Título", "Autor"));
        assertThrows(IllegalArgumentException.class, () -> new Book("COD-1", " ", "Autor"));
        assertThrows(IllegalArgumentException.class, () -> new Book("COD-1", "Título", null));
        Book book = new Book(" COD-1 ", " Título ", " Autor ");
        assertEquals("COD-1", book.getCode());
        assertEquals("Título", book.getTitle());
        assertEquals("Autor", book.getAuthor());
        book.markBorrowed();
        assertThrows(IllegalStateException.class, book::markBorrowed);
        book.markReturned();
        assertThrows(IllegalStateException.class, book::markReturned);
    }

    @Test
    void bookEqualityUsesIsbn() {
        Book first = new Book("COD-1", "A", "Autor");
        assertEquals(first, new Book("COD-1", "Outro", "Outra"));
        assertNotEquals(first, new Book("COD-2", "A", "Autor"));
        assertNotEquals(first, null);
        assertEquals(first.hashCode(), new Book("COD-1", "Outro", "Outra").hashCode());
    }

    @Test
    void readerShouldValidateFieldsAndEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Reader(" ", "Nome", "nome@exemplo.ao"));
        assertThrows(IllegalArgumentException.class, () -> new Reader("ID", null, "nome@exemplo.ao"));
        assertThrows(IllegalArgumentException.class, () -> new Reader("ID", "Nome", "email-invalido"));
        Reader reader = new Reader(" ID ", " Nome ", "nome@exemplo.ao");
        assertEquals("ID", reader.getId());
        assertEquals("Nome", reader.getName());
        assertEquals("nome@exemplo.ao", reader.getEmail());
    }

    @Test
    void readerEqualityUsesIdentifier() {
        Reader first = new Reader("ID-1", "A", "a@exemplo.ao");
        assertEquals(first, new Reader("ID-1", "Outro", "b@exemplo.ao"));
        assertNotEquals(first, new Reader("ID-2", "A", "a@exemplo.ao"));
        assertNotEquals(first, "ID-1");
        assertEquals(first.hashCode(), new Reader("ID-1", "Outro", "b@exemplo.ao").hashCode());
    }

    @Test
    void loanShouldValidateReturnsAndTrackState() {
        Loan loan = new Loan("COD-1", "LEITOR", LocalDate.of(2026, 11, 4));
        assertTrue(loan.isActive());
        assertThrows(IllegalArgumentException.class, () -> loan.returnOn(LocalDate.of(2026, 11, 3)));
        loan.returnOn(LocalDate.of(2026, 11, 4));
        assertFalse(loan.isActive());
        assertEquals(LocalDate.of(2026, 11, 4), loan.getReturnedOn());
        assertThrows(IllegalStateException.class, () -> loan.returnOn(LocalDate.of(2026, 11, 5)));
    }
}
