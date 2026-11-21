// header.js

function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) return;

    if (window.location.pathname.endsWith("/") || window.location.pathname.endsWith("/index.html")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        headerDiv.innerHTML = `
            <header class="header">
                <div class="logo-section logo-link">
                    <img src="/assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                    <span class="logo-title">Hospital CMS</span>
                </div>
            </header>`;
        return;
    }

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    let headerContent = `<header class="header">
        <div class="logo-section logo-link">
            <img src="/assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
            <span class="logo-title">Hospital CMS</span>
        </div>
        <nav>`;

    if (role === "admin") {
        headerContent += `
            <button id="addDocBtn" class="adminBtn" onclick="openModal('addDoctor')">Add Doctor</button>
            <a href="#" onclick="logout()">Logout</a>`;
    } else if (role === "doctor") {
        headerContent += `
            <button id="doctorHomeBtn" class="adminBtn" onclick="selectRole('doctor')">Home</button>
            <a href="#" onclick="logout()">Logout</a>`;
    } else if (role === "patient") {
        headerContent += `
            <button id="patientLogin" class="adminBtn">Login</button>
            <button id="patientSignup" class="adminBtn">Sign Up</button>`;
    } else if (role === "loggedPatient") {
        headerContent += `
            <button id="home" class="adminBtn" onclick="window.location.href='/pages/loggedPatientDashboard.html'">Home</button>
            <button id="patientAppointments" class="adminBtn" onclick="window.location.href='/pages/patientAppointments.html'">Appointments</button>
            <a href="#" onclick="logoutPatient()">Logout</a>`;
    }

    headerContent += `</nav></header>`;
    headerDiv.innerHTML = headerContent;
    attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
    const patientLoginBtn = document.getElementById("patientLogin");
    if (patientLoginBtn && typeof openModal === "function") {
        patientLoginBtn.addEventListener("click", () => openModal("patientLogin"));
    }

    const patientSignupBtn = document.getElementById("patientSignup");
    if (patientSignupBtn && typeof openModal === "function") {
        patientSignupBtn.addEventListener("click", () => openModal("patientSignup"));
    }

    const adminLoginBtn = document.getElementById("adminBtn");
    if (adminLoginBtn && typeof openModal === "function") {
        adminLoginBtn.addEventListener("click", () => openModal("adminLogin"));
    }

    const doctorLoginBtn = document.getElementById("doctorBtn");
    if (doctorLoginBtn && typeof openModal === "function") {
        doctorLoginBtn.addEventListener("click", () => openModal("doctorLogin"));
    }
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
}

function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
}

renderHeader();
