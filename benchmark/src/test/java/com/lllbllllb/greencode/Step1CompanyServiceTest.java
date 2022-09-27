package com.lllbllllb.greencode;

import java.util.Collection;
import java.util.Set;

import com.lllbllllb.greencode.factory.CompanyFactoryImperative;
import com.lllbllllb.greencode.service.CompanyService;
import com.lllbllllb.greencode.service.CompanyServiceFunctional;
import com.lllbllllb.greencode.service.CompanyServiceImperative;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

public class Step1CompanyServiceTest extends BaseBenchmarkTest {

    private Set<Company> companies;

    private CompanyService serviceFunctional;

    private CompanyService serviceImperative;

    @Param({"2", "16"})
    private int count;

    @Param({"-", "--"}) // everything, nothing
    private String seq;

    @Setup(Level.Trial)
    public void setupTrial() {
        companies = new CompanyFactoryImperative().getCompanies(count, count, count, count);
        serviceFunctional = new CompanyServiceFunctional();
        serviceImperative = new CompanyServiceImperative();
    }

    @Benchmark
    public Collection<Company.Employee> flattingFunctionalTest() {
        return serviceFunctional.getEmployees(companies, seq);
    }

    @Benchmark
    public Collection<Company.Employee> flattingImperativeTest() {
        return serviceImperative.getEmployees(companies, seq);
    }

}