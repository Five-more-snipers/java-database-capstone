package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

/**
 * REST controller handling patient signup, login, profile lookup, and appointment filtering.
 */
@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return toObjectErrorResponse(tokenCheck);
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@Valid @RequestBody Patient patient) {
        if (!service.validatePatient(patient)) {
            return messageResponse(HttpStatus.CONFLICT, "Patient with email id or phone no already exist");
        }
        int created = patientService.createPatient(patient);
        if (created == 1) {
            return messageResponse(HttpStatus.CREATED, "Signup successful");
        }
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping({ "/{id}/{token}", "/{id}/{user}/{token}" })
    public ResponseEntity<Map<String, Object>> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable(required = false) String user,
            @PathVariable String token) {
        String role = (user != null && !user.isBlank()) ? user : "patient";
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, role);
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return toObjectErrorResponse(tokenCheck);
        }
        return patientService.getPatientAppointment(id, token);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "patient");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return toObjectErrorResponse(tokenCheck);
        }
        return service.filterPatient(condition, name, token);
    }

    private ResponseEntity<Map<String, String>> messageResponse(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<Map<String, Object>> toObjectErrorResponse(ResponseEntity<Map<String, String>> source) {
        Map<String, Object> body = new HashMap<>();
        if (source.getBody() != null) {
            body.putAll(source.getBody());
        }
        return ResponseEntity.status(source.getStatusCode()).body(body);
    }
}
