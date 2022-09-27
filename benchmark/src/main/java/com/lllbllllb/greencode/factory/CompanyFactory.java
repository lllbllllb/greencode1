package com.lllbllllb.greencode.factory;

import java.util.Set;

import com.lllbllllb.greencode.Company;

public interface CompanyFactory {

    Company getSimpleCompany(int seed);

    Set<Company> getSimpleCompanies(int count);

    Company getCompany(int seed, int studiosCount, int domainCount, int employeeCount);

    Set<Company> getCompanies(int count, int studiosCount, int domainCount, int employeeCount);

    Company.Studio getStudio(int seed, int domainCount, int employeeCount);

    Set<Company.Studio> getStudios(int count, int domainCount, int employeeCount);

    Company.Domain getDomain(int seed, int employeeCount);

    Set<Company.Domain> getDomains(int count, int employeeCount);

    Company.Employee getEmployee(int seed);

    Set<Company.Employee> getEmployees(int count);

    default Company.JobTitle getJobTitleBySeed(int seed) {
        var jobTitles = Company.JobTitle.values();
        return jobTitles[seed % jobTitles.length];
    }

    default Company.Level getLevelBySeed(int seed) {
        var levels = Company.Level.values();
        return levels[seed % levels.length];
    }

}
