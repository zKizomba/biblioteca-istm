package ao.istm.biblioteca.repository.memory;

import ao.istm.biblioteca.domain.Reader;
import ao.istm.biblioteca.repository.ReaderRepository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryReaderRepository implements ReaderRepository {
    private final Map<String, Reader> readers = new ConcurrentHashMap<>();
    @Override public synchronized void save(Reader reader) {
        if (readers.containsKey(reader.getId()) || findByEmail(reader.getEmail()).isPresent())
            throw new IllegalArgumentException("Já existe um leitor com este identificador ou correio electrónico.");
        readers.put(reader.getId(), reader);
    }
    @Override public Optional<Reader> findById(String id) { return Optional.ofNullable(readers.get(id)); }
    @Override public Optional<Reader> findByEmail(String email) {
        return readers.values().stream().filter(reader -> reader.getEmail().equalsIgnoreCase(email)).findFirst();
    }
    @Override public List<Reader> findAll() { return readers.values().stream().sorted(Comparator.comparing(Reader::getName)).toList(); }
}
