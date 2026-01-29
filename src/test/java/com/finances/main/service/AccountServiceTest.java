package com.finances.main.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.finances.main.model.Account;
import com.finances.main.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountStoresNewAccount() {
        when(accountRepository.existsByNameIgnoreCase("Principal")).thenReturn(false);
        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.createAccount("Principal", "EUR");

        assertThat(account.getName()).isEqualTo("Principal");
        assertThat(account.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void createAccountRejectsDuplicates() {
        when(accountRepository.existsByNameIgnoreCase("Principal")).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount("Principal", "EUR"))
            .isInstanceOf(ResponseStatusException.class);
    }
}
