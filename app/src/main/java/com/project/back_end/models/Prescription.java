package com.project.back_end.models;

@Document(collection = "prescriptions")
public class Prescription {
  @Id
  private String id;

  @NotNull
  @Size(min = 3, max = 100)
  private String patientName;

  @NotNull
  private Long appointmentId;

  @NotNull
  @Size(min = 3, max = 100)
  private String medication;

  @NotNull
  @Size(min = 3, max = 20)
  private String dosage;

  @Size(max = 200)
  private String doctorNotes;
}