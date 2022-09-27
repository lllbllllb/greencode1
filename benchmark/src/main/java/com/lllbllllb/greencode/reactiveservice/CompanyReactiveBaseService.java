package com.lllbllllb.greencode.reactiveservice;

import java.util.Collection;
import java.util.UUID;

import com.lllbllllb.greencode.Company;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CompanyReactiveBaseService implements CompanyReactiveService {

    @Override
    public Flux<Company.Employee> getEmployeesPublisher(Collection<Company> companies, String filter) {
        return Flux.fromIterable(companies)
            .filter(company -> !company.getName().contains(filter))
            .flatMap(company -> Flux.fromIterable(company.getStudios()))
            .filter(studio -> !studio.getName().contains(filter))
            .flatMap(studio -> Flux.fromIterable(studio.getDomains()))
            .filter(domain -> !domain.getName().contains(filter))
            .flatMap(domain -> Flux.fromIterable(domain.getEmployees()))
            .filter(employee -> !employee.getName().contains(filter));
    }

    @Override
    public Mono<Company.Employee> findEmployeeById(Collection<Company> companies, UUID uuid) {
        return Flux.fromIterable(companies)
            .flatMap(company -> Flux.fromIterable(company.getStudios()))
            .flatMap(studio -> Flux.fromIterable(studio.getDomains()))
            .flatMap(domain -> Flux.fromIterable(domain.getEmployees()))
            .filter(employee -> employee.getUuid().equals(uuid))
            .next();
    }

}
