package com.settled.services;

import com.settled.enums.ErrorCode;
import com.settled.enums.PostingDirection;
import com.settled.enums.TransactionStatus;
import com.settled.exceptions.DuplicateResourceException;
import com.settled.exceptions.InvalidPostingException;
import com.settled.exceptions.ResourceNotFoundException;
import com.settled.models.entities.Posting;
import com.settled.models.entities.Transaction;
import com.settled.models.requests.CreateTransactionRequest;
import com.settled.models.responses.TransactionResponse;
import com.settled.repositories.PostingRepository;
import com.settled.repositories.TransactionRepository;
import com.settled.utils.MapperUtil;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PostingRepository postingRepository;

    @Transactional
    public TransactionResponse create(CreateTransactionRequest request) {

        if (transactionRepository.findByReference(request.getReference()).isPresent()) {
            throw new DuplicateResourceException(
                    ErrorCode.DUPLICATE_TRANSACTION_REFERENCE,
                    String.format("Transaction reference '%s' already exists", request.getReference()),
                    "reference");
        }

        Transaction transaction = Transaction.builder()
                .reference(request.getReference())
                .description(request.getDescription())
                .postedAt(request.getPostedAt())
                .status(TransactionStatus.DRAFT)
                .createdAt(Instant.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return MapperUtil.toTransactionResponse(saved);
    }

    public TransactionResponse getById(UUID id) {
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.TRANSACTION_NOT_FOUND, String.format("Transaction with ID '%s' not found", id)));
        return MapperUtil.toTransactionResponse(transaction);
    }

    public TransactionResponse getByReference(String reference) {
        Transaction transaction = transactionRepository
                .findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.TRANSACTION_NOT_FOUND,
                        String.format("Transaction with reference '%s' not found", reference)));
        return MapperUtil.toTransactionResponse(transaction);
    }

    public List<TransactionResponse> listByStatus(TransactionStatus status) {
        return ObjectUtils.isEmpty(status)
                ? MapperUtil.toTransactionResponseList(transactionRepository.findAll())
                : MapperUtil.toTransactionResponseList(transactionRepository.findByStatus(status));
    }

    @Transactional
    public TransactionResponse markAsPosted(UUID id) {
        Transaction transaction = transactionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.TRANSACTION_NOT_FOUND, String.format("Transaction with ID '%s' not found", id)));

        if (transaction.getStatus() != TransactionStatus.DRAFT) {
            throw new InvalidPostingException(
                    ErrorCode.TRANSACTION_NOT_DRAFT,
                    String.format(
                            "Transaction must be in DRAFT state to post. Current state: %s", transaction.getStatus()));
        }

        List<Posting> postings = postingRepository.findByTransactionId(id);

        BigDecimal totalDebits = postings.stream()
                .filter(posting -> posting.getDirection() == PostingDirection.DEBIT)
                .map(posting -> posting.getAmount())
                .reduce(BigDecimal.ZERO, (first, second) -> first.add(second));

        BigDecimal totalCredits = postings.stream()
                .filter(posting -> posting.getDirection() == PostingDirection.CREDIT)
                .map(posting -> posting.getAmount())
                .reduce(BigDecimal.ZERO, (first, second) -> first.add(second));

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new InvalidPostingException(
                    ErrorCode.INVALID_POSTING,
                    String.format("Transaction does not balance. Debits: %s, Credits: %s", totalDebits, totalCredits));
        }

        transaction.setStatus(TransactionStatus.POSTED);
        Transaction saved = transactionRepository.save(transaction);
        return MapperUtil.toTransactionResponse(saved);
    }
}
