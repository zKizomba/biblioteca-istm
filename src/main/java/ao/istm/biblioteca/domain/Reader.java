package ao.istm.biblioteca.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public class Reader {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final String id;
    private final String name;
    private final String email;

    public Reader(String id, String name, String email) {
        this.id = required(id, "Identificador");
        this.name = required(name, "Nome");
        this.email = required(email, "Correio electrónico");
        if (!EMAIL.matcher(this.email).matches()) throw new IllegalArgumentException("Formato de correio electrónico inválido.");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " é obrigatório.");
        return value.trim();
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    @Override public boolean equals(Object other) { return other instanceof Reader reader && id.equals(reader.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
