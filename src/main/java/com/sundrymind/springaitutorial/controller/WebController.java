// src/main/java/com/example/springaitutorial/controller/WebController.java
package com.sundrymind.springaitutorial.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving web pages
 * Uses Thymeleaf template engine to render HTML pages
 */
@Controller
public class WebController {

    /**
     * Serve the main chat interface
     * GET /
     *
     * @param model Spring's Model object for passing data to templates
     * @return Template name (maps to src/main/resources/templates/index.html)
     */
    @GetMapping("/")
    public String index(Model model) {
        // Add any model attributes needed by the template
        model.addAttribute("appName", "Spring AI Chat Demo");
        model.addAttribute("version", "1.0.0");

        // Return template name (without .html extension)
        return "index";
    }

    /**
     * Alternative endpoint for the chat interface
     * GET /chat
     */
    @GetMapping("/chat")
    public String chat() {
        return "index";
    }
}