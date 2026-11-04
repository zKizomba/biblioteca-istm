package ao.istm.biblioteca.repository;

import ao.istm.biblioteca.domain.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    void save(Book book);
    Optional<Book> findByCode(String code);
    List<Book> findAll();
}
