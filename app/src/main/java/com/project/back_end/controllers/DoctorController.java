package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;

import jakarta.validation.Valid;

/**
 * REST controller managing doctor CRUD, login, availability, and filtering endpoints.
 */
@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, user);
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return toObjectErrorResponse(tokenCheck);
        }
        List<String> availability = doctorService.getDoctorAvailability(doctorId, LocalDate.parse(date));
        Map<String, Object> body = new HashMap<>();
        body.put("availability", availability);
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> body = new HashMap<>();
        body.put("doctors", doctorService.getDoctors());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "admin");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }
        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            return messageResponse(HttpStatus.CREATED, "Doctor added to db");
        }
        if (result == -1) {
            return messageResponse(HttpStatus.CONFLICT, "Doctor already exists");
        }
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Some internal error occurred");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(
            @Valid @RequestBody Doctor doctor,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "admin");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }
        int result = doctorService.updateDoctor(doctor);
        if (result == 1) {
            return messageResponse(HttpStatus.OK, "Doctor updated");
        }
        if (result == -1) {
            return messageResponse(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Some internal error occurred");
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(
            @PathVariable long id,
            @PathVariable String token) {
        ResponseEntity<Map<String, String>> tokenCheck = service.validateToken(token, "admin");
        if (!tokenCheck.getStatusCode().is2xxSuccessful()) {
            return tokenCheck;
        }
        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            return messageResponse(HttpStatus.OK, "Doctor deleted successfully");
        }
        if (result == -1) {
            return messageResponse(HttpStatus.NOT_FOUND, "Doctor not found with id");
        }
        return messageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Some internal error occurred");
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {
        return ResponseEntity.ok(service.filterDoctor(name, speciality, time));
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
