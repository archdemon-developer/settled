package com.settled.services;

import com.settled.enums.AccountStatus;
import com.settled.enums.ErrorCode;
import com.settled.exceptions.DuplicateResourceException;
import com.settled.exceptions.ResourceNotFoundException;
import com.settled.models.entities.Account;
import com.settled.models.requests.CreateAccountRequest;
import com.settled.models.responses.AccountResponse;
import com.settled.repositories.AccountRepository;
import com.settled.utils.MapperUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {

        if (accountRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException(
                    ErrorCode.DUPLICATE_ACCOUNT_CODE,
                    String.format("Code '%s' already exists", request.getCode()),
                    "code");
        }

        Account account = Account.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .status(AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        return MapperUtil.toAccountResponse(saved);
    }

    public AccountResponse getById(UUID id) {
        return MapperUtil.toAccountResponse(findAccountOrThrow(id));
    }

    public AccountResponse getByCode(String code) {
        Account account = accountRepository
                .findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ACCOUNT_NOT_FOUND, String.format("Account with code '%s' not found", code), "code"));
        return MapperUtil.toAccountResponse(account);
    }

    public List<AccountResponse> listByStatus(AccountStatus status) {
        return status == null
                ? MapperUtil.toAccountResponseList(accountRepository.findAll())
                : MapperUtil.toAccountResponseList(accountRepository.findByStatus(status));
    }

    @Transactional
    public AccountResponse archive(UUID id) {
        Account account = findAccountOrThrow(id);
        account.setStatus(AccountStatus.ARCHIVED);
        Account saved = accountRepository.save(account);
        return MapperUtil.toAccountResponse(saved);
    }

    private Account findAccountOrThrow(UUID id) {
        return accountRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.ACCOUNT_NOT_FOUND, String.format("Account with ID '%s' not found", id)));
    }
}
