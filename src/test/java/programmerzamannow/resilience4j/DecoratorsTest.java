package programmerzamannow.resilience4j;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public class DecoratorsTest {

    @SneakyThrows
    public void slow() {
        log.info("slow");
//        Thread.sleep(5_000L);
        Thread.sleep(1_000L);
        throw new IllegalArgumentException("Error");
    }

    @SneakyThrows
    public String sayHello() {
        log.info("Say hello");
//        Thread.sleep(5_000L);
        Thread.sleep(1_000L);
        throw new IllegalArgumentException("Error");
    }

    @Test
    void decorators() throws InterruptedException {
        RateLimiter rateLimiter = RateLimiter.of("code-ratelimiter", RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .build());

        Retry retry = Retry.of("code-retry", RetryConfig.custom()
                .maxAttempts(10)
                .waitDuration(Duration.ofMillis(10))
                .build());

        Runnable runnable = Decorators.ofRunnable(() -> slow())
                .withRetry(retry)
                .withRateLimiter(rateLimiter)
                .decorate();

        for (int index = 0; index < 100; index++) {
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @Test
    void fallback() throws InterruptedException {
        RateLimiter rateLimiter = RateLimiter.of("code-ratelimiter", RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .build());

        Retry retry = Retry.of("code-retry", RetryConfig.custom()
                .maxAttempts(10)
                .waitDuration(Duration.ofMillis(10))
                .build());

        Supplier<String> supplier = Decorators.ofSupplier(() -> sayHello())
                .withRetry(retry)
                .withRateLimiter(rateLimiter)
                .withFallback(throwable -> "Hello Guest")
                .decorate();

        System.out.println(supplier.get());
    }
}
