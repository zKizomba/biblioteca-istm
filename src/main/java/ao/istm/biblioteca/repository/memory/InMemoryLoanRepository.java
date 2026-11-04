package ao.istm.biblioteca.repository.memory;

import ao.istm.biblioteca.domain.Loan;
import ao.istm.biblioteca.repository.LoanRepository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLoanRepository implements LoanRepository {
    private final Map<String, Loan> loans = new ConcurrentHashMap<>();
    @Override public void save(Loan loan) { loans.put(loan.getId(), loan); }
    @Override public Optional<Loan> findById(String id) { return Optional.ofNullable(loans.get(id)); }
    @Override public List<Loan> findAll() { return List.copyOf(loans.values()); }
}
