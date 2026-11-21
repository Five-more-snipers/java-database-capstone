package com.project.back_end.mvc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

/**
 * MVC controller that serves role-specific Thymeleaf dashboard views
 * after validating the caller's token via {@link Service}.
 */
@Controller
public class DashboardController {

    @Autowired
    private Service service;

    /**
     * Renders the Admin Dashboard view if the provided token is valid for an admin.
     */
    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        return resolveDashboardView(token, "admin", "admin/adminDashboard");
    }

    /**
     * Renders the Doctor Dashboard view if the provided token is valid for a doctor.
     */
    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        return resolveDashboardView(token, "doctor", "doctor/doctorDashboard");
    }

    private String resolveDashboardView(String token, String role, String viewName) {
        ResponseEntity<Map<String, String>> response = service.validateToken(token, role);
        if (response != null && response.getBody() != null && response.getBody().isEmpty()) {
            return viewName;
        }
        return "redirect:/";
    }
}
