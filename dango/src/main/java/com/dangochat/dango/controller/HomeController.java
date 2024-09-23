package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"", "/"})
    public String home() {
        return "home";              
    }
    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "OK";
    }

    @GetMapping("/hyeonmin")
    public String hyeonmin() {
        return "hyeonmin";
    }

    @GetMapping("leean")
    public String test() {
    	return"leean";
    }

    @GetMapping("honeybitterchip")
    public String honey() {
    	return "honeybitterchip";
    }

    @GetMapping("miynnn")
    public String miynnn() {
        return "miynnn";
    }
}
