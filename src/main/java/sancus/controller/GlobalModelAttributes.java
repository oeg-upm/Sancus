package sancus.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import sancus.Keys;

/**
 * Injects global model attributes (like demoMode) into all Thymeleaf templates.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("demoMode")
    public boolean demoMode() {
        return Keys.isDemoMode();
    }
}
