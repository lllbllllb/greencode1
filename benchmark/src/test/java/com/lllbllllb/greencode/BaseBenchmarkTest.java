package com.lllbllllb.greencode;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 1, warmups = 1, jvmArgs = {"-Xms512M", "-Xmx512M"})
@Warmup(iterations = 2, time = 4, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 6, time = 10, timeUnit = TimeUnit.SECONDS)
@Threads(1)
@ExtendWith(TimingExtension.class)
abstract class BaseBenchmarkTest {

    @Test
//    @Disabled("for remote build")
    void benchmark() throws Exception {
        org.openjdk.jmh.Main.main(new String[]{});
    }

}
