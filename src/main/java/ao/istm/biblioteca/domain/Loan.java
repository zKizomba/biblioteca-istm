package ao.istm.biblioteca.domain;

import java.time.LocalDate;
import java.util.UUID;

public class Loan {
    private final String id;
    private final String bookCode;
    private final String readerId;
    private final LocalDate borrowedOn;
    private final LocalDate dueOn;
    private LocalDate returnedOn;

    public Loan(String bookCode, String readerId, LocalDate borrowedOn) {
        this(bookCode, readerId, borrowedOn, borrowedOn.plusDays(14));
    }

    public Loan(String bookCode, String readerId, LocalDate borrowedOn, LocalDate dueOn) {
        this.id = UUID.randomUUID().toString();
        this.bookCode = bookCode;
        this.readerId = readerId;
        this.borrowedOn = borrowedOn;
        this.dueOn = dueOn;
    }
    public String getId() { return id; }
    public String getBookCode() { return bookCode; }
    public String getReaderId() { return readerId; }
    public LocalDate getBorrowedOn() { return borrowedOn; }
    public LocalDate getDueOn() { return dueOn; }
    public LocalDate getReturnedOn() { return returnedOn; }
    public boolean isActive() { return returnedOn == null; }
    public boolean isOverdueOn(LocalDate date) { return isActive() && date.isAfter(dueOn); }
    public void returnOn(LocalDate date) {
        if (!isActive()) throw new IllegalStateException("Este empréstimo já foi devolvido.");
        if (date.isBefore(borrowedOn)) throw new IllegalArgumentException("A devolução não pode anteceder o empréstimo.");
        returnedOn = date;
    }
}
