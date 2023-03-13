package com.inversion.service.impl;

import com.inversion.annotation.Bean;
import com.inversion.annotation.Inject;
import com.inversion.annotation.Qualifier;
import com.inversion.service.TestService;

@Bean
public class TestServiceImpl implements TestService {
    @Inject
    @Qualifier("includedServiceImpl")
    private IncludedService includedService;
    @Override
    public void print() {
        System.out.println("test");
        includedService.print();
    }
}
