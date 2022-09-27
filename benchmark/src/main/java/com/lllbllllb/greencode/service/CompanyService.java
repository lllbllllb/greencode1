package com.lllbllllb.greencode.service;

import java.util.Collection;

import com.lllbllllb.greencode.Company;

public interface CompanyService {

    Collection<Company.Employee> getEmployees(Collection<Company> companies, String filter);

}
