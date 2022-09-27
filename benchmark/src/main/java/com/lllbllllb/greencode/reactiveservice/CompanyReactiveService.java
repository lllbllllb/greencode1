package com.lllbllllb.greencode.reactiveservice;

import java.util.Collection;
import java.util.UUID;

import com.lllbllllb.greencode.Company;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CompanyReactiveService {

    Flux<Company.Employee> getEmployeesPublisher(Collection<Company> companies, String filter);

    Mono<Company.Employee> findEmployeeById(Collection<Company> companies, UUID uuid);

}
