package com.settled.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private final String appVersion;

    public OpenApiConfiguration(@Value("${app.version}") String appVersion) {
        this.appVersion = appVersion;
    }

    @Bean
    public OpenAPI settledOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Settled - Ledger as a Service")
                        .description("""
        Double-entry ledger API for financial transaction management.

        ## Typical flow
        1. **Create accounts** - `POST /api/v1/accounts`
        2. **Create a transaction** - `POST /api/v1/transactions` (starts in `DRAFT`)
        3. **Add postings** - `POST /api/v1/transactions/{id}/postings` (one debit or credit per call)
        4. **Post the transaction** - `POST /api/v1/transactions/{id}/post` (total debits must equal total credits)

        Once a transaction is posted, no further postings can be added.

        ## Responses
        Every response uses the same envelope: `status` (`SUCCESS` or `ERROR`), `code`, `timestamp` and `requestId`, followed by either `data` or `error`. Errors carry an `errorCode` such as `ACC_003` or `TXN_002`, and validation failures list each problem under `violations`.
        """)
                        .version(this.appVersion)
                        .contact(new Contact()
                                .name("Archdemon Developer")
                                .url("https://github.com/archdemon-developer/settled"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/license    s/LICENSE-2.0.html")))
                .addServersItem(new Server().url("http://localhost:8080").description("Local Development"));
    }
}
