package com.logistica.sistema;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Classe base para testes end-to-end: sobe um PostgreSQL real em
 * container (nao um H2 em memoria), porque a migration usa recursos
 * especificos do Postgres (indice unico parcial com WHERE) que um
 * banco em memoria nao reproduziria fielmente.
 *
 * Para rodar com Docker/Testcontainers (padrao):
 *   mvn test
 *
 * Para rodar com um PostgreSQL ja rodando (sem Docker):
 *   mvn test -Dskip.testcontainers=true \
 *            -Dspring.datasource.url=jdbc:postgresql://localhost:5433/logistica_test \
 *            -Dspring.datasource.username=test -Dspring.datasource.password=test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    private static final boolean SKIP_TESTCONTAINERS = Boolean.getBoolean("skip.testcontainers");

    private static PostgreSQLContainer<?> postgres;

    static {
        if (!SKIP_TESTCONTAINERS) {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("logistica_test")
                    .withUsername("test")
                    .withPassword("test");
            postgres.start();
        }
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        if (postgres != null && postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        }
    }

    @AfterAll
    static void pararContainer() {
        if (postgres != null && postgres.isRunning()) {
            postgres.stop();
        }
    }
}
