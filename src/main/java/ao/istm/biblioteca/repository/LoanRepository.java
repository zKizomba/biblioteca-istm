package ao.istm.biblioteca.repository;

import ao.istm.biblioteca.domain.Loan;
import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    void save(Loan loan);
    Optional<Loan> findById(String id);
    List<Loan> findAll();
}
