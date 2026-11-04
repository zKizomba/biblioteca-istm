package ao.istm.biblioteca.domain;

import java.util.Objects;

public class Book {
    private final String code;
    private final String title;
    private final String author;
    private boolean available = true;

    public Book(String code, String title, String author) {
        this.code = required(code, "Código do exemplar");
        this.title = required(title, "Título");
        this.author = required(author, "Autor");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " é obrigatório.");
        return value.trim();
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }
    public void markBorrowed() {
        if (!available) throw new IllegalStateException("O livro já está emprestado.");
        available = false;
    }
    public void markReturned() {
        if (available) throw new IllegalStateException("O livro já se encontra disponível.");
        available = true;
    }

    @Override public boolean equals(Object other) {
        return other instanceof Book book && code.equals(book.code);
    }
    @Override public int hashCode() { return Objects.hash(code); }
}
