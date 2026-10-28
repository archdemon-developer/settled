package com.settled.integration;

import com.jayway.jsonpath.JsonPath;
import com.settled.configuration.PostgresContainerConfiguration;
import com.settled.repositories.AccountRepository;
import com.settled.repositories.PostingRepository;
import com.settled.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
@Import(PostgresContainerConfiguration.class)
public class LedgerFlowIntegrationTests {

    @Autowired
    private RestTestClient client;

    @Autowired
    private PostingRepository postingRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void cleanDatabase() {
        postingRepository.deleteAllInBatch();
        transactionRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
    }

    @Test
    public void testCreateAccount_returnsCreated() {
        client.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", "ACC001", "name", "Main Checking", "type", "CHECKING"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("SUCCESS")
                .jsonPath("$.code")
                .isEqualTo(201)
                .jsonPath("$.data.code")
                .isEqualTo("ACC001")
                .jsonPath("$.data.status")
                .isEqualTo("ACTIVE")
                .jsonPath("$.data.id")
                .isNotEmpty();
    }

    @Test
    public void testFullFlow_createPostAndRetrieve() {
        String checking = createAccount("CHK001", "CHECKING");
        String savings = createAccount("SAV001", "SAVINGS");
        String transactionId = createTransaction();

        addPosting(transactionId, checking, "100.50", "DEBIT");
        addPosting(transactionId, savings, "100.50", "CREDIT");

        client.post()
                .uri("/api/v1/transactions/{id}/post", transactionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.data.status")
                .isEqualTo("POSTED");

        client.get()
                .uri("/api/v1/transactions/{id}", transactionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.data.status")
                .isEqualTo("POSTED");

        client.get()
                .uri("/api/v1/transactions?status=POSTED")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.data.length()")
                .isEqualTo(1);
    }

    @Test
    public void testPost_unbalancedTransaction_isRejectedAndStaysDraft() {
        String checking = createAccount("CHK001", "CHECKING");
        String savings = createAccount("SAV001", "SAVINGS");
        String transactionId = createTransaction();
        addPosting(transactionId, checking, "100.00", "DEBIT");
        addPosting(transactionId, savings, "50.00", "CREDIT");

        client.post()
                .uri("/api/v1/transactions/{id}/post", transactionId)
                .exchange()
                .expectStatus()
                .isEqualTo(422)
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("ERROR")
                .jsonPath("$.error.errorCode")
                .isEqualTo("POST_001");

        client.get()
                .uri("/api/v1/transactions/{id}", transactionId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.data.status")
                .isEqualTo("DRAFT");
    }

    @Test
    public void testPost_alreadyPosted_isRejected() {
        String checking = createAccount("CHK001", "CHECKING");
        String savings = createAccount("SAV001", "SAVINGS");
        String transactionId = createBalancedTransaction(checking, savings);
        post(transactionId);

        client.post()
                .uri("/api/v1/transactions/{id}/post", transactionId)
                .exchange()
                .expectStatus()
                .isEqualTo(409)
                .expectBody()
                .jsonPath("$.error.errorCode")
                .isEqualTo("TXN_002");
    }

    @Test
    public void testAddPosting_toPostedTransaction_isRejected() {
        String checking = createAccount("CHK001", "CHECKING");
        String savings = createAccount("SAV001", "SAVINGS");
        String transactionId = createBalancedTransaction(checking, savings);
        post(transactionId);

        client.post()
                .uri("/api/v1/transactions/{id}/postings", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accountId", checking, "amount", new BigDecimal("10.00"), "direction", "DEBIT"))
                .exchange()
                .expectStatus()
                .isEqualTo(409)
                .expectBody()
                .jsonPath("$.error.errorCode")
                .isEqualTo("TXN_002");
    }

    @Test
    public void testCreateAccount_duplicateCode_isRejected() {
        createAccount("CHK001", "CHECKING");

        client.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", "CHK001", "name", "Other", "type", "SAVINGS"))
                .exchange()
                .expectStatus()
                .isEqualTo(409)
                .expectBody()
                .jsonPath("$.error.errorCode")
                .isEqualTo("ACC_003")
                .jsonPath("$.error.field")
                .isEqualTo("code");
    }

    @Test
    public void testCreateTransaction_duplicateReference_isRejected() {
        createTransaction();

        client.post()
                .uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("reference", "TXN001", "postedAt", "2026-10-28T10:00:00.000Z"))
                .exchange()
                .expectStatus()
                .isEqualTo(409)
                .expectBody()
                .jsonPath("$.error.errorCode")
                .isEqualTo("TXN_003");
    }

    @Test
    public void testAddPosting_unknownAccount_isRejected() {
        String transactionId = createTransaction();

        client.post()
                .uri("/api/v1/transactions/{id}/postings", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "accountId",
                        UUID.randomUUID().toString(),
                        "amount",
                        new BigDecimal("10.00"),
                        "direction",
                        "DEBIT"))
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.error.errorCode")
                .isEqualTo("ACC_001");
    }

    @Test
    public void testAddPosting_zeroAmount_failsValidation() {
        String checking = createAccount("CHK001", "CHECKING");
        String transactionId = createTransaction();

        client.post()
                .uri("/api/v1/transactions/{id}/postings", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accountId", checking, "amount", new BigDecimal("0.00"), "direction", "DEBIT"))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody()
                .jsonPath("$.error.errorCode")
                .isEqualTo("VAL_001")
                .jsonPath("$.error.violations[0].field")
                .isEqualTo("amount");
    }

    @Test
    public void testAddPosting_acceptsAmountAsString_andReturnsItAsString() {
        String checking = createAccount("CHK001", "CHECKING");
        String transactionId = createTransaction();

        client.post()
                .uri("/api/v1/transactions/{id}/postings", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accountId", checking, "amount", "100.50", "direction", "DEBIT"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.data.postings[0].amount")
                .isEqualTo("100.50");
    }

    private String createBalancedTransaction(String debitAccountId, String creditAccountId) {
        String transactionId = createTransaction();
        addPosting(transactionId, debitAccountId, "100.00", "DEBIT");
        addPosting(transactionId, creditAccountId, "100.00", "CREDIT");
        return transactionId;
    }

    private void post(String transactionId) {
        client.post()
                .uri("/api/v1/transactions/{id}/post", transactionId)
                .exchange()
                .expectStatus()
                .isOk();
    }

    private String createAccount(String code, String type) {
        String body = client.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "name", code, "type", type))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        return JsonPath.read(body, "$.data.id");
    }

    private String createTransaction() {
        String body = client.post()
                .uri("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("reference", "TXN001", "description", "Test", "postedAt", "2026-10-28T10:00:00.000Z"))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        return JsonPath.read(body, "$.data.id");
    }

    private void addPosting(String transactionId, String accountId, String amount, String direction) {
        client.post()
                .uri("/api/v1/transactions/{id}/postings", transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("accountId", accountId, "amount", new BigDecimal(amount), "direction", direction))
                .exchange()
                .expectStatus()
                .isCreated();
    }
}
