package com.inversion.service.impl;

import com.inversion.annotation.Bean;

@Bean
public class IncludedServiceImpl implements IncludedService {
    @Override
    public void print() {
        System.out.println("test from included");
    }
}
