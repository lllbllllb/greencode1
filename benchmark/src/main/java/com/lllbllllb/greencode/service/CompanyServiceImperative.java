package com.lllbllllb.greencode.service;

import java.util.ArrayList;
import java.util.Collection;

import com.lllbllllb.greencode.Company;

public class CompanyServiceImperative implements CompanyService {

    @Override
    public Collection<Company.Employee> getEmployees(Collection<Company> companies, String filter) {
        var employees = new ArrayList<Company.Employee>();

        for (var company : companies) {
            if (!company.getName().contains(filter)) {
                for (var studio : company.getStudios()) {
                    if (!studio.getName().contains(filter)) {
                        for (var domain : studio.getDomains()) {
                            if (!domain.getName().contains(filter)) {
                                for (var employee : domain.getEmployees()) {
                                    if (!employee.getName().contains(filter)) {
                                        employees.add(employee);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return employees;
    }

}
