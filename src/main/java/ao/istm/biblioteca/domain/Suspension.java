package ao.istm.biblioteca.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Temporary borrowing restriction issued when a late return is completed. */
public final class Suspension {
    private final String id = UUID.randomUUID().toString();
    private final String readerId;
    private final String loanId;
    private final LocalDate startsOn;
    private final LocalDate endsOn;

    public Suspension(String readerId, String loanId, LocalDate startsOn, LocalDate endsOn) {
        this.readerId = Objects.requireNonNull(readerId, "Leitor é obrigatório.");
        this.loanId = Objects.requireNonNull(loanId, "Empréstimo é obrigatório.");
        this.startsOn = Objects.requireNonNull(startsOn, "Data de início é obrigatória.");
        this.endsOn = Objects.requireNonNull(endsOn, "Data de fim é obrigatória.");
        if (endsOn.isBefore(startsOn)) throw new IllegalArgumentException("A suspensão não pode terminar antes de começar.");
    }

    public String getId() { return id; }
    public String getReaderId() { return readerId; }
    public String getLoanId() { return loanId; }
    public LocalDate getStartsOn() { return startsOn; }
    public LocalDate getEndsOn() { return endsOn; }
    public boolean isActiveOn(LocalDate date) { return !date.isBefore(startsOn) && !date.isAfter(endsOn); }
}
