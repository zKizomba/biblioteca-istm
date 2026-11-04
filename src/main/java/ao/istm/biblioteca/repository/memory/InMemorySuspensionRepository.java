package ao.istm.biblioteca.repository.memory;

import ao.istm.biblioteca.domain.Suspension;
import ao.istm.biblioteca.repository.SuspensionRepository;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemorySuspensionRepository implements SuspensionRepository {
    private final ConcurrentMap<String, Suspension> suspensions = new ConcurrentHashMap<>();

    @Override public void save(Suspension suspension) { suspensions.put(suspension.getId(), suspension); }

    @Override public List<Suspension> findAll() {
        return suspensions.values().stream().sorted(Comparator.comparing(Suspension::getStartsOn).reversed()).toList();
    }
}
