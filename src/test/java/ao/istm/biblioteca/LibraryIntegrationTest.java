package ao.istm.biblioteca;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class LibraryIntegrationTest {
    @Test
    void shouldUseInMemoryRepositoriesAcrossCompleteLoanLifecycle() {
        var library = LibraryFactory.createInMemoryLibrary();
        library.registerBook("9780000000002", "Arquitectura", "Autor A");
        library.registerReader("L020", "Maria Exemplo", "maria@exemplo.ao");

        var loan = library.borrowBook("9780000000002", "L020", LocalDate.of(2026, 11, 4));
        assertFalse(library.findBook("9780000000002").isAvailable());
        assertEquals(1, library.listLoans().size());

        library.returnBook(loan.getId(), LocalDate.of(2026, 11, 9));
        assertTrue(library.findBook("9780000000002").isAvailable());
        assertFalse(library.listLoans().get(0).isActive());
    }
}
