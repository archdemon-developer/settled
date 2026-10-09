package com.settled.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI settledOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Settled - Ledger as a Service")
                        .description("Double-entry ledger API for financial transaction management. "
                                + "Create accounts, manage transactions, and post double-entry accounting entries.")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Settled Development Team")
                                .email("dev@settled.io")
                                .url("https://github.com/archdemon-developer/settled"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addServersItem(new Server().url("http://localhost:8080").description("Local Development"))
                .addServersItem(new Server().url("https://api.settled.io").description("Production"));
    }
}
