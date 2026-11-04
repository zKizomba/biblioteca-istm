package ao.istm.biblioteca.repository.memory;

import ao.istm.biblioteca.domain.Book;
import ao.istm.biblioteca.repository.BookRepository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryBookRepository implements BookRepository {
    private final Map<String, Book> books = new ConcurrentHashMap<>();
    @Override public void save(Book book) {
        if (books.putIfAbsent(book.getCode(), book) != null) throw new IllegalArgumentException("Já existe um exemplar com este código.");
    }
    @Override public Optional<Book> findByCode(String code) { return Optional.ofNullable(books.get(code)); }
    @Override public List<Book> findAll() { return books.values().stream().sorted(Comparator.comparing(Book::getTitle)).toList(); }
}
