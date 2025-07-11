import AppConfig from "./AppConfig.js";
function resetPassword() {
    const usernameInput = document.getElementById("username");
    const username = usernameInput.value;
    if (!username) {
        return;
    }

    const body = {
        "username": username
    }

    const lang = document.getElementById("current-lang").textContent;

    fetch(AppConfig.API+'/api/v1/auth/password-reset', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept-Language': lang
        },
        body: JSON.stringify(body)
    }).then(response => {
        if (response.ok) {
            return response.json()
        } else {
            return Promise.reject(response.text())
        }
    }).then(item => {
        alert(item.data)
        localStorage.setItem("username", username)
        window.location.href = "./reset-password-confirm.html";
        usernameInput.textContent="";

    }).catch(error => {
        error.then(errMessage => {
            alert(errMessage)
        })
    })
}