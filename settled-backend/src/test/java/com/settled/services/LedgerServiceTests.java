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
import com.settled.exceptions.InvalidPostingException;
import com.settled.exceptions.ResourceNotFoundException;
import com.settled.models.entities.Account;
import com.settled.models.entities.Posting;
import com.settled.models.entities.Transaction;
import com.settled.models.requests.CreatePostingRequest;
import com.settled.models.responses.TransactionDetailResponse;
import com.settled.repositories.AccountRepository;
import com.settled.repositories.PostingRepository;
import com.settled.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LedgerServiceTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PostingRepository postingRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void testAddPosting_Success() {
        UUID txnId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.DRAFT)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        Account account = Account.builder().id(accountId).code("ACC001").build();

        CreatePostingRequest request = CreatePostingRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .build();

        Posting savedPosting = Posting.builder()
                .id(UUID.randomUUID())
                .transaction(transaction)
                .account(account)
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.findById(txnId)).thenReturn(Optional.of(transaction));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(postingRepository.save(any(Posting.class))).thenReturn(savedPosting);
        when(postingRepository.findByTransactionId(txnId)).thenReturn(List.of(savedPosting));

        TransactionDetailResponse response = ledgerService.addPosting(txnId, request);

        assertNotNull(response);
        assertEquals(txnId, response.getId());
        assertEquals("TXN001", response.getReference());
        assertEquals(TransactionStatus.DRAFT, response.getStatus());
        assertEquals(1, response.getPostings().size());
        assertEquals(new BigDecimal("100.00"), response.getPostings().get(0).getAmount());

        verify(transactionRepository, times(1)).findById(txnId);
        verify(accountRepository, times(1)).findById(accountId);
        verify(postingRepository, times(1)).save(any(Posting.class));
        verify(postingRepository, times(1)).findByTransactionId(txnId);
    }

    @Test
    void testAddPosting_TransactionNotFound() {
        UUID txnId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        CreatePostingRequest request = CreatePostingRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .build();

        when(transactionRepository.findById(txnId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ledgerService.addPosting(txnId, request));

        verify(transactionRepository, times(1)).findById(txnId);
        verify(accountRepository, never()).findById(any());
        verify(postingRepository, never()).save(any());
    }

    @Test
    void testAddPosting_TransactionNotInDraftState() {
        UUID txnId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.POSTED)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        CreatePostingRequest request = CreatePostingRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .build();

        when(transactionRepository.findById(txnId)).thenReturn(Optional.of(transaction));

        assertThrows(InvalidPostingException.class, () -> ledgerService.addPosting(txnId, request));

        verify(transactionRepository, times(1)).findById(txnId);
        verify(accountRepository, never()).findById(any());
        verify(postingRepository, never()).save(any());
    }

    @Test
    void testAddPosting_AccountNotFound() {
        UUID txnId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .id(txnId)
                .reference("TXN001")
                .status(TransactionStatus.DRAFT)
                .postedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        CreatePostingRequest request = CreatePostingRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .direction(PostingDirection.DEBIT)
                .build();

        when(transactionRepository.findById(txnId)).thenReturn(Optional.of(transaction));
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ledgerService.addPosting(txnId, request));

        verify(transactionRepository, times(1)).findById(txnId);
        verify(accountRepository, times(1)).findById(accountId);
        verify(postingRepository, never()).save(any());
    }
}
