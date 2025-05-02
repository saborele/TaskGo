let token = null;

async function login() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error("Error al iniciar sesión: " + response.status + " " + response.statusText + " - " + errorText);
        }

        token = await response.text();
        console.log("Token recibido:", token);
        alert("Inicio de sesión exitoso");
        document.getElementById("login-section").style.display = "none";
        document.getElementById("task-section").style.display = "block";
        loadTasks();
    } catch (error) {
        console.error("Error en login:", error);
        alert(error.message);
    }
}

async function loadTasks() {
    console.log("Enviando solicitud GET a /api/tareas con token:", token);
    try {
        const response = await fetch("/api/tareas", {
            headers: {
                "Authorization": "Bearer " + (token || "no-token")
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error("Error al cargar las tareas: " + response.status + " " + response.statusText + " - " + errorText);
        }

        const tasks = await response.json();
        const taskList = document.getElementById("task-list");
        taskList.innerHTML = "";

        tasks.forEach(task => {
            const taskDiv = document.createElement("div");
            taskDiv.className = "task";
            if (task.completada) {
                taskDiv.classList.add("completed");
            }
            taskDiv.innerHTML = `
                <span>${task.descripcion} (${task.fecha})</span>
                <button onclick="toggleTask(${task.id})">${task.completada ? "Desmarcar" : "Completar"}</button>
            `;
            taskList.appendChild(taskDiv);
        });
    } catch (error) {
        console.error("Error en loadTasks:", error);
        alert(error.message);
    }
}

async function createTask() {
    const description = document.getElementById("task-description").value;

    // Validar que la descripción no esté vacía o sea solo espacios
    if (!description || description.trim() === "") {
        alert("La descripción de la tarea no puede estar vacía");
        return;
    }

    const taskData = {
        descripcion: description.trim(), // Asegurarse de eliminar espacios innecesarios
        fecha: new Date().toISOString().split('T')[0], // Formato YYYY-MM-DD
        completada: false,
        favorita: false
    };

    console.log("Enviando solicitud POST a /api/tareas con token:", token);
    console.log("Datos enviados:", JSON.stringify(taskData));
    try {
        const response = await fetch("/api/tareas", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + (token || "no-token")
            },
            body: JSON.stringify(taskData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error("Error al crear la tarea: " + response.status + " " + response.statusText + " - " + errorText);
        }

        document.getElementById("task-description").value = "";
        loadTasks();
    } catch (error) {
        console.error("Error en createTask:", error);
        alert(error.message);
    }
}

async function toggleTask(id) {
    try {
        const response = await fetch(`/api/tareas/${id}/toggle`, {
            method: "PUT",
            headers: {
                "Authorization": "Bearer " + (token || "no-token")
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error("Error al cambiar el estado de la tarea: " + response.status + " " + response.statusText + " - " + errorText);
        }

        loadTasks();
    } catch (error) {
        console.error("Error en toggleTask:", error);
        alert(error.message);
    }
}