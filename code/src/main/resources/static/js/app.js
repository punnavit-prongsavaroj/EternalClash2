document.addEventListener("DOMContentLoaded", () => {
    checkLoginState();
});

// ฟังก์ชันตรวจสอบคุกกี้ (ใช้ LocalStorage แทนคุกกี้เพื่อให้จัดการง่ายในฝั่ง Client)
function checkLoginState() {
    const savedName = localStorage.getItem("eternalClashPlayerName");
    
    if (savedName && savedName.trim() !== "") {
        showLobby(savedName);
    } else {
        showLogin();
    }
}

// ฟังก์ชันตั้งชื่อและเข้าห้อง
function joinLobby() {
    const nameInput = document.getElementById("player-name-input").value.trim();
    
    if (nameInput === "") {
        alert("ท่านขุนพล! โปรดระบุชื่อของท่านก่อนเข้าสู่สนามรบ");
        return;
    }
    
    // จำชื่อลง LocalStorage
    localStorage.setItem("eternalClashPlayerName", nameInput);
    showLobby(nameInput);
}

// ฟังก์ชันออกจากระบบ
function logout() {
    localStorage.removeItem("eternalClashPlayerName");
    document.getElementById("player-name-input").value = "";
    showLogin();
}

// สลับแสดงหน้าล็อกอิน
function showLogin() {
    document.getElementById("login-screen").style.display = "flex";
    document.getElementById("lobby-screen").style.display = "none";
}

// สลับแสดงหน้าล็อบบี้
function showLobby(playerName) {
    document.getElementById("login-screen").style.display = "none";
    document.getElementById("lobby-screen").style.display = "flex";
    
    document.getElementById("display-name").innerText = playerName;
}
