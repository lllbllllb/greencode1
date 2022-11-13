package com.lllbllllb.loader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadService implements Finalizable, Resettable, Initializable {

    private final Map<String, Mono<Tuple2<Long, Integer>>> preyNameToLoadUnitMap = new ConcurrentHashMap<>();

    private final Map<String, Disposable> preyNameToDisposableMap = new ConcurrentHashMap<>();

    private final Map<String, LoadConfiguration> nameToLoadConfigurationMap = new ConcurrentHashMap<>();

    private final LoadUnitFactory loadUnitFactory;

    @Override
    public void initialize(Prey prey) {
        var preyName = prey.name();
        var loadUnit = loadUnitFactory.create(prey);

        preyNameToLoadUnitMap.put(preyName, loadUnit);
    }

    public Mono<Tuple2<Long, Integer>> getUnit(String preyName) {
        return preyNameToLoadUnitMap.get(preyName);
    }

    public void registerActiveLoaderDisposable(String preyName, Disposable loaderDisposable) {
        preyNameToDisposableMap.put(preyName, loaderDisposable);
    }

    public void registerLoadConfiguration(String preyName, LoadConfiguration loadConfiguration) {
        nameToLoadConfigurationMap.put(preyName, loadConfiguration);
    }

    public LoadConfiguration getLoadConfiguration() {
        if (nameToLoadConfigurationMap.size() > 0) {
            return nameToLoadConfigurationMap.values().iterator().next();
        } else {
            return new LoadConfiguration(0, false, true);
        }
    }

    @Override
    public void finalize(String preyName) {
        var loadUnit = preyNameToLoadUnitMap.remove(preyName);

        if (loadUnit == null) {
            log.warn("Load unit for [{}] was already finalized", preyName);
        }

        nameToLoadConfigurationMap.remove(preyName);

        reset(preyName);
    }

    @Override
    public void reset(String preyName) {
        var loadDisposable = preyNameToDisposableMap.remove(preyName);

        if (loadDisposable != null && !loadDisposable.isDisposed()) {
            loadDisposable.dispose();
        } else {
            log.info("Load disposable for [{}] was already finalized", preyName);
        }
    }
}
