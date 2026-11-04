package ao.istm.biblioteca.service;

import ao.istm.biblioteca.domain.Loan;
import ao.istm.biblioteca.domain.Book;
import ao.istm.biblioteca.domain.Reader;
import ao.istm.biblioteca.repository.memory.InMemoryBookRepository;
import ao.istm.biblioteca.repository.memory.InMemoryLoanRepository;
import ao.istm.biblioteca.repository.memory.InMemoryReaderRepository;
import ao.istm.biblioteca.repository.BookRepository;
import ao.istm.biblioteca.repository.LoanRepository;
import ao.istm.biblioteca.repository.ReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {
    private LibraryService library;

    @BeforeEach
    void setUp() {
        library = new LibraryService(new InMemoryBookRepository(), new InMemoryReaderRepository(), new InMemoryLoanRepository());
        library.registerBook("9780000000001", "Engenharia de Software", "Autor Exemplo");
        library.registerReader("L001", "Leitor Exemplo", "leitor@exemplo.ao");
    }

    @Test
    void shouldBorrowAvailableBookAndMarkItUnavailable() {
        Loan loan = library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 4));
        assertTrue(loan.isActive());
        assertFalse(library.findBook("9780000000001").isAvailable());
    }

    @Test
    void shouldRegisterMultiplePhysicalCopiesWithIndividualCodesAndCategory() {
        var copies = library.registerBookStock("ISTM-ENG-01", "Fundamentos de Engenharia", "Autor Exemplo", "Engenharia", 3);

        assertEquals(3, copies.size());
        assertEquals(List.of("ISTM-ENG-01-001", "ISTM-ENG-01-002", "ISTM-ENG-01-003"),
                copies.stream().map(Book::getCode).toList());
        assertTrue(copies.stream().allMatch(book -> book.getCategory().equals("Engenharia")));
        assertEquals(4, library.listBooks().size());

        library.borrowBook("ISTM-ENG-01-001", "L001", LocalDate.of(2026, 11, 4));
        assertFalse(library.findBook("ISTM-ENG-01-001").isAvailable());
        assertTrue(library.findBook("ISTM-ENG-01-002").isAvailable());
    }

    @Test
    void shouldRejectStockRegistrationWhenAnyGeneratedCopyCodeAlreadyExists() {
        library.registerBook("ISTM-ENG-02-002", "Existing copy", "Author");

        assertThrows(IllegalArgumentException.class, () ->
                library.registerBookStock("ISTM-ENG-02", "New title", "Author", "Engineering", 3));
        assertTrue(library.listBooks().stream().noneMatch(book -> book.getCode().equals("ISTM-ENG-02-001")));
        assertTrue(library.listBooks().stream().noneMatch(book -> book.getCode().equals("ISTM-ENG-02-003")));
    }

    @Test
    void shouldRejectBorrowingBookThatIsAlreadyOnLoan() {
        library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 4));
        assertThrows(IllegalStateException.class,
                () -> library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 4)));
    }

    @Test
    void shouldReturnLoanAndRestoreBookAvailability() {
        Loan loan = library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 4));
        library.returnBook(loan.getId(), LocalDate.of(2026, 11, 8));
        assertFalse(loan.isActive());
        assertEquals(LocalDate.of(2026, 11, 8), loan.getReturnedOn());
        assertTrue(library.findBook("9780000000001").isAvailable());
        assertTrue(library.listSuspensions().isEmpty());
    }

    @Test
    void shouldApplyTemporarySuspensionAfterLateReturnAndBlockBorrowing() {
        LocalDate borrowedOn = LocalDate.of(2026, 11, 4);
        Loan loan = library.borrowBook("9780000000001", "L001", borrowedOn);
        assertEquals(LocalDate.of(2026, 11, 18), loan.getDueOn());
        assertEquals(1, library.listOverdueLoans(LocalDate.of(2026, 11, 19)).size());

        library.returnBook(loan.getId(), LocalDate.of(2026, 11, 20));

        assertEquals(1, library.listSuspensions().size());
        assertTrue(library.isReaderSuspended("L001", LocalDate.of(2026, 11, 20)));
        assertFalse(library.isReaderSuspended("L001", LocalDate.of(2026, 11, 27)));
        assertThrows(IllegalStateException.class,
                () -> library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 25)));
        assertDoesNotThrow(() -> library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 28)));
    }

    @Test
    void shouldBlockNewLoanWhileReaderHasAnOverdueActiveLoan() {
        library.registerBook("9780000000002", "Outro exemplar", "Outro Autor");
        library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 4));
        assertThrows(IllegalStateException.class,
                () -> library.borrowBook("9780000000002", "L001", LocalDate.of(2026, 11, 19)));
    }

    @Test
    void shouldRejectInvalidReaderEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> library.registerReader("L002", "Outro Leitor", "email-invalido"));
    }

    @Test
    void shouldRejectReturningUnknownLoan() {
        assertThrows(IllegalArgumentException.class,
                () -> library.returnBook("inexistente", LocalDate.of(2026, 11, 8)));
    }

    @Test
    void shouldRejectDuplicateBookIsbn() {
        assertThrows(IllegalArgumentException.class,
                () -> library.registerBook("9780000000001", "Outro título", "Outro autor"));
    }

    @Test
    void shouldRejectDuplicateReaderIdAndEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> library.registerReader("L001", "Outro Nome", "outro@exemplo.ao"));
        assertThrows(IllegalArgumentException.class,
                () -> library.registerReader("L002", "Outro Nome", "LEITOR@EXEMPLO.AO"));
    }

    @Test
    void shouldRejectMissingBookOrReader() {
        assertThrows(IllegalArgumentException.class, () -> library.findBook("desconhecido"));
        assertThrows(IllegalArgumentException.class, () -> library.findReader("desconhecido"));
        assertThrows(IllegalArgumentException.class,
                () -> library.borrowBook("9780000000001", "desconhecido", LocalDate.now()));
        assertThrows(IllegalArgumentException.class,
                () -> library.borrowBook("desconhecido", "L001", LocalDate.now()));
    }

    @Test
    void shouldRejectNullDatesAndReturnBeforeBorrowDate() {
        assertThrows(IllegalArgumentException.class, () -> library.borrowBook("9780000000001", "L001", null));
        assertThrows(IllegalArgumentException.class, () -> library.returnBook("inexistente", null));
        Loan loan = library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 4));
        assertThrows(IllegalArgumentException.class,
                () -> library.returnBook(loan.getId(), LocalDate.of(2026, 11, 3)));
        assertTrue(loan.isActive());
        assertFalse(library.findBook("9780000000001").isAvailable());
    }

    @Test
    void shouldRejectSecondReturn() {
        Loan loan = library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 4));
        library.returnBook(loan.getId(), LocalDate.of(2026, 11, 5));
        assertThrows(IllegalStateException.class,
                () -> library.returnBook(loan.getId(), LocalDate.of(2026, 11, 6)));
    }

    @Test
    void shouldListRegisteredRecordsAndLoans() {
        Loan loan = library.borrowBook("9780000000001", "L001", LocalDate.of(2026, 11, 4));
        assertEquals(1, library.listBooks().size());
        assertEquals(1, library.listReaders().size());
        assertEquals(1, library.listLoans().size());
        assertEquals(loan.getId(), library.listLoans().get(0).getId());
    }

    @Test
    void shouldRejectAnActiveDuplicateEvenIfBookAvailabilityIsInconsistent() {
        BookRepository books = new InMemoryBookRepository();
        ReaderRepository readers = new InMemoryReaderRepository();
        LoanRepository loans = new InMemoryLoanRepository();
        books.save(new Book("B-1", "Livro 1", "Autor"));
        books.save(new Book("B-2", "Livro 2", "Autor"));
        readers.save(new Reader("R-1", "Leitor 1", "r1@exemplo.ao"));
        readers.save(new Reader("R-2", "Leitor 2", "r2@exemplo.ao"));
        LocalDate date = LocalDate.of(2026, 11, 4);
        loans.save(new Loan("B-1", "R-1", date));
        loans.save(new Loan("B-2", "R-1", date));
        Loan closedLoan = new Loan("B-1", "R-2", date);
        closedLoan.returnOn(date);
        loans.save(closedLoan);
        LibraryService service = new LibraryService(books, readers, loans);

        assertThrows(IllegalStateException.class, () -> service.borrowBook("B-1", "R-1", date));
        Loan validLoan = service.borrowBook("B-1", "R-2", date);
        assertTrue(validLoan.isActive());
    }
}
