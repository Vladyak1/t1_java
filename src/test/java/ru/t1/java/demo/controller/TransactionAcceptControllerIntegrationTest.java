package ru.t1.java.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.dto.ClientStatusRequest;
import ru.t1.java.demo.repository.AccountRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionAcceptControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AccountRepository accountRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/transaction-accept/checkClientStatus";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void whenAccountIsOpen_ShouldReturnActive() {
        // Arrange
        Long clientId = 1L;
        Account account = new Account();
        account.setClientId(clientId);
        account.setStatus(Account.AccountStatus.OPEN);

        when(accountRepository.findByClientId(clientId)).thenReturn(account);

        ClientStatusRequest request = new ClientStatusRequest();
        request.setClientId(clientId);

        HttpEntity<ClientStatusRequest> httpEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("ACTIVE");
    }

    @Test
    void whenAccountIsBlocked_ShouldReturnBlocked() {
        // Arrange
        Long clientId = 1L;
        Account account = new Account();
        account.setClientId(clientId);
        account.setStatus(Account.AccountStatus.BLOCKED);

        when(accountRepository.findByClientId(clientId)).thenReturn(account);

        ClientStatusRequest request = new ClientStatusRequest();
        request.setClientId(clientId);

        HttpEntity<ClientStatusRequest> httpEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("BLOCKED");
    }

    @Test
    void whenAccountIsArrested_ShouldReturnBlocked() {
        // Arrange
        Long clientId = 1L;
        Account account = new Account();
        account.setClientId(clientId);
        account.setStatus(Account.AccountStatus.ARRESTED);

        when(accountRepository.findByClientId(clientId)).thenReturn(account);

        ClientStatusRequest request = new ClientStatusRequest();
        request.setClientId(clientId);

        HttpEntity<ClientStatusRequest> httpEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("BLOCKED");
    }

    @Test
    void whenClientNotFound_ShouldReturnNotFound() {
        // Arrange
        Long clientId = 999L;
        when(accountRepository.findByClientId(clientId)).thenReturn(null);

        ClientStatusRequest request = new ClientStatusRequest();
        request.setClientId(clientId);

        HttpEntity<ClientStatusRequest> httpEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("CLIENT_NOT_FOUND");
    }

    @Test
    void whenAccountStatusIsUnknown_ShouldReturnError() {
        // Arrange
        Long clientId = 1L;
        Account account = new Account();
        account.setClientId(clientId);
        account.setStatus(null);

        when(accountRepository.findByClientId(clientId)).thenReturn(account);

        ClientStatusRequest request = new ClientStatusRequest();
        request.setClientId(clientId);

        HttpEntity<ClientStatusRequest> httpEntity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("UNKNOWN_STATUS");
    }
}

