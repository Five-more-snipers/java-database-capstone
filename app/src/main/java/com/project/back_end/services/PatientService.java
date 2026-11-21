package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

/**
 * Service handling patient registration, profile lookup, and appointment filtering.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        try {
            String email = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null || !patient.getId().equals(id)) {
                return errorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized access");
            }
            List<AppointmentDTO> dtos = toDtoList(appointmentRepository.findByPatientId(id));
            return appointmentsResponse(dtos);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching appointments");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        try {
            Integer status = parseConditionStatus(condition);
            if (status == null) {
                return errorResponse(HttpStatus.BAD_REQUEST, "Invalid condition");
            }
            List<AppointmentDTO> dtos = toDtoList(
                    appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status));
            return appointmentsResponse(dtos);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error filtering appointments");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        try {
            List<AppointmentDTO> dtos = toDtoList(
                    appointmentRepository.filterByDoctorNameAndPatientId(name, patientId));
            return appointmentsResponse(dtos);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error filtering appointments by doctor");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        try {
            Integer status = parseConditionStatus(condition);
            if (status == null) {
                return errorResponse(HttpStatus.BAD_REQUEST, "Invalid condition");
            }
            List<AppointmentDTO> dtos = toDtoList(
                    appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(name, patientId, status));
            return appointmentsResponse(dtos);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error filtering appointments");
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        try {
            String email = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                return errorResponse(HttpStatus.NOT_FOUND, "Patient not found");
            }
            Map<String, Object> body = new HashMap<>();
            body.put("patient", patient);
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching patient details");
        }
    }

    private Integer parseConditionStatus(String condition) {
        if ("past".equalsIgnoreCase(condition)) {
            return 1;
        }
        if ("future".equalsIgnoreCase(condition) || "upcoming".equalsIgnoreCase(condition)) {
            return 0;
        }
        return null;
    }

    private List<AppointmentDTO> toDtoList(List<Appointment> appointments) {
        return appointments.stream()
                .map(a -> new AppointmentDTO(
                        a.getId(),
                        a.getDoctor().getId(),
                        a.getDoctor().getName(),
                        a.getPatient().getId(),
                        a.getPatient().getName(),
                        a.getPatient().getEmail(),
                        a.getPatient().getPhone(),
                        a.getPatient().getAddress(),
                        a.getAppointmentTime(),
                        a.getStatus()))
                .collect(Collectors.toList());
    }

    private ResponseEntity<Map<String, Object>> appointmentsResponse(List<AppointmentDTO> dtos) {
        Map<String, Object> body = new HashMap<>();
        body.put("appointments", dtos);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
