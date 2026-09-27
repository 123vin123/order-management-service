package vineet.order_management.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vineet.order_management.model.Account;
import vineet.order_management.repository.AccountRepository;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Account register(String name, String email, String password) {
        String normalizedName = name == null ? "" : name.trim();
        String normalizedEmail = normalizeEmail(email);
        if (normalizedName.isBlank() || normalizedName.length() > 120) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter your name (up to 120 characters).");
        }
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be between 8 and 72 characters.");
        }
        if (accounts.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        return accounts.saveAndFlush(new Account(normalizedName, normalizedEmail, passwordEncoder.encode(password)));
    }

    @Transactional(readOnly = true)
    public Account authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        Account account = accounts.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."));
        if (password == null || !passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
        }
        return account;
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accounts.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}