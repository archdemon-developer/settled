package com.settled.docs;

public final class ApiExamples {

    private ApiExamples() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String SUCCESS_201_TRANSACTION_CREATED = """
            {
              "status": "SUCCESS",
              "code": 201,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": {
                "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                "reference": "TXN-001",
                "description": "Monthly rent payment",
                "postedAt": "2026-10-10T06:50:48.115Z",
                "status": "DRAFT",
                "createdAt": "2026-10-10T06:50:48.115Z"
              }
            }
            """;

    public static final String SUCCESS_201_POSTING_ADDED = """
            {
              "status": "SUCCESS",
              "code": 201,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": {
                "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                "reference": "TXN-001",
                "description": "Monthly rent payment",
                "postedAt": "2026-10-10T06:50:48.115Z",
                "status": "DRAFT",
                "createdAt": "2026-10-10T06:50:48.115Z",
                "postings": [
                  {
                    "id": "3f2b8c1a-9d4e-4b7a-8c6d-2e1f0a9b8c7d",
                    "accountId": "550e8400-e29b-41d4-a716-446655440000",
                    "amount": 1000.50,
                    "direction": "DEBIT",
                    "createdAt": "2026-10-10T06:50:48.115Z"
                  }
                ]
              }
            }
            """;
    public static final String SUCCESS_201_ACCOUNT_CREATED = """
            {
              "status": "SUCCESS",
              "code": 201,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "code": "CHK001",
                "name": "Main Checking",
                "type": "CHECKING",
                "status": "ACTIVE",
                "createdAt": "2026-10-10T06:50:48.115Z"
              }
            }
            """;
    public static final String VAL_001_VALIDATION_ERROR = """
            {
                                                  "status": "ERROR",
                                                  "code": 400,
                                                  "timestamp": "2026-10-10T06:50:48.115Z",
                                                  "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
                                                  "error": {
                                                    "errorCode": "VAL_001",
                                                    "message": "Validation failed",
                                                    "violations": [
                                                      { "field": "name", "message": "Name is required" },
                                                      { "field": "type", "message": "Type is required" }
                                                    ]
                                                  }
                                                }
            """;

    public static final String VAL_002_MALFORMED_REQUEST_BODY = """
            {
                                                  "status": "ERROR",
                                                  "code": 400,
                                                  "timestamp": "2026-10-10T06:50:48.115Z",
                                                  "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
                                                  "error": {
                                                    "errorCode": "VAL_002",
                                                    "message": "Request body is malformed or contains an invalid value",
                                                    "details": "Request body is missing, is not valid JSON, or contains a value of the wrong type"
                                                  }
                                                }
            """;

    public static final String ACC_003_ACCOUNT_ALREADY_EXISTS = """
                       {
                                                  "status": "ERROR",
                                                  "code": 409,
                                                  "timestamp": "2026-10-10T06:50:48.115Z",
                                                  "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
                                                  "error": {
                                                    "errorCode": "ACC_003",
                                                    "message": "Account code already exists",
                                                    "details": "Code 'CHK001' already exists",
                                                    "field": "code"
                                                  }
                                                }
            """;

    public static final String ACC_001_ACCOUNT_WITH_ID_NOT_FOUND = """
            {
                                                  "status": "ERROR",
                                                  "code": 404,
                                                  "timestamp": "2026-10-10T06:50:48.115Z",
                                                  "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
                                                  "error": {
                                                    "errorCode": "ACC_001",
                                                    "message": "Account not found",
                                                    "details": "Account with ID '550e8400-e29b-41d4-a716-446655440000' not found"
                                                  }
                                                }
            """;

    public static final String ACC_001_ACCOUNT_WITH_CODE_NOT_FOUND = """
             {
                "status": "ERROR",
                "code": 404,
                "timestamp": "2026-10-10T06:50:48.115Z",
                "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
                "error": {
                  "errorCode": "ACC_001",
                  "message": "Account not found",
                  "details": "Account with code 'CHK001' not found",
                  "field": "code"
                }
              }
            """;

    public static final String VAL_001_CREATE_TRANSACTION_VALIDATION_ERROR = """
            {
              "status": "ERROR",
              "code": 400,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "VAL_001",
                "message": "Validation failed",
                "violations": [
                  { "field": "reference", "message": "Transaction reference is required" },
                  { "field": "postedAt", "message": "Posted at timestamp is required" }
                ]
              }
            }
            """;

    public static final String VAL_001_ADD_POSTING_VALIDATION_ERROR = """
            {
              "status": "ERROR",
              "code": 400,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "VAL_001",
                "message": "Validation failed",
                "violations": [
                  { "field": "accountId", "message": "Account ID is required" },
                  { "field": "amount", "message": "Amount must be greater than zero" }
                ]
              }
            }
            """;

    public static final String VAL_003_INVALID_PATH_PARAMETER = """
            {
              "status": "ERROR",
              "code": 400,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "VAL_003",
                "message": "Request parameter has an invalid value",
                "details": "Parameter 'id' has an invalid value: 'not-a-uuid'",
                "field": "id"
              }
            }
            """;

    public static final String TXN_001_TRANSACTION_NOT_FOUND = """
            {
              "status": "ERROR",
              "code": 404,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "TXN_001",
                "message": "Transaction not found",
                "details": "Transaction with ID '550e8400-e29b-41d4-a716-446655440000' not found"
              }
            }
            """;

    public static final String TXN_002_NOT_DRAFT_ON_POST = """
            {
              "status": "ERROR",
              "code": 409,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "TXN_002",
                "message": "Transaction is not in DRAFT state",
                "details": "Transaction must be in DRAFT state to post. Current state: POSTED"
              }
            }
            """;

    public static final String TXN_002_NOT_DRAFT_ON_ADD_POSTING = """
            {
              "status": "ERROR",
              "code": 409,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "TXN_002",
                "message": "Transaction is not in DRAFT state",
                "details": "Can only add postings to DRAFT transactions. Current state: POSTED"
              }
            }
            """;

    public static final String TXN_003_DUPLICATE_REFERENCE = """
            {
              "status": "ERROR",
              "code": 409,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "TXN_003",
                "message": "Transaction reference already exists",
                "details": "Transaction reference 'TXN-001' already exists",
                "field": "reference"
              }
            }
            """;

    public static final String TXN_004_NO_POSTINGS = """
            {
              "status": "ERROR",
              "code": 422,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "TXN_004",
                "message": "Transaction has no postings",
                "details": "Transaction has no postings. Add at least one debit and one credit before posting."
              }
            }
            """;

    public static final String POST_001_POSTINGS_DO_NOT_BALANCE = """
            {
              "status": "ERROR",
              "code": 422,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "POST_001",
                "message": "Invalid posting: debits must equal credits",
                "details": "Transaction does not balance. Debits: 1000.50, Credits: 500.00"
              }
            }
            """;

    public static final String VAL_003_INVALID_STATUS_PARAMETER = """
            {
              "status": "ERROR",
              "code": 400,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "error": {
                "errorCode": "VAL_003",
                "message": "Request parameter has an invalid value",
                "details": "Parameter 'status' has an invalid value: 'INVALID_STATUS'",
                "field": "status"
              }
            }
            """;

    public static final String SUCCESS_200_ACCOUNT_FOUND = """
            {
              "status": "SUCCESS",
              "code": 200,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "code": "CHK001",
                "name": "Main Checking",
                "type": "CHECKING",
                "status": "ACTIVE",
                "createdAt": "2026-10-10T06:50:48.115Z"
              }
            }
            """;

    public static final String SUCCESS_200_ACCOUNT_ARCHIVED = """
            {
              "status": "SUCCESS",
              "code": 200,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "code": "CHK001",
                "name": "Main Checking",
                "type": "CHECKING",
                "status": "ARCHIVED",
                "createdAt": "2026-10-10T06:50:48.115Z"
              }
            }
            """;

    public static final String SUCCESS_200_ACCOUNT_LIST = """
            {
              "status": "SUCCESS",
              "code": 200,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": [
                {
                  "id": "550e8400-e29b-41d4-a716-446655440000",
                  "code": "CHK001",
                  "name": "Main Checking",
                  "type": "CHECKING",
                  "status": "ACTIVE",
                  "createdAt": "2026-10-10T06:50:48.115Z"
                },
                {
                  "id": "9b2f4a6c-1d3e-4f58-a7b9-0c8d7e6f5a4b",
                  "code": "SAV001",
                  "name": "Savings",
                  "type": "SAVINGS",
                  "status": "ACTIVE",
                  "createdAt": "2026-10-10T06:50:48.115Z"
                }
              ]
            }
            """;
    public static final String SUCCESS_200_TRANSACTION_FOUND = """
            {
              "status": "SUCCESS",
              "code": 200,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": {
                "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                "reference": "TXN-001",
                "description": "Monthly rent payment",
                "postedAt": "2026-10-10T06:50:48.115Z",
                "status": "DRAFT",
                "createdAt": "2026-10-10T06:50:48.115Z"
              }
            }
            """;

    public static final String SUCCESS_200_TRANSACTION_LIST = """
            {
              "status": "SUCCESS",
              "code": 200,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": [
                {
                  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                  "reference": "TXN-001",
                  "description": "Monthly rent payment",
                  "postedAt": "2026-10-10T06:50:48.115Z",
                  "status": "DRAFT",
                  "createdAt": "2026-10-10T06:50:48.115Z"
                },
                {
                  "id": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
                  "reference": "TXN-002",
                  "description": "Office supplies",
                  "postedAt": "2026-10-10T06:50:48.115Z",
                  "status": "POSTED",
                  "createdAt": "2026-10-10T06:50:48.115Z"
                }
              ]
            }
            """;

    public static final String SUCCESS_200_TRANSACTION_POSTED = """
            {
              "status": "SUCCESS",
              "code": 200,
              "timestamp": "2026-10-10T06:50:48.115Z",
              "requestId": "6f1d9c1e-3b7a-4a55-9c0e-1d2f3a4b5c6d",
              "data": {
                "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                "reference": "TXN-001",
                "description": "Monthly rent payment",
                "postedAt": "2026-10-10T06:50:48.115Z",
                "status": "POSTED",
                "createdAt": "2026-10-10T06:50:48.115Z"
              }
            }
            """;
}
