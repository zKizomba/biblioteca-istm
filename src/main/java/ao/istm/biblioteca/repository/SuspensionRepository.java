package ao.istm.biblioteca.repository;

import ao.istm.biblioteca.domain.Suspension;
import java.util.List;

public interface SuspensionRepository {
    void save(Suspension suspension);
    List<Suspension> findAll();
}
