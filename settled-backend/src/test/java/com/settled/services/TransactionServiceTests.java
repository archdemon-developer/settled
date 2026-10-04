package com.settled.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.settled.enums.PostingDirection;
import com.settled.enums.TransactionStatus;
import com.settled.exceptions.DuplicateResourceException;
import com.settled.exceptions.InvalidPostingException;
import com.settled.exceptions.ResourceNotFoundException;
import com.settled.models.entities.Account;
import com.settled.models.entities.Posting;
import com.settled.models.entities.Transaction;
import com.settled.models.requests.CreateTransactionRequest;
import com.settled.models.responses.TransactionResponse;
import com.settled.repositories.PostingRepository;
import com.settled.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PostingRepository postingRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    public void testCreateTransaction_duplicateReference() {
        Transaction transaction = Transaction.builder()
                .reference("TXN123")
                .description("Test Transaction")
                .postedAt(Instant.now())
                .build();

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .reference("TXN123")
                .description("Test Transaction")
                .postedAt(Instant.now())
                .build();

        when(transactionRepository.findByReference("TXN123")).thenReturn(Optional.of(transaction));

        assertThrows(DuplicateResourceException.class, () -> transactionService.create(request));
        verify(transactionRepository, times(1)).findByReference("TXN123");
    }

    @Test
    public void testCreateTransaction_savesSucessfully() {
        Instant now = Instant.now();
        UUID txnId = UUID.randomUUID();

        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .reference("TXN123")
                .description("Test Transaction")
                .postedAt(now)
                .build();

        when(transactionRepository.findByReference("TXN123")).thenReturn(Optional.empty());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        Transaction savedTransaction = Transaction.builder()
                .id(txnId)
                .reference("TXN123")
                .description("Test Transaction")
                .postedAt(now)
                .status(TransactionStatus.DRAFT)
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.save(captor.capture())).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.create(request);

        Transaction captured = captor.getValue();
        assertNotNull(response);
        assertEquals("TXN123", captured.getReference());
        assertEquals("Test Transaction", captured.getDescription());
        assertEquals(now, captured.getPostedAt());
        assertEquals(TransactionStatus.DRAFT, captured.getStatus());

        verify(transactionRepository, times(1)).findByReference("TXN123");
        verify(transactionRepository, times(1)).save(captor.getValue());
    }

    @Test
    public void testGetById_success() {
        UUID txnId = UUID.randomUUID();
        Transaction transaction = Transaction.builder()
                .id(txnId)
                .reference("TXN123")
                .description("Test Transaction")
                .postedAt(Instant.now())
                .status(TransactionStatus.DRAFT)
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findById(txnId)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.getById(txnId);

        assertNotNull(response);
        assertEquals("TXN123", response.getReference());
        assertEquals("Test Transaction", response.getDescription());
        assertEquals(TransactionStatus.DRAFT, response.getStatus());

        verify(transactionRepository, times(1)).findById(txnId);
    }

    @Test
    public void testGetById_NotFound() {
        UUID txnId = UUID.randomUUID();
        when(transactionRepository.findById(txnId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getById(txnId));
        verify(transactionRepository, times(1)).findById(txnId);
    }

    @Test
    public void testGetByReference_Success() {
        String reference = "TXN123";
        UUID txnId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .reference(reference)
                .description("Test Transaction")
                .status(TransactionStatus.DRAFT)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findByReference(reference)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.getByReference(reference);

        assertNotNull(response);
        assertEquals(reference, response.getReference());
        assertEquals(TransactionStatus.DRAFT, response.getStatus());

        verify(transactionRepository, times(1)).findByReference(reference);
    }

    @Test
    public void testGetByReference_NotFound() {
        String reference = "NONEXISTENT";
        when(transactionRepository.findByReference(reference)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getByReference(reference));

        verify(transactionRepository, times(1)).findByReference(reference);
    }

    @Test
    public void testListByStatus_WithStatus() {
        UUID txnId1 = UUID.randomUUID();
        UUID txnId2 = UUID.randomUUID();

        Transaction txn1 = Transaction.builder()
                .id(txnId1)
                .reference("TXN001")
                .status(TransactionStatus.DRAFT)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        Transaction txn2 = Transaction.builder()
                .id(txnId2)
                .reference("TXN002")
                .status(TransactionStatus.DRAFT)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findByStatus(TransactionStatus.DRAFT)).thenReturn(List.of(txn1, txn2));

        List<TransactionResponse> responses = transactionService.listByStatus(TransactionStatus.DRAFT);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("TXN001", responses.get(0).getReference());
        assertEquals("TXN002", responses.get(1).getReference());

        verify(transactionRepository, times(1)).findByStatus(TransactionStatus.DRAFT);
    }

    @Test
    public void testListByStatus_WithoutStatus() {
        UUID txnId1 = UUID.randomUUID();
        UUID txnId2 = UUID.randomUUID();

        Transaction txn1 = Transaction.builder()
                .id(txnId1)
                .reference("TXN001")
                .status(TransactionStatus.DRAFT)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        Transaction txn2 = Transaction.builder()
                .id(txnId2)
                .reference("TXN002")
                .status(TransactionStatus.POSTED)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findAll()).thenReturn(List.of(txn1, txn2));

        List<TransactionResponse> responses = transactionService.listByStatus(null);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(TransactionStatus.DRAFT, responses.get(0).getStatus());
        assertEquals(TransactionStatus.POSTED, responses.get(1).getStatus());

        verify(transactionRepository, times(1)).findAll();
        verify(transactionRepository, never()).findByStatus(any());
    }

    @Test
    public void testMarkAsPosted_Success() {
        UUID txnId = UUID.randomUUID();
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.DRAFT)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        Posting posting1 = Posting.builder()
                .id(UUID.randomUUID())
                .transaction(transaction)
                .account(Account.builder().id(accountId1).build())
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .createdAt(Instant.now())
                .build();

        Posting posting2 = Posting.builder()
                .id(UUID.randomUUID())
                .transaction(transaction)
                .account(Account.builder().id(accountId2).build())
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.CREDIT)
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findById(txnId)).thenReturn(Optional.of(transaction));
        when(postingRepository.findByTransactionId(txnId)).thenReturn(List.of(posting1, posting2));

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        Transaction postedTransaction = Transaction.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.POSTED)
                .postedAt(transaction.getPostedAt())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        when(transactionRepository.save(captor.capture())).thenReturn(postedTransaction);

        TransactionResponse response = transactionService.markAsPosted(txnId);

        Transaction captured = captor.getValue();
        assertEquals(TransactionStatus.POSTED, captured.getStatus());

        assertNotNull(response);
        assertEquals(TransactionStatus.POSTED, response.getStatus());

        verify(transactionRepository, times(1)).findById(txnId);
        verify(postingRepository, times(1)).findByTransactionId(txnId);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testMarkAsPosted_TransactionNotFound() {
        UUID txnId = UUID.randomUUID();
        when(transactionRepository.findById(txnId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.markAsPosted(txnId));

        verify(transactionRepository, times(1)).findById(txnId);
        verify(postingRepository, never()).findByTransactionId(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    public void testMarkAsPosted_NotInDraftState() {
        UUID txnId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.POSTED)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findById(txnId)).thenReturn(Optional.of(transaction));

        assertThrows(InvalidPostingException.class, () -> transactionService.markAsPosted(txnId));

        verify(transactionRepository, times(1)).findById(txnId);
        verify(postingRepository, never()).findByTransactionId(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    public void testMarkAsPosted_ImbalancedTransaction() {
        UUID txnId = UUID.randomUUID();
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.DRAFT)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        Posting posting1 = Posting.builder()
                .id(UUID.randomUUID())
                .transaction(transaction)
                .account(Account.builder().id(accountId1).build())
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .createdAt(Instant.now())
                .build();

        Posting posting2 = Posting.builder()
                .id(UUID.randomUUID())
                .transaction(transaction)
                .account(Account.builder().id(accountId2).build())
                .amount(new BigDecimal("50.00"))
                .direction(PostingDirection.CREDIT)
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findById(txnId)).thenReturn(Optional.of(transaction));
        when(postingRepository.findByTransactionId(txnId)).thenReturn(List.of(posting1, posting2));

        assertThrows(InvalidPostingException.class, () -> transactionService.markAsPosted(txnId));

        verify(transactionRepository, times(1)).findById(txnId);
        verify(postingRepository, times(1)).findByTransactionId(txnId);
        verify(transactionRepository, never()).save(any());
    }
}
