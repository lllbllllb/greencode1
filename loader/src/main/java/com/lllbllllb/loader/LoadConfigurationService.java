package com.lllbllllb.loader;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class LoadConfigurationService implements Initializable, Finalizable {

    private static final LoadConfiguration DEFAULT_LOAD_CONFIGURATION = new LoadConfiguration(0, false, true, 30);

    private static final long NANOS_PER_SECOND =  1000_000_000L;

    private final Map<String, LoadConfiguration> nameToLoadConfigurationMap = new ConcurrentHashMap<>();

    public LoadConfiguration getLoadConfiguration() {
        if (nameToLoadConfigurationMap.size() > 0) {
            return nameToLoadConfigurationMap.values().iterator().next();
        } else {
            return DEFAULT_LOAD_CONFIGURATION;
        }
    }

    public LoadConfiguration getLoadConfiguration(String preyName) {
        var configuration = nameToLoadConfigurationMap.get(preyName);

        if (configuration != null) {
            return configuration;
        }

        throw new IllegalStateException("Load configuration for [%s] not exists".formatted(preyName));
    }

    public void updateLoadConfiguration(String preyName, LoadConfiguration loadConfiguration) {
        nameToLoadConfigurationMap.put(preyName, loadConfiguration);
    }

    public Mono<Duration> getLoadInterval(String preyName) {
        var rps = getLoadConfiguration(preyName).rps();

        if (rps > 0) {
            return Mono.just(Duration.ofNanos(NANOS_PER_SECOND / rps));
        }

        return Mono.empty();
    }

    public int getMaxConcurrency(String preyName) {
        var configuration = getLoadConfiguration(preyName).rps();

        return 9_999_999; // to achive arrayQueue
    }

    @Override
    public void finalize(String preyName) {
        nameToLoadConfigurationMap.remove(preyName);
    }

    @Override
    public void initialize(Prey prey) {
        nameToLoadConfigurationMap.put(prey.name(), DEFAULT_LOAD_CONFIGURATION);
    }

}
