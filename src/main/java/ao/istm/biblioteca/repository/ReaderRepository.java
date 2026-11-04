package ao.istm.biblioteca.repository;

import ao.istm.biblioteca.domain.Reader;
import java.util.List;
import java.util.Optional;

public interface ReaderRepository {
    void save(Reader reader);
    Optional<Reader> findById(String id);
    Optional<Reader> findByEmail(String email);
    List<Reader> findAll();
}
