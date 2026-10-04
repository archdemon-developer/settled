package com.settled.utils;

import com.settled.models.entities.Account;
import com.settled.models.entities.Transaction;
import com.settled.models.responses.AccountResponse;
import com.settled.models.responses.TransactionResponse;
import java.util.List;

public class MapperUtil {

    private MapperUtil() {}

    public static AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountResponse.builder()
                .id(account.getId())
                .code(account.getCode())
                .name(account.getName())
                .type(account.getType())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public static List<AccountResponse> toAccountResponseList(List<Account> accounts) {
        return accounts.stream().map(MapperUtil::toAccountResponse).toList();
    }

    public static List<TransactionResponse> toTransactionResponseList(List<Transaction> transactions) {
        return transactions.stream().map(MapperUtil::toTransactionResponse).toList();
    }

    public static TransactionResponse toTransactionResponse(Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .description(transaction.getDescription())
                .postedAt(transaction.getPostedAt() != null ? transaction.getPostedAt() : null)
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
