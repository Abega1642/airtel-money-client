package dev.razafindratelo.airtel_money_client.conf;

import dev.razafindratelo.airtel_money_client.InfraGenerated;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base integration test facade responsible for bootstrapping all infrastructure dependencies
 * required for end-to-end tests.
 *
 * <p>This class starts and manages the lifecycle of the following Testcontainers:
 *
 * <ul>
 *   <li><b>Database</b> - Auto-discovered via classpath scanning
 *   <li><b>RabbitMQ</b> - asynchronous messaging
 *   <li><b>S3-compatible bucket</b> - file storage
 *   <li><b>Email service</b> - outbound email testing
 * </ul>
 *
 * <p><b>Database Auto-Discovery:</b><br>
 * The database implementation is automatically discovered by scanning for classes implementing
 * PersistenceConf. No configuration needed - it uses whichever implementation is present.
 *
 * <p>Additionally, it dynamically injects environment variables and container connection properties
 * into the Spring context using {@link DynamicPropertySource}.
 *
 * <p><b>Lifecycle guarantees:</b>
 *
 * <ul>
 *   <li>Containers are started once per test JVM
 *   <li>Containers are stopped gracefully via JVM shutdown hook
 * </ul>
 *
 * <p>This class is marked as {@link InfraGenerated} and must be extended by all integration tests
 * that require real infrastructure.
 */
@Slf4j
@InfraGenerated
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
public abstract class FacadeIT {

  /**
   * Registers dynamic container and environment properties into the Spring context.
   *
   * <p>If {@code EnvConf} is present in the project, it will be loaded reflectively to allow
   * project-specific environment variables without hard dependency.
   */
  @SneakyThrows
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {

    Class<?> envConfClazz = EnvConf.class;
    var configureMethod =
        envConfClazz.getDeclaredMethod("configureProperties", DynamicPropertyRegistry.class);
    var envConfInstance = envConfClazz.getConstructor().newInstance();
    configureMethod.invoke(envConfInstance, registry);
  }
}
