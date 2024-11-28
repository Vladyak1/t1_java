package ru.t1.java.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.util.JwtUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AccountRepository accountRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private HttpHeaders headers;
    private static final String TEST_USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/accounts";
        accountRepository.deleteAll();

        // Setup authentication headers
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create UserDetails
        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // Create Authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String token = jwtUtils.generateJwtToken(authentication);
        headers.setBearerAuth(token);
    }

    @Test
    void createAccount_WithValidData_ShouldReturnCreatedAccount() {
        // Arrange
        Account account = new Account();
        account.setClientId(1L);
        account.setBalance(1000.0);

        HttpEntity<Account> request = new HttpEntity<>(account, headers);

        // Act
        ResponseEntity<Account> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                Account.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getClientId()).isEqualTo(account.getClientId());
        assertThat(response.getBody().getBalance()).isEqualTo(account.getBalance());
        assertThat(response.getBody().getAccountId()).isNotNull();
    }

    @Test
    void createAccount_WithoutAuthentication_ShouldReturnUnauthorized() {
        // Arrange
        Account account = new Account();
        account.setClientId(1L);
        account.setBalance(1000.0);

        HttpEntity<Account> request = new HttpEntity<>(account);

        // Act
        ResponseEntity<Account> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                Account.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getAccount_WithExistingId_ShouldReturnAccount() {
        // Arrange
        Account account = new Account();
        account.setClientId(1L);
        account.setBalance(1000.0);
        Account savedAccount = accountRepository.save(account);

        HttpEntity<?> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<Account> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.GET,
                request,
                Account.class,
                savedAccount.getAccountId()
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountId()).isEqualTo(savedAccount.getAccountId());
    }

    @Test
    void getAccount_WithNonExistingId_ShouldReturnNotFound() {
        // Arrange
        HttpEntity<?> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<Account> response = restTemplate.exchange(
                baseUrl + "/{id}",
                HttpMethod.GET,
                request,
                Account.class,
                999L
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAccount_WithoutAuthentication_ShouldReturnUnauthorized() {
        // Act
        ResponseEntity<Account> response = restTemplate.getForEntity(
                baseUrl + "/{id}",
                Account.class,
                1L
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JwtUtils jwtUtils() {
            return new JwtUtils(); // Configure with test properties if needed
        }
    }
}

