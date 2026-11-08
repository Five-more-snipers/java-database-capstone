@Entity
public class Appointment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @NotNull
  private Doctor doctor;

  @ManyToOne
  @NotNull
  private Patient patient;

  @Future(message = "Appointment time must be in the future")
  private LocalDateTime appointmentTime;

  private int status; // 0 = Scheduled, 1 = Completed

  @Transient
  public LocalDateTime getEndTime() {
    return appointmentTime.plusHours(1);
  }
}