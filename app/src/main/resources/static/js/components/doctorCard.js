// doctorCard.js

import { showBookingOverlay } from '../loggedPatient.js';
import { deleteDoctor } from '../services/doctorServices.js';
import { getPatientData } from '../services/patientServices.js';

export function createDoctorCard(doctor) {
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    const role = localStorage.getItem("userRole");

    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name;

    const specialization = document.createElement("p");
    specialization.textContent = doctor.specialty || doctor.specialization || "";

    const email = document.createElement("p");
    email.textContent = doctor.email;

    const availability = document.createElement("p");
    availability.textContent = Array.isArray(doctor.availableTimes)
        ? doctor.availableTimes.join(", ")
        : (doctor.availableTimes || "");

    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.addEventListener("click", async () => {
            const confirmed = confirm(`Are you sure you want to delete ${doctor.name}?`);
            if (!confirmed) return;

            const token = localStorage.getItem("token");
            if (!token) {
                alert("Unauthorized: Admin token not found.");
                return;
            }

            const result = await deleteDoctor(doctor.id, token);
            if (result && result.success) {
                alert(result.message || "Doctor deleted successfully.");
                card.remove();
            } else {
                alert(result?.message || "Failed to delete doctor.");
            }
        });
        actionsDiv.appendChild(removeBtn);
    } else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });
        actionsDiv.appendChild(bookNow);
    } else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.addEventListener("click", async (e) => {
            const token = localStorage.getItem("token");
            if (!token) {
                alert("Session expired. Please log in again.");
                window.location.href = "/";
                return;
            }
            const patientData = await getPatientData(token);
            if (patientData) {
                showBookingOverlay(e, doctor, patientData);
            } else {
                alert("Unable to fetch patient details. Please try again.");
            }
        });
        actionsDiv.appendChild(bookNow);
    }

    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}
