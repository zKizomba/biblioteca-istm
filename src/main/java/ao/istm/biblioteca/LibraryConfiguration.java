package ao.istm.biblioteca;

import ao.istm.biblioteca.service.LibraryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class LibraryConfiguration {
    @Bean
    public LibraryService libraryService(
            @Value("${library.loan.duration-days:14}") int loanDurationDays,
            @Value("${library.penalty.suspension-days:7}") int suspensionDurationDays) {
        return LibraryFactory.createInMemoryLibrary(loanDurationDays, suspensionDurationDays);
    }
}
