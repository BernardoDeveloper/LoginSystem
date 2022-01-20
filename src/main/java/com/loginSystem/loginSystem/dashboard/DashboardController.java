package com.loginSystem.loginSystem.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class DashboardController {

    @GetMapping
    public String index() {
        return "Hello World";
    }
}
