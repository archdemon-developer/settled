package com.settled;

import com.settled.configuration.PostgresContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresContainerConfiguration.class)
public class SettledApplicationTests {

    @Test
    public void contextLoads() {}
}
