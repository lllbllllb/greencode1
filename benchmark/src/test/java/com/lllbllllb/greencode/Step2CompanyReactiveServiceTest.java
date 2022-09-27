package com.lllbllllb.greencode;

import java.util.Set;

import com.lllbllllb.greencode.factory.CompanyFactoryImperative;
import com.lllbllllb.greencode.reactiveservice.CompanyReactiveBaseService;
import com.lllbllllb.greencode.reactiveservice.CompanyReactiveManualOptimizedService;
import com.lllbllllb.greencode.reactiveservice.CompanyReactiveService;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import reactor.core.Disposable;
import reactor.core.publisher.Hooks;
import reactor.tools.agent.ReactorDebugAgent;

public class Step2CompanyReactiveServiceTest extends BaseBenchmarkTest {

    private Set<Company> companies;

    private CompanyReactiveService serviceBase;

    private CompanyReactiveService serviceManualOptimized;

    @Param({"2", "16"})
    private int count;

    @Param({"-", "--"}) // everything, nothing
    private String seq;

    @Setup(Level.Trial)
    public void setupTrial() {
        Hooks.onOperatorDebug(); // https://projectreactor.io/docs/core/release/reference/#debug-activate
        ReactorDebugAgent.init();

        companies = new CompanyFactoryImperative().getCompanies(count, count, count, count);
        serviceBase = new CompanyReactiveBaseService();
        serviceManualOptimized = new CompanyReactiveManualOptimizedService();
    }

    @Benchmark
    public Disposable flattingBaseTest() {
        return serviceBase.getEmployeesPublisher(companies, seq)
            .subscribe();
    }

    @Benchmark
    public Disposable flattingManualOptimizedTest() {
        return serviceManualOptimized.getEmployeesPublisher(companies, seq)
            .subscribe();
    }

    @Benchmark
    public Disposable findBaseTest() {
        var target = Util.getUuidBySeed(1);

        return serviceBase.findEmployeeById(companies, target)
            .subscribe();
    }

    @Benchmark
    public Disposable findManualOptimizedTest() {
        var target = Util.getUuidBySeed(1);

        return serviceManualOptimized.findEmployeeById(companies, target)
            .subscribe();
    }

}
