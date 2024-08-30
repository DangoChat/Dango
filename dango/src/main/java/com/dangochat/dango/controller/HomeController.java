package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"", "/"})
    public String home() {
        return "home";
    }

    @GetMapping("/hyeonmin")
    public String hyeonmin() {
        return "hyeonmin";
    }
    
}
