package com.lllbllllb.greencode.factory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.lllbllllb.greencode.Company;

import static com.lllbllllb.greencode.Util.getStringBySeed;
import static com.lllbllllb.greencode.Util.getUuidBySeed;

public class CompanyFactoryFunctional implements CompanyFactory {

    @Override
    public Company getSimpleCompany(int seed) {
        return Company.builder()
            .name(getStringBySeed(seed))
            .build();
    }

    @Override
    public Set<Company> getSimpleCompanies(int count) {
        return IntStream.range(0, count)
            .mapToObj(this::getSimpleCompany)
            .collect(Collectors.toSet());
    }

    @Override
    public Company getCompany(int seed, int studiosCount, int domainCount, int employeeCount) {
        return Company.builder()
            .uuid(getUuidBySeed(seed))
            .name(getStringBySeed(seed))
            .studios(getStudios(studiosCount, domainCount, employeeCount))
            .build();
    }

    @Override
    public Set<Company> getCompanies(int count, int studiosCount, int domainCount, int employeeCount) {
        return IntStream.range(0, count)
            .mapToObj(i -> getCompany(i, studiosCount, domainCount, employeeCount))
            .collect(Collectors.toSet());
    }

    @Override
    public Company.Studio getStudio(int seed, int domainCount, int employeeCount) {
        return Company.Studio.builder()
            .uuid(getUuidBySeed(seed))
            .name(getStringBySeed(seed))
            .domains(getDomains(domainCount, employeeCount))
            .build();
    }

    @Override
    public Set<Company.Studio> getStudios(int count, int domainCount, int employeeCount) {
        return IntStream.range(0, count)
            .mapToObj(i -> getStudio(i, domainCount, employeeCount))
            .collect(Collectors.toSet());
    }

    @Override
    public Company.Domain getDomain(int seed, int employeeCount) {
        return Company.Domain.builder()
            .uuid(getUuidBySeed(seed))
            .name(getStringBySeed(seed))
            .employees(getEmployees(employeeCount))
            .build();
    }

    @Override
    public Set<Company.Domain> getDomains(int count, int employeeCount) {
        return IntStream.range(0, count)
            .mapToObj(i -> getDomain(i, employeeCount))
            .collect(Collectors.toSet());
    }

    @Override
    public Company.Employee getEmployee(int seed) {
        return Company.Employee.builder()
            .uuid(getUuidBySeed(seed))
            .name(getStringBySeed(seed))
            .age(seed)
            .jobTitle(getJobTitleBySeed(seed))
            .level(getLevelBySeed(seed))
            .build();
    }

    @Override
    public Set<Company.Employee> getEmployees(int count) {
        return IntStream.range(0, count)
            .mapToObj(this::getEmployee)
            .collect(Collectors.toSet());
    }

}
