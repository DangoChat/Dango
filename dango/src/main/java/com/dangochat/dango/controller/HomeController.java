package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"", "/"})
    public String home() {
        return "home";              
    }
<<<<<<< HEAD
<<<<<<< HEAD

    @GetMapping("/hyeonmin")
    public String hyeonmin() {
        return "hyeonmin";
    }
    
=======
    
    
    @GetMapping("leean")
    public String test() {
    	return"leean";
    }
>>>>>>> bba1cc6e4e7c07f356b8faca53203b6927a4cc18
=======
    @GetMapping("honeybitterchip")
    public String honey() {
    	return "honeybitterchip";
    }


    @GetMapping("miynnn")
    public String miynnn() {
        return "miynnn";
    }

>>>>>>> 0410472aa74a43a2db53ddcafef4de3e9bd2acac
}
