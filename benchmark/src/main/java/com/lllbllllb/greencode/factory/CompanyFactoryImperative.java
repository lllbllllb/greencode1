package com.lllbllllb.greencode.factory;

import java.util.HashSet;
import java.util.Set;

import com.lllbllllb.greencode.Company;

import static com.lllbllllb.greencode.Util.getStringBySeed;
import static com.lllbllllb.greencode.Util.getUuidBySeed;

public class CompanyFactoryImperative implements CompanyFactory {

    @Override
    public Company getSimpleCompany(int seed) {
        return new Company(
            null,
            getStringBySeed(seed),
            null
        );
    }

    @Override
    public Set<Company> getSimpleCompanies(int count) {
        var companies = new HashSet<Company>();

        for (int i = 0; i < count; i++) {
            var company = getSimpleCompany(i);

            companies.add(company);
        }

        return companies;
    }

    @Override
    public Company getCompany(int seed, int studiosCount, int domainCount, int employeeCount) {
        return new Company(
            getUuidBySeed(seed),
            getStringBySeed(seed),
            getStudios(studiosCount, domainCount, employeeCount)
        );
    }

    @Override
    public Set<Company> getCompanies(int count, int studiosCount, int domainCount, int employeeCount) {
        var companies = new HashSet<Company>();

        for (int i = 0; i < count; i++) {
            var company = getCompany(i, studiosCount, domainCount, employeeCount);

            companies.add(company);
        }

        return companies;
    }

    @Override
    public Company.Studio getStudio(int seed, int domainCount, int employeeCount) {
        return new Company.Studio(
            getUuidBySeed(seed),
            getStringBySeed(seed),
            getDomains(domainCount, employeeCount)
        );
    }

    @Override
    public Set<Company.Studio> getStudios(int count, int domainCount, int employeeCount) {
        var studios = new HashSet<Company.Studio>();

        for (int i = 0; i < count; i++) {
            var studio = getStudio(i, domainCount, employeeCount);

            studios.add(studio);
        }

        return studios;
    }

    @Override
    public Company.Domain getDomain(int seed, int employeeCount) {
        return new Company.Domain(
            getUuidBySeed(seed),
            getStringBySeed(seed),
            getEmployees(employeeCount)
        );
    }

    @Override
    public Set<Company.Domain> getDomains(int count, int employeeCount) {
        var domains = new HashSet<Company.Domain>();

        for (int i = 0; i < count; i++) {
            var employee = getDomain(i, employeeCount);

            domains.add(employee);
        }

        return domains;
    }

    @Override
    public Company.Employee getEmployee(int seed) {
        return new Company.Employee(
            getUuidBySeed(seed),
            getStringBySeed(seed),
            seed,
            getJobTitleBySeed(seed),
            getLevelBySeed(seed)
        );
    }

    @Override
    public Set<Company.Employee> getEmployees(int count) {
        var employees = new HashSet<Company.Employee>();

        for (int i = 0; i < count; i++) {
            var employee = getEmployee(i);

            employees.add(employee);
        }

        return employees;
    }

}
