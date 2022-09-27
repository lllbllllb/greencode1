package com.lllbllllb.greencode.service;

import java.util.Collection;
import java.util.stream.Collectors;

import com.lllbllllb.greencode.Company;

public class CompanyServiceFunctional implements CompanyService {

    @Override
    public Collection<Company.Employee> getEmployees(Collection<Company> companies, String filter) {
        return companies.stream()
            .filter(company -> !company.getName().contains(filter))
            .flatMap(company -> company.getStudios().stream())
            .filter(studio -> !studio.getName().contains(filter))
            .flatMap(studio -> studio.getDomains().stream())
            .filter(domain -> !domain.getName().contains(filter))
            .flatMap(domain -> domain.getEmployees().stream())
            .filter(employee -> !employee.getName().contains(filter))
            .collect(Collectors.toList());
    }

}
