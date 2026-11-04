package ao.istm.biblioteca.service;

import ao.istm.biblioteca.domain.Book;
import ao.istm.biblioteca.domain.Loan;
import ao.istm.biblioteca.domain.Reader;
import ao.istm.biblioteca.domain.Suspension;
import ao.istm.biblioteca.repository.BookRepository;
import ao.istm.biblioteca.repository.LoanRepository;
import ao.istm.biblioteca.repository.ReaderRepository;
import ao.istm.biblioteca.repository.SuspensionRepository;
import ao.istm.biblioteca.repository.memory.InMemorySuspensionRepository;
import java.time.LocalDate;
import java.util.List;

public class LibraryService {
    private final BookRepository books;
    private final ReaderRepository readers;
    private final LoanRepository loans;
    private final SuspensionRepository suspensions;
    private final int loanDurationDays;
    private final int suspensionDurationDays;

    public LibraryService(BookRepository books, ReaderRepository readers, LoanRepository loans) {
        this(books, readers, loans, new InMemorySuspensionRepository(), 14, 7);
    }

    public LibraryService(BookRepository books, ReaderRepository readers, LoanRepository loans,
                          SuspensionRepository suspensions, int loanDurationDays, int suspensionDurationDays) {
        if (loanDurationDays < 1 || suspensionDurationDays < 1) throw new IllegalArgumentException("Os períodos têm de ser pelo menos um dia.");
        this.books = books;
        this.readers = readers;
        this.loans = loans;
        this.suspensions = suspensions;
        this.loanDurationDays = loanDurationDays;
        this.suspensionDurationDays = suspensionDurationDays;
    }
    public Book registerBook(String code, String title, String author) {
        Book book = new Book(code, title, author); books.save(book); return book;
    }
    public synchronized List<Book> registerBookStock(String codePrefix, String title, String author,
                                                       String category, int quantity) {
        if (quantity < 1 || quantity > 1000) throw new IllegalArgumentException("A quantidade deve estar entre 1 e 1000 exemplares.");
        if (codePrefix == null || codePrefix.isBlank()) throw new IllegalArgumentException("O prefixo dos cÃ³digos Ã© obrigatÃ³rio.");
        String prefix = codePrefix.trim();
        List<Book> copies = java.util.stream.IntStream.rangeClosed(1, quantity)
                .mapToObj(number -> new Book(prefix + "-%03d".formatted(number), title, author, category)).toList();
        for (Book copy : copies) {
            if (books.findByCode(copy.getCode()).isPresent()) throw new IllegalArgumentException("JÃ¡ existe um exemplar com o cÃ³digo " + copy.getCode() + ".");
        }
        copies.forEach(books::save);
        return copies;
    }
    public Reader registerReader(String id, String name, String email) {
        Reader reader = new Reader(id, name, email); readers.save(reader); return reader;
    }
    public List<Book> listBooks() { return books.findAll(); }
    public List<Reader> listReaders() { return readers.findAll(); }
    public Book findBook(String code) { return books.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Livro não encontrado.")); }
    public Reader findReader(String id) { return readers.findById(id).orElseThrow(() -> new IllegalArgumentException("Leitor não encontrado.")); }
    public synchronized Loan borrowBook(String bookCode, String readerId, LocalDate date) {
        if (date == null) throw new IllegalArgumentException("A data do empréstimo é obrigatória.");
        Book book = findBook(bookCode); findReader(readerId);
        if (isReaderSuspended(readerId, date)) throw new IllegalStateException("O leitor está suspenso e não pode fazer empréstimos nesta data.");
        if (loans.findAll().stream().anyMatch(loan -> loan.getReaderId().equals(readerId) && loan.isOverdueOn(date)))
            throw new IllegalStateException("O leitor tem um empréstimo em atraso por devolver.");
        if (!book.isAvailable()) throw new IllegalStateException("O livro não está disponível para empréstimo.");
        boolean duplicate = loans.findAll().stream().anyMatch(loan -> loan.isActive() && loan.getBookCode().equals(bookCode) && loan.getReaderId().equals(readerId));
        if (duplicate) throw new IllegalStateException("O leitor já tem este livro emprestado.");
        Loan loan = new Loan(bookCode, readerId, date, date.plusDays(loanDurationDays)); book.markBorrowed(); loans.save(loan); return loan;
    }
    public synchronized Loan returnBook(String loanId, LocalDate date) {
        if (date == null) throw new IllegalArgumentException("A data da devolução é obrigatória.");
        Loan loan = loans.findById(loanId).orElseThrow(() -> new IllegalArgumentException("Empréstimo não encontrado."));
        Book book = findBook(loan.getBookCode());
        loan.returnOn(date);
        book.markReturned();
        loans.save(loan);
        if (date.isAfter(loan.getDueOn())) {
            LocalDate startsOn = date;
            LocalDate latestEnd = suspensions.findAll().stream()
                    .filter(item -> item.getReaderId().equals(loan.getReaderId()))
                    .map(Suspension::getEndsOn).max(LocalDate::compareTo).orElse(null);
            if (latestEnd != null && !latestEnd.isBefore(startsOn)) startsOn = latestEnd.plusDays(1);
            suspensions.save(new Suspension(loan.getReaderId(), loan.getId(), startsOn,
                    startsOn.plusDays(suspensionDurationDays - 1L)));
        }
        return loan;
    }
    public List<Loan> listLoans() { return loans.findAll(); }
    public List<Loan> listOverdueLoans(LocalDate date) {
        return loans.findAll().stream().filter(loan -> loan.isOverdueOn(date)).toList();
    }
    public List<Suspension> listSuspensions() { return suspensions.findAll(); }
    public boolean isReaderSuspended(String readerId, LocalDate date) {
        return suspensions.findAll().stream().anyMatch(item -> item.getReaderId().equals(readerId) && item.isActiveOn(date));
    }
    public int getLoanDurationDays() { return loanDurationDays; }
    public int getSuspensionDurationDays() { return suspensionDurationDays; }
}
