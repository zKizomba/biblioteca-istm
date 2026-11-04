package ao.istm.biblioteca.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class StaffAccountServiceTest {
    private StaffAccountService accounts() {
        return new StaffAccountService(new BCryptPasswordEncoder(),
                "admin.teste", "Admin-Seguro-2026", "bibliotecario.teste", "Bibliotecario-2026");
    }

    @Test
    void shouldHashPasswordsAndNeverExposeThemInTheAccountList() {
        StaffAccountService service = accounts();
        var admin = service.loadUserByUsername("admin.teste");
        assertTrue(admin.getPassword().startsWith("$2a$"));
        assertEquals(2, service.list().size());
        assertFalse(service.list().toString().contains("Admin-Seguro-2026"));
    }

    @Test
    void shouldValidateNewAccountsAndProtectLastAdministrator() {
        StaffAccountService service = accounts();
        assertThrows(IllegalArgumentException.class,
                () -> service.create("novo.utilizador", "curta", "BIBLIOTECARIO"));
        assertThrows(IllegalArgumentException.class,
                () -> service.create("novo.utilizador", "Password-Longa-2026", "LEITOR"));
        assertThrows(IllegalArgumentException.class,
                () -> service.changeRole("admin.teste", "BIBLIOTECARIO", "admin.teste"));
        assertThrows(IllegalArgumentException.class,
                () -> service.changeRole("admin.teste", "BIBLIOTECARIO", "outro.admin"));
    }
}
