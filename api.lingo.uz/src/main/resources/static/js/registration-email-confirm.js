 const element = document.getElementById("registrationConfirmPageId");
element.textContent = localStorage.getItem("registrationEmailMessage");
localStorage.removeItem("registrationEmailMessage")

