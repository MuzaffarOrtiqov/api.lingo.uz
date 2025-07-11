import { AppConfig } from './AppConfig.js';
document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("loginBtn").addEventListener("click", login);
});
function login() {
    const usernameInput = document.getElementById("username");
    const username = usernameInput.value;

    const passwordInput = document.getElementById("password");
    const password = passwordInput.value;

    let hasError = false;
    if (username === null || username === 'undefined' || username.length === 0) {
        document.getElementById("usernameErrorSpan").style.display = 'block';
      //  document.getElementById("entrance_input").style.borderColor = 'red';
        hasError = true;
    }

    if (password === null || password === 'undefined' || password.length === 0) {
        passwordInput.nextElementSibling.style.display = 'block';
       // passwordInput.nextElementSibling.style.borderColor='red'
        hasError = true;
    }

    if (hasError) {
        return;
    }
    document.getElementById("usernameErrorSpan").style.display = 'none';
    passwordInput.nextElementSibling.style.display = 'none';

    const body = {
        "username": username,
        "password": password
    }

    const lang = document.getElementById("current-lang").textContent;

    fetch(AppConfig.API+'/api/v1/auth/login', {
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

        console.log(item)
        localStorage.setItem("userDetail", JSON.stringify(item))
        localStorage.setItem("jwtToken",item.jwt)
        window.location.href = "./index.html";

        passwordInput.value = '';
        usernameInput.value = '';

    }).catch(error => {
        error.then(errMessage => {
            alert(errMessage)
        })
    })
}