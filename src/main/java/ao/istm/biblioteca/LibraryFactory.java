package ao.istm.biblioteca;

import ao.istm.biblioteca.repository.memory.InMemoryBookRepository;
import ao.istm.biblioteca.repository.memory.InMemoryLoanRepository;
import ao.istm.biblioteca.repository.memory.InMemoryReaderRepository;
import ao.istm.biblioteca.repository.memory.InMemorySuspensionRepository;
import ao.istm.biblioteca.service.LibraryService;

/** Factory that assembles the application with its in-memory persistence adapters. */
public final class LibraryFactory {
    private LibraryFactory() { }

    public static LibraryService createInMemoryLibrary() {
        return createInMemoryLibrary(14, 7);
    }

    public static LibraryService createInMemoryLibrary(int loanDurationDays, int suspensionDurationDays) {
        return new LibraryService(new InMemoryBookRepository(), new InMemoryReaderRepository(),
                new InMemoryLoanRepository(), new InMemorySuspensionRepository(), loanDurationDays, suspensionDurationDays);
    }
}
