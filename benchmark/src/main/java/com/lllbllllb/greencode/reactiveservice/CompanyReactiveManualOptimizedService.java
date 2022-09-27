package com.lllbllllb.greencode.reactiveservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.lllbllllb.greencode.Company;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CompanyReactiveManualOptimizedService implements CompanyReactiveService {

    @Override
    public Flux<Company.Employee> getEmployeesPublisher(Collection<Company> companies, String filter) {
        return Flux.fromIterable(companies)
            .flatMap(company -> {
                var cs = new ArrayList<Company.Employee>();

                if (!company.getName().contains(filter)) {
                    for (var studio : company.getStudios()) {
                        if (!studio.getName().contains(filter)) {
                            for (var domain : studio.getDomains()) {
                                if (!domain.getName().contains(filter)) {
                                    for (var employee : domain.getEmployees()) {
                                        if (!employee.getName().contains(filter)) {
                                            cs.add(employee);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                return Flux.fromIterable(cs);
            });
    }

    @Override
    public Mono<Company.Employee> findEmployeeById(Collection<Company> companies, UUID uuid) {
        return Mono.just(companies)
            .handle((c, sink) -> {
                for (var company : c) {
                    for (var studio : company.getStudios()) {
                        for (var domain : studio.getDomains()) {
                            for (var employee : domain.getEmployees()) {
                                if (employee.getUuid().equals(uuid)) {
                                    sink.next(employee);
                                    return;
                                }
                            }
                        }
                    }
                }
            });
    }

}
