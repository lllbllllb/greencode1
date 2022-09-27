package com.lllbllllb.greencode;

import java.util.Set;

import com.lllbllllb.greencode.factory.CompanyFactory;
import com.lllbllllb.greencode.factory.CompanyFactoryFunctional;
import com.lllbllllb.greencode.factory.CompanyFactoryImperative;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

public class Step0CompanyFactoryTest extends BaseBenchmarkTest {

    private CompanyFactory factoryFunctional;

    private CompanyFactory factoryImperative;

    @Param({"2", "16"})
    private int count;

    @Setup(Level.Trial)
    public void setupTrial() {
        factoryFunctional = new CompanyFactoryFunctional();
        factoryImperative = new CompanyFactoryImperative();
    }

    @Benchmark
    public Set<Company> createCollectionFunctionalTest() {
        return factoryFunctional.getCompanies(count, count, count, count);
    }

    @Benchmark
    public Set<Company> createCollectionImperativeTest() {
        return factoryImperative.getCompanies(count, count, count, count);
    }

    @Benchmark
    public Set<Company> buildSimpleTest() {
        return factoryFunctional.getSimpleCompanies(count);
    }

    @Benchmark
    public Set<Company> constructSimpleTest() {
        return factoryImperative.getSimpleCompanies(count);
    }

}