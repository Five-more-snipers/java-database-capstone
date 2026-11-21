package com.project.back_end.services;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;

/**
 * Shared application service coordinating authentication, token checks, and cross-entity filtering.
 */
@org.springframework.stereotype.Service
public class Service {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(
            TokenService tokenService,
            AdminRepository adminRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> response = new HashMap<>();
        if (!tokenService.validateToken(token, user)) {
            response.put("message", "Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            if (admin == null || admin.getPassword() == null
                    || !admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("message", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            response.put("token", tokenService.generateToken(admin.getUsername()));
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        String normalizedName = normalizeParam(name);
        String normalizedSpecialty = normalizeParam(specialty);
        String normalizedTime = normalizeParam(time);

        if (normalizedName != null && normalizedSpecialty != null && normalizedTime != null) {
            return doctorService.filterDoctorsByNameSpecilityandTime(normalizedName, normalizedSpecialty, normalizedTime);
        }
        if (normalizedName != null && normalizedTime != null) {
            return doctorService.filterDoctorByNameAndTime(normalizedName, normalizedTime);
        }
        if (normalizedName != null && normalizedSpecialty != null) {
            return doctorService.filterDoctorByNameAndSpecility(normalizedName, normalizedSpecialty);
        }
        if (normalizedSpecialty != null && normalizedTime != null) {
            return doctorService.filterDoctorByTimeAndSpecility(normalizedSpecialty, normalizedTime);
        }
        if (normalizedName != null) {
            return doctorService.findDoctorByName(normalizedName);
        }
        if (normalizedSpecialty != null) {
            return doctorService.filterDoctorBySpecility(normalizedSpecialty);
        }
        if (normalizedTime != null) {
            return doctorService.filterDoctorsByTime(normalizedTime);
        }

        Map<String, Object> allDoctors = new HashMap<>();
        allDoctors.put("doctors", doctorService.getDoctors());
        return allDoctors;
    }

    public int validateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return -1;
        }
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
        if (doctor == null) {
            return -1;
        }
        if (appointment.getAppointmentTime() == null) {
            return 0;
        }
        List<String> availableSlots = doctorService.getDoctorAvailability(
                doctor.getId(),
                appointment.getAppointmentTime().toLocalDate());
        String requestedStart = appointment.getAppointmentTime().toLocalTime().format(TIME_FORMATTER);
        boolean matches = availableSlots.stream()
                .anyMatch(slot -> slot.split("-")[0].trim().equals(requestedStart));
        return matches ? 1 : 0;
    }

    public boolean validatePatient(Patient patient) {
        return patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) == null;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getIdentifier());
            if (patient == null || patient.getPassword() == null
                    || !patient.getPassword().equals(login.getPassword())) {
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            response.put("token", tokenService.generateToken(patient.getEmail()));
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        String email = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Patient not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String normalizedCondition = normalizeParam(condition);
        String normalizedName = normalizeParam(name);

        if (normalizedCondition != null && normalizedName != null) {
            return patientService.filterByDoctorAndCondition(normalizedCondition, normalizedName, patient.getId());
        }
        if (normalizedCondition != null) {
            return patientService.filterByCondition(normalizedCondition, patient.getId());
        }
        if (normalizedName != null) {
            return patientService.filterByDoctor(normalizedName, patient.getId());
        }
        return patientService.getPatientAppointment(patient.getId(), token);
    }

    private String normalizeParam(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
