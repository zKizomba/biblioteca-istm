package ao.istm.biblioteca.web;

import ao.istm.biblioteca.domain.Book;
import ao.istm.biblioteca.domain.Loan;
import ao.istm.biblioteca.domain.Suspension;
import ao.istm.biblioteca.security.StaffAccountService;
import ao.istm.biblioteca.service.LibraryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class LibraryWebController {
    private final LibraryService library;
    private final StaffAccountService staff;
    private final int loanDurationDays;
    private final int suspensionDurationDays;

    public LibraryWebController(LibraryService library, StaffAccountService staff,
            @Value("${library.loan.duration-days:14}") int loanDurationDays,
            @Value("${library.penalty.suspension-days:7}") int suspensionDurationDays) {
        this.library = library;
        this.staff = staff;
        this.loanDurationDays = loanDurationDays;
        this.suspensionDurationDays = suspensionDurationDays;
    }

    @ModelAttribute("currentUser")
    public String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "" : authentication.getName();
    }

    @ModelAttribute("currentRole")
    public String currentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return "";
        return authentication.getAuthorities().stream().map(a -> a.getAuthority().replace("ROLE_", "")).findFirst().orElse("");
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/admin/utilizadores")
    public String staffUsers(Model model) {
        model.addAttribute("staffUsers", staff.list());
        return "staff-users";
    }

    @PostMapping("/admin/utilizadores")
    public String createStaffUser(@RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  @RequestParam("role") String role,
                                  RedirectAttributes redirect) {
        try {
            staff.create(username, password, role);
            redirect.addFlashAttribute("success", "Conta de colaborador criada.");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/utilizadores";
    }

    @PostMapping("/admin/utilizadores/{username}/perfil")
    public String changeStaffRole(@PathVariable("username") String username,
                                  @RequestParam("role") String role,
                                  RedirectAttributes redirect) {
        try {
            staff.changeRole(username, role, currentUser());
            redirect.addFlashAttribute("success", "Privilégios actualizados.");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/utilizadores";
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        var overdue = library.listOverdueLoans(today);
        model.addAttribute("bookCount", library.listBooks().size());
        model.addAttribute("readerCount", library.listReaders().size());
        model.addAttribute("activeLoanCount", library.listLoans().stream().filter(Loan::isActive).count());
        model.addAttribute("recentBooks", library.listBooks().stream().limit(5).toList());
        model.addAttribute("overdueCount", overdue.size());
        model.addAttribute("overdueLoans", overdue.stream().map(loan -> loanNotice(loan, today)).toList());
        model.addAttribute("activeSuspensions", library.listSuspensions().stream().filter(item -> item.isActiveOn(today)).toList());
        model.addAttribute("loanDurationDays", loanDurationDays);
        model.addAttribute("suspensionDurationDays", suspensionDurationDays);
        return "dashboard";
    }

    @GetMapping("/livros")
    public String books(@RequestParam(value = "q", defaultValue = "") String query, Model model) {
        String search = query.trim().toLowerCase(java.util.Locale.ROOT);
        model.addAttribute("books", library.listBooks().stream().filter(book -> search.isEmpty()
                || book.getCode().toLowerCase(java.util.Locale.ROOT).contains(search)
                || book.getTitle().toLowerCase(java.util.Locale.ROOT).contains(search)
                || book.getAuthor().toLowerCase(java.util.Locale.ROOT).contains(search)).toList());
        model.addAttribute("searchQuery", query);
        return "books";
    }

    @PostMapping("/livros")
    public String registerBook(@RequestParam("code") String code, @RequestParam("title") String title,
                               @RequestParam("author") String author, RedirectAttributes redirect) {
        try {
            library.registerBook(code, title, author);
            redirect.addFlashAttribute("success", "Exemplar registado com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/livros";
    }

    @GetMapping("/leitores")
    public String readers(@RequestParam(value = "q", defaultValue = "") String query, Model model) {
        String search = query.trim().toLowerCase(java.util.Locale.ROOT);
        model.addAttribute("readers", library.listReaders().stream().filter(reader -> search.isEmpty()
                || reader.getId().toLowerCase(java.util.Locale.ROOT).contains(search)
                || reader.getName().toLowerCase(java.util.Locale.ROOT).contains(search)
                || reader.getEmail().toLowerCase(java.util.Locale.ROOT).contains(search)).toList());
        model.addAttribute("searchQuery", query);
        model.addAttribute("suspendedReaderIds", library.listSuspensions().stream()
                .filter(item -> item.isActiveOn(LocalDate.now())).map(Suspension::getReaderId).distinct().toList());
        return "readers";
    }

    @PostMapping("/leitores")
    public String registerReader(@RequestParam("id") String id, @RequestParam("name") String name,
                                 @RequestParam("email") String email, RedirectAttributes redirect) {
        try {
            library.registerReader(id, name, email);
            redirect.addFlashAttribute("success", "Leitor registado com sucesso.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/leitores";
    }

    @GetMapping("/emprestimos")
    public String loans(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("loans", library.listLoans());
        model.addAttribute("loanNotices", library.listLoans().stream().map(loan -> loanNotice(loan, today)).toList());
        model.addAttribute("availableBooks", library.listBooks().stream().filter(Book::isAvailable).toList());
        model.addAttribute("readers", library.listReaders());
        model.addAttribute("blockedReaderIds", java.util.stream.Stream.concat(
                library.listOverdueLoans(today).stream().map(Loan::getReaderId),
                library.listSuspensions().stream().filter(item -> item.isActiveOn(today)).map(Suspension::getReaderId))
                .distinct().toList());
        model.addAttribute("loanDurationDays", loanDurationDays);
        return "loans";
    }

    @GetMapping("/suspensoes")
    public String suspensions(Model model) {
        LocalDate today = LocalDate.now();
        model.addAttribute("suspensions", library.listSuspensions().stream().map(item -> suspensionNotice(item, today)).toList());
        model.addAttribute("activeSuspensionCount", library.listSuspensions().stream().filter(item -> item.isActiveOn(today)).count());
        model.addAttribute("overdueCount", library.listOverdueLoans(today).size());
        return "suspensions";
    }

    @PostMapping("/emprestimos")
    public String borrowBook(@RequestParam("bookCode") String bookCode, @RequestParam("readerId") String readerId,
                             RedirectAttributes redirect) {
        try {
            library.borrowBook(bookCode, readerId, LocalDate.now());
            redirect.addFlashAttribute("success", "Empréstimo registado. Prazo de devolução: " + loanDurationDays + " dias.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/emprestimos";
    }

    @PostMapping("/emprestimos/{loanId}/devolver")
    public String returnBook(@PathVariable("loanId") String loanId, RedirectAttributes redirect) {
        try {
            Loan returned = library.returnBook(loanId, LocalDate.now());
            if (returned.getReturnedOn().isAfter(returned.getDueOn())) {
                redirect.addFlashAttribute("success", "Devolução registada. Foi aplicada uma suspensão temporária de " + suspensionDurationDays + " dias ao leitor por atraso.");
            } else {
                redirect.addFlashAttribute("success", "Devolução registada dentro do prazo.");
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/emprestimos";
    }

    private LoanNotice loanNotice(Loan loan, LocalDate today) {
        return new LoanNotice(loan, library.findReader(loan.getReaderId()).getName(),
                Math.max(0, ChronoUnit.DAYS.between(loan.getDueOn(), today)));
    }

    private SuspensionNotice suspensionNotice(Suspension suspension, LocalDate today) {
        return new SuspensionNotice(suspension, library.findReader(suspension.getReaderId()).getName(), suspension.isActiveOn(today));
    }

    public record LoanNotice(Loan loan, String readerName, long overdueDays) { }
    public record SuspensionNotice(Suspension suspension, String readerName, boolean active) { }
}
