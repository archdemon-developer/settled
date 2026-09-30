package com.settled.utils;

import com.settled.models.entities.Account;
import com.settled.models.responses.AccountResponse;
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
}
