package com.lllbllllb.greencode;

import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder
@RequiredArgsConstructor
public class Company {

    UUID uuid;

    String name;

    Set<Studio> studios;

    // ---- ---- ----

    @Value
    @Builder
    @RequiredArgsConstructor
    public static class Studio {

        UUID uuid;

        String name;

        Set<Domain> domains;

    }

    @Value
    @Builder
    @RequiredArgsConstructor
    public static class Domain {

        UUID uuid;

        String name;

        Set<Employee> employees;

    }

    @Value
    @Builder
    @RequiredArgsConstructor
    public static class Employee {

        UUID uuid;

        String name;

        int age;

        JobTitle jobTitle;

        Level level;

    }

    public enum JobTitle {
        SWE,
        QA,
        MANAGER
    }

    public enum Level {
        JUN,
        MID,
        SEN
    }

}
