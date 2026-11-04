package ao.istm.biblioteca.web;

import ao.istm.biblioteca.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "bibliotecario", roles = "BIBLIOTECARIO")
class LibraryWebControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private LibraryService library;

    @Test
    void shouldRequireLoginAndShowLoginPage() throws Exception {
        mvc.perform(get("/").with(anonymous())).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(content().string(containsString("Iniciar sessão")));
    }

    @Test
    void librarianCannotOpenAdminUserManagement() throws Exception {
        mvc.perform(get("/admin/utilizadores")).andExpect(status().isForbidden());
        mvc.perform(post("/livros").param("code", "CSRF-1").param("title", "Livro").param("author", "Autor"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRenderAllMainPages() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("Bem-vindo à Biblioteca ISTM")));
        mvc.perform(get("/livros")).andExpect(status().isOk()).andExpect(content().string(containsString("Gestão de exemplares")));
        mvc.perform(get("/leitores")).andExpect(status().isOk()).andExpect(content().string(containsString("Gestão de leitores")));
        mvc.perform(get("/emprestimos")).andExpect(status().isOk()).andExpect(content().string(containsString("Empréstimos e devoluções")));
        mvc.perform(get("/suspensoes")).andExpect(status().isOk()).andExpect(content().string(containsString("Atrasos e suspensões")));
    }

    @Test
    void shouldRegisterSearchAndRejectDuplicateBook() throws Exception {
        String code = "WEB-B-" + UUID.randomUUID();
        mvc.perform(post("/livros").with(csrf()).param("code", code).param("title", "Livro Web").param("author", "Autora Web"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/livros"));
        mvc.perform(get("/livros").param("q", "Livro Web")).andExpect(status().isOk()).andExpect(content().string(containsString("Livro Web")));
        mvc.perform(post("/livros").with(csrf()).param("code", code).param("title", "Duplicado").param("author", "Autora Web"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/livros").with(csrf()).param("code", " ").param("title", "Inválido").param("author", "Autor"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void shouldRegisterSearchAndRejectInvalidReader() throws Exception {
        String id = "WEB-R-" + UUID.randomUUID();
        mvc.perform(post("/leitores").with(csrf()).param("id", id).param("name", "Leitor Web").param("email", "web-" + id + "@exemplo.ao"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/leitores"));
        mvc.perform(get("/leitores").param("q", "Leitor Web")).andExpect(status().isOk()).andExpect(content().string(containsString("Leitor Web")));
        mvc.perform(post("/leitores").with(csrf()).param("id", id).param("name", "Duplicado").param("email", "outro@exemplo.ao"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/leitores").with(csrf()).param("id", "WEB-INVALID").param("name", "Inválido").param("email", "email-invalido"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void shouldCompleteLoanLifecycle() throws Exception {
        String bookCode = "WEB-L-" + UUID.randomUUID();
        String readerId = "WEB-R-" + UUID.randomUUID();
        library.registerBook(bookCode, "Exemplar para Web", "Autor Web");
        library.registerReader(readerId, "Leitora Web", "web-" + readerId + "@exemplo.ao");
        mvc.perform(post("/emprestimos").with(csrf()).param("bookCode", bookCode).param("readerId", readerId))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/emprestimos"));
        var loan = library.listLoans().stream().filter(item -> item.getBookCode().equals(bookCode) && item.isActive()).findFirst().orElseThrow();
        mvc.perform(get("/emprestimos")).andExpect(status().isOk()).andExpect(content().string(containsString(bookCode)));
        mvc.perform(post("/emprestimos").with(csrf()).param("bookCode", bookCode).param("readerId", readerId)).andExpect(status().is3xxRedirection());
        mvc.perform(post("/emprestimos").with(csrf()).param("bookCode", "nao-existe").param("readerId", readerId)).andExpect(status().is3xxRedirection());
        mvc.perform(post("/emprestimos/nao-existe/devolver").with(csrf())).andExpect(status().is3xxRedirection());
        mvc.perform(post("/emprestimos/{id}/devolver", loan.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/emprestimos"));
        mvc.perform(get("/emprestimos")).andExpect(status().isOk()).andExpect(content().string(containsString("Devolvido")));
    }

    @Test
    void dashboardShouldShowOverdueReaderAndNumberOfDays() throws Exception {
        String bookCode = "WEB-OVERDUE-" + UUID.randomUUID();
        String readerId = "WEB-LATE-" + UUID.randomUUID();
        library.registerBook(bookCode, "Livro atrasado", "Autor Web");
        library.registerReader(readerId, "Leitor em atraso", readerId + "@exemplo.ao");
        library.borrowBook(bookCode, readerId, java.time.LocalDate.now().minusDays(18));

        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Empréstimos vencidos")))
                .andExpect(content().string(containsString("Leitor em atraso")));
    }

    @Test
    @WithMockUser(username = "admin-teste", roles = "ADMIN")
    void administratorCanCreateStaffAccount() throws Exception {
        String username = "staff-" + UUID.randomUUID().toString().substring(0, 8);
        mvc.perform(post("/admin/utilizadores").with(csrf()).param("username", username)
                        .param("password", "Strong-Passphrase-2026").param("role", "BIBLIOTECARIO"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/admin/utilizadores"));
        mvc.perform(get("/admin/utilizadores")).andExpect(status().isOk()).andExpect(content().string(containsString(username)));
    }
}
