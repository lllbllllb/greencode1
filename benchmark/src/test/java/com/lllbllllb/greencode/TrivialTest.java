package com.lllbllllb.greencode;

import com.lllbllllb.greencode.factory.CompanyFactoryFunctional;
import com.lllbllllb.greencode.factory.CompanyFactoryImperative;
import com.lllbllllb.greencode.service.CompanyServiceFunctional;
import com.lllbllllb.greencode.service.CompanyServiceImperative;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrivialTest {

    @Test
    void shouldProduceTheSameCompanies() {
        var count = 10;
        var studiosCount = 9;
        var domainCount = 8;
        var employeeCount = 7;
        var func = new CompanyFactoryFunctional();
        var impr = new CompanyFactoryImperative();

        assertEquals(
            func.getCompanies(count, studiosCount, domainCount, employeeCount),
            impr.getCompanies(count, studiosCount, domainCount, employeeCount)
        );
    }

    @Test
    void shouldProduceTheSameSimpleCompanies() {
        var count = 10;
        var func = new CompanyFactoryFunctional();
        var impr = new CompanyFactoryImperative();

        assertEquals(
            func.getSimpleCompanies(count),
            impr.getSimpleCompanies(count)
        );
    }

    @Test
    void shouldFlatAllTheSame() {
        var construct = new CompanyFactoryImperative();
        var collection = construct.getCompanies(10, 9, 8, 7);
        var func = new CompanyServiceFunctional();
        var impr = new CompanyServiceImperative();
        var filter = "-";

        assertEquals(func.getEmployees(collection, filter), impr.getEmployees(collection, filter));
    }
}
