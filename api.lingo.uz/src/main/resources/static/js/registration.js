import { AppConfig } from './AppConfig.js';
document.getElementById("registrationForm")
    .addEventListener("submit", (event) => {
        event.preventDefault();

        const name = document.getElementById("name").value;
        const phoneEmail = document.getElementById("phoneEmail").value;
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;
        const errorTextTag = document.getElementById("errorText");
        const userSelectedLanguage = localStorage.getItem("current-lang") || "uz"

        if (password !== confirmPassword) {
            errorTextTag.textContent = "Mazgimiz nima balo?";
            errorTextTag.style.display = "block";
            document.getElementById("confirmPassword").style.borderColor = "red";
            document.getElementById("password").style.borderColor = "red";
            return;
        } else {
            errorTextTag.style.display = "none";
            document.getElementById("confirmPassword").style.borderColor = "#ddd";
            document.getElementById("password").style.borderColor = "#ddd";
        }
        //

        const body = {
            "name": name,
            "username": phoneEmail,
            "password": password
        }

        const lang = document.getElementById("current-lang").textContent;


        fetch(AppConfig.API+'/api/v1/auth/registration', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept-Language': userSelectedLanguage
            },
            body: JSON.stringify(body)
        }).then(response => {
            if (response.ok) {
                return response.json()
            } else {
                return Promise.reject(response.text())
            }
        }).then(item => {
            const phoneOrEmail = checkEmailOrPhone(phoneEmail)
            if(phoneOrEmail==='Email'){
                localStorage.setItem("registrationEmailMessage", item.data)
                window.location.href = "./registration-email-confirm.html";
            }else if(phoneOrEmail==='Phone'){
                localStorage.setItem("userPhoneNumber", phoneEmail)
                window.location.href = "./sms-confirm.html";
            }

        }).catch(error => {
            error.then(errMessage => {
                alert(errMessage)
            })
        })

    });


function checkEmailOrPhone(value) {
    // Regular expression for validating email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    // Regular expression for validating phone numbers
    // Adjust based on your requirements (e.g., country-specific formats)
    const phoneRegex = /^998\d{9}$/; // 998 91 572 1213

    if (emailRegex.test(value)) {
        return "Email";
    } else if (phoneRegex.test(value)) {
        return "Phone";
    } else {
        return "Invalid";
    }
}