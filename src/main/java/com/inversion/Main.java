package com.inversion;

import com.inversion.ioc.Injector;
import com.inversion.service.TestService;

public class Main {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        Injector.startup(Main.class);
        final var testService = Injector.getBean(TestService.class);

        testService.print();
    }
}