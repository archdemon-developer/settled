package com.settled.services;

import com.settled.enums.ErrorCode;
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
import com.settled.utils.MapperUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final TransactionRepository transactionRepository;
    private final PostingRepository postingRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDetailResponse addPosting(UUID transactionId, CreatePostingRequest request) {

        Transaction transaction = transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.TRANSACTION_NOT_FOUND,
                        String.format("Transaction with ID '%s' not found", transactionId)));

        if (transaction.getStatus() != TransactionStatus.DRAFT) {
            throw new InvalidPostingException(
                    ErrorCode.TRANSACTION_NOT_DRAFT,
                    String.format(
                            "Can only add postings to DRAFT transactions. Current state: %s", transaction.getStatus()));
        }

        Account account = accountRepository
                .findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ACCOUNT_NOT_FOUND,
                        String.format("Account with ID '%s' not found", request.getAccountId())));

        Posting posting = Posting.builder()
                .transaction(transaction)
                .account(account)
                .amount(request.getAmount())
                .direction(request.getDirection())
                .build();

        postingRepository.save(posting);

        List<Posting> allPostings = postingRepository.findByTransactionId(transactionId);
        return MapperUtil.toTransactionDetailResponse(transaction, allPostings);
    }
}
