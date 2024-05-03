package programmerzamannow.resilience4j;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class TimeLimiterTest {

    @SneakyThrows
    public String slow() {
        log.info("slow");
        Thread.sleep(5000);
        return "alvenio";
    }

    @Test
    void testLimiter() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> slow());

        TimeLimiter timeLimiter = TimeLimiter.ofDefaults("coding");
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);

        callable.call();
    }

    @Test
    void testLimiterConfig() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> slow());

        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();

        TimeLimiter timeLimiter = TimeLimiter.of("code", config);
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);

        callable.call();
    }

    @Test
    void testLimiterRegistry() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> slow());

        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();
        registry.timeLimiter("config", config);

        TimeLimiter timeLimiter = registry.timeLimiter("code", "config");
        Callable<String> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, () -> future);

        callable.call();
    }
}
