package com.example.greeting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    public record Greeting(String message) {
    }

    /**
     * GET /greeting            -> {"message":"Hola, Mundo!"}
     * GET /greeting?name=Ana   -> {"message":"Hola, Ana!"}
     */
    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(name = "name", required = false, defaultValue = "Mundo") String name) {
        String safeName = name.isBlank() ? "Mundo" : name.trim();
        return new Greeting("Hola, " + safeName + "!");
    }
}
