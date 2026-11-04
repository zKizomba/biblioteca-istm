package ao.istm.biblioteca.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class StaffAccountService implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaffAccountService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ConcurrentMap<String, UserDetails> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public StaffAccountService(PasswordEncoder passwordEncoder,
            @Value("${biblioteca.security.admin.username:admin}") String adminName,
            @Value("${biblioteca.security.admin.password:}") String adminPassword,
            @Value("${biblioteca.security.librarian.username:bibliotecario}") String librarianName,
            @Value("${biblioteca.security.librarian.password:}") String librarianPassword) {
        this.passwordEncoder = passwordEncoder;
        normalizeUsername(adminName);
        normalizeUsername(librarianName);
        if (adminName.equals(librarianName)) throw new IllegalArgumentException("As contas iniciais têm de ter nomes diferentes.");
        users.put(adminName, build(adminName, passwordOrGenerate(adminName, "ADMIN", adminPassword), "ADMIN"));
        users.put(librarianName, build(librarianName, passwordOrGenerate(librarianName, "BIBLIOTECARIO", librarianPassword), "BIBLIOTECARIO"));
    }

    @Override public UserDetails loadUserByUsername(String username) {
        UserDetails user = users.get(username);
        if (user == null) throw new UsernameNotFoundException("Conta não encontrada.");
        return user;
    }

    public synchronized void create(String username, String password, String role) {
        String normalized = normalizeUsername(username);
        validateRole(role);
        if (password == null || password.length() < 12) throw new IllegalArgumentException("A palavra-passe deve ter pelo menos 12 caracteres.");
        UserDetails candidate = build(normalized, password, role);
        if (users.putIfAbsent(normalized, candidate) != null) throw new IllegalArgumentException("Já existe uma conta com esse nome de utilizador.");
    }

    public synchronized void changeRole(String username, String role, String actingUsername) {
        validateRole(role);
        UserDetails existing = users.get(username);
        if (existing == null) throw new IllegalArgumentException("Conta não encontrada.");
        if (username.equals(actingUsername) && !role.equals("ADMIN")) throw new IllegalArgumentException("Não pode retirar o seu próprio privilégio de administrador.");
        boolean wasAdmin = existing.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (wasAdmin && !role.equals("ADMIN") && adminCount() == 1) throw new IllegalArgumentException("Tem de existir pelo menos um administrador.");
        users.put(username, User.withUsername(username).password(existing.getPassword()).roles(role).build());
    }

    public List<StaffAccount> list() {
        return users.values().stream().map(user -> new StaffAccount(user.getUsername(),
                        user.getAuthorities().stream().map(a -> a.getAuthority().replace("ROLE_", "")).findFirst().orElse("BIBLIOTECARIO")))
                .sorted(Comparator.comparing(StaffAccount::username)).toList();
    }

    private long adminCount() {
        return users.values().stream().filter(user -> user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))).count();
    }

    private UserDetails build(String username, String rawPassword, String role) {
        return User.withUsername(username).password(passwordEncoder.encode(rawPassword)).roles(role).build();
    }

    private static String passwordOrGenerate(String username, String role, String configured) {
        if (configured != null && !configured.isBlank()) {
            if (configured.length() < 12) throw new IllegalArgumentException("A palavra-passe configurada para " + username + " deve ter pelo menos 12 caracteres.");
            return configured;
        }
        byte[] bytes = new byte[18];
        RANDOM.nextBytes(bytes);
        String generated = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LOGGER.warn("Credencial local temporária para {} '{}': {}. Defina uma variável de ambiente antes de uma instalação institucional.", role, username, generated);
        return generated;
    }

    private static String normalizeUsername(String username) {
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,40}"))
            throw new IllegalArgumentException("O utilizador deve ter 3 a 40 caracteres: letras, números, ponto, hífen ou sublinhado.");
        return username;
    }

    private static void validateRole(String role) {
        if (!"ADMIN".equals(role) && !"BIBLIOTECARIO".equals(role)) throw new IllegalArgumentException("Perfil de acesso inválido.");
    }
}
