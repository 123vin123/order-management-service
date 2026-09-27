package vineet.order_management.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vineet.order_management.model.Account;
import vineet.order_management.service.AccountService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String ACCOUNT_ID = "accountId";
    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Credentials request,
            HttpServletRequest servletRequest) {
        Account account = accountService.register(request.name(), request.email(), request.password());
        establishSession(account, servletRequest, false);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse(account));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Credentials request, HttpServletRequest servletRequest) {
        Account account = accountService.authenticate(request.email(), request.password());
        establishSession(account, servletRequest, Boolean.TRUE.equals(request.rememberMe()));
        return accountResponse(account);
    }

    @GetMapping("/me")
    public Map<String, Object> currentAccount(HttpSession session) {
        Object accountId = session.getAttribute(ACCOUNT_ID);
        if (!(accountId instanceof Long id)) {
            return Map.of("authenticated", false);
        }
        return accountResponse(accountService.getAccount(id));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> accountResponse(Account account) {
        return Map.of("authenticated", true, "id", account.getId(), "name", account.getName(), "email",
                account.getEmail());
    }

    private void establishSession(Account account, HttpServletRequest request, boolean rememberMe) {
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(ACCOUNT_ID, account.getId());
        if (rememberMe) {
            session.setMaxInactiveInterval(14 * 24 * 60 * 60);
        }
    }

    public record Credentials(String name, String email, String password, Boolean rememberMe) {
    }
}