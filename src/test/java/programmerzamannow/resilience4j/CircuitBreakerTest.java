package programmerzamannow.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class CircuitBreakerTest {

    public void callMe() {
        log.info("Call Me");
        throw new IllegalArgumentException("Ups");
    }

    @Test
    void circuitBreaker() {

        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("code");

        for (int index = 0; index < 200; index++) {
            try {
                Runnable runnable = CircuitBreaker.decorateRunnable(circuitBreaker, () -> callMe());
                runnable.run();

            } catch (Exception exception) {
                log.error("Error : {}", exception.getMessage());
            }
        }
    }

    @Test
    void circuitBreakerConfig() {

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(10f)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(10)
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("code", config);

        for (int index = 0; index < 200; index++) {
            try {
                Runnable runnable = CircuitBreaker.decorateRunnable(circuitBreaker, () -> callMe());
                runnable.run();

            } catch (Exception exception) {
                log.error("Error : {}", exception.getMessage());
            }
        }
    }

    @Test
    void circuitBreakerRegistry() {

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(10f)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(10)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        registry.addConfiguration("config", config);

        CircuitBreaker circuitBreaker = registry.circuitBreaker("code", "config");

        for (int index = 0; index < 200; index++) {
            try {
                Runnable runnable = CircuitBreaker.decorateRunnable(circuitBreaker, () -> callMe());
                runnable.run();

            } catch (Exception exception) {
                log.error("Error : {}", exception.getMessage());
            }
        }
    }
}
