package com.voilandPantry.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CardReaderController {

    @GetMapping("/card-reader")
    public String cardReaderPage() {
        return "card_reader"; // maps to card_reader.html in templates
    }
}