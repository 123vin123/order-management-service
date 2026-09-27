package vineet.order_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vineet.order_management.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmailIgnoreCase(String email);
}