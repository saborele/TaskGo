// Función para actualizar el mensaje de progreso
function updateProgress(message, isError = false) {
    const progressMessage = document.getElementById('progress-message');
    if (!progressMessage) {
        console.error("Elemento con ID 'progress-message' no encontrado en el DOM.");
        return;
    }
    progressMessage.innerText = message;
    progressMessage.className = 'card-text ' + (isError ? 'text-danger' : 'text-success');
}

// Función para actualizar el contador de tareas
function updateTaskCount(tasks) {
    const pendingCountElement = document.getElementById('pending-count');
    const completedCountElement = document.getElementById('completed-count');
    if (!pendingCountElement || !completedCountElement) {
        console.error("Elementos de contador no encontrados en el DOM.");
        return;
    }
    const pendingCount = tasks.filter(task => !task.completada).length;
    const completedCount = tasks.filter(task => task.completada).length;
    pendingCountElement.innerText = pendingCount;
    completedCountElement.innerText = completedCount;
}

// Función para cargar y mostrar las tareas
async function loadTasks(filter = 'all') {
    try {
        const response = await fetch('http://localhost:8080/api/tareas');
        if (response.ok) {
            let tasks = await response.json();
            tasks = tasks.map(task => ({
                ...task,
                completada: task.completada !== undefined ? task.completada : false,
                favorita: task.favorita !== undefined ? task.favorita : false
            }));

            updateTaskCount(tasks);

            if (filter === 'pending') {
                tasks = tasks.filter(task => !task.completada);
            } else if (filter === 'completed') {
                tasks = tasks.filter(task => task.completada);
            }

            const taskList = document.getElementById('task-list');
            if (!taskList) {
                console.error("Elemento con ID 'task-list' no encontrado en el DOM.");
                return;
            }
            taskList.innerHTML = '';
            tasks.forEach(task => {
                const li = document.createElement('li');
                li.className = 'list-group-item task-item';
                li.innerHTML = `
                    <div class="d-flex align-items-center">
                        <i class="bi bi-star-fill favorite-star me-2 ${task.favorita ? 'favorita' : ''}" data-id="${task.id}"></i>
                        <span class="${task.completada ? 'task-completed' : ''}">
                            ${task.descripcion} (Creada: ${task.fecha})
                        </span>
                    </div>
                    <div>
                        <button class="btn btn-primary btn-sm edit-task me-1" data-id="${task.id}">Editar</button>
                        <button class="btn btn-warning btn-sm toggle-task me-1" data-id="${task.id}">
                            ${task.completada ? 'Desmarcar' : 'Completar'}
                        </button>
                        <button class="btn btn-danger btn-sm delete-task" data-id="${task.id}">Eliminar</button>
                    </div>
                `;
                taskList.appendChild(li);
            });

            document.querySelectorAll('.edit-task').forEach(button => {
                button.addEventListener('click', (event) => {
                    const id = event.target.getAttribute('data-id');
                    const taskItem = event.target.closest('.task-item');
                    const taskSpan = taskItem.querySelector('span');
                    const currentDescription = taskSpan.innerText.split(' (Creada:')[0];

                    const editForm = document.createElement('form');
                    editForm.className = 'edit-form d-flex align-items-center';
                    editForm.innerHTML = `
                        <input type="text" class="form-control me-2" value="${currentDescription}" required>
                        <button type="submit" class="btn btn-success btn-sm me-1">Guardar</button>
                        <button type="button" class="btn btn-secondary btn-sm cancel-edit">Cancelar</button>
                    `;

                    taskItem.innerHTML = '';
                    taskItem.appendChild(editForm);

                    editForm.addEventListener('submit', async (e) => {
                        e.preventDefault();
                        const newDescription = editForm.querySelector('input').value.trim();
                        if (!newDescription) {
                            updateProgress('La descripción no puede estar vacía', true);
                            return;
                        }

                        try {
                            const response = await fetch(`http://localhost:8080/api/tareas/${id}`, {
                                method: 'PUT',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ descripcion: newDescription })
                            });
                            if (response.ok) {
                                updateProgress(`Tarea con ID ${id} actualizada`);
                                loadTasks(document.getElementById('task-filter').value);
                            } else {
                                updateProgress(`Error al actualizar la tarea: ${response.status} ${response.statusText}`, true);
                                loadTasks(document.getElementById('task-filter').value);
                            }
                        } catch (error) {
                            updateProgress(`Error de conexión: ${error.message}`, true);
                            loadTasks(document.getElementById('task-filter').value);
                        }
                    });

                    editForm.querySelector('.cancel-edit').addEventListener('click', () => {
                        loadTasks(document.getElementById('task-filter').value);
                    });
                });
            });

            document.querySelectorAll('.toggle-task').forEach(button => {
                button.addEventListener('click', async (event) => {
                    const id = event.target.getAttribute('data-id');
                    try {
                        const response = await fetch(`http://localhost:8080/api/tareas/${id}/toggle`, {
                            method: 'PUT'
                        });
                        if (response.ok) {
                            updateProgress(`Estado de la tarea con ID ${id} cambiado`);
                            loadTasks(document.getElementById('task-filter').value);
                        } else {
                            updateProgress(`Error al cambiar el estado de la tarea: ${response.status} ${response.statusText}`, true);
                        }
                    } catch (error) {
                        updateProgress(`Error de conexión: ${error.message}`, true);
                    }
                });
            });

            document.querySelectorAll('.delete-task').forEach(button => {
                button.addEventListener('click', async (event) => {
                    const id = event.target.getAttribute('data-id');
                    try {
                        const response = await fetch(`http://localhost:8080/api/tareas/${id}`, {
                            method: 'DELETE'
                        });
                        if (response.ok) {
                            updateProgress(`Tarea con ID ${id} eliminada`);
                            loadTasks(document.getElementById('task-filter').value);
                        } else {
                            updateProgress(`Error al eliminar tarea: ${response.status} ${response.statusText}`, true);
                        }
                    } catch (error) {
                        updateProgress(`Error de conexión: ${error.message}`, true);
                    }
                });
            });

            document.querySelectorAll('.favorite-star').forEach(star => {
                star.addEventListener('click', async (event) => {
                    const id = event.target.getAttribute('data-id');
                    try {
                        const response = await fetch(`http://localhost:8080/api/tareas/${id}/favorita`, {
                            method: 'PUT'
                        });
                        if (response.ok) {
                            updateProgress(`Tarea con ID ${id} marcada/desmarcada como favorita`);
                            loadTasks(document.getElementById('task-filter').value);
                        } else {
                            updateProgress(`Error al cambiar el estado de favorita: ${response.status} ${response.statusText}`, true);
                        }
                    } catch (error) {
                        updateProgress(`Error de conexión: ${error.message}`, true);
                    }
                });
            });
        } else {
            updateProgress('Error al cargar tareas', true);
        }
    } catch (error) {
        updateProgress(`Error de conexión: ${error.message}`, true);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadTasks();

    const taskFilter = document.getElementById('task-filter');
    if (taskFilter) {
        taskFilter.addEventListener('change', (event) => {
            loadTasks(event.target.value);
        });
    }

    const completeAllButton = document.getElementById('complete-all');
    if (completeAllButton) {
        completeAllButton.addEventListener('click', async () => {
            try {
                const response = await fetch('http://localhost:8080/api/tareas/completar-todas', {
                    method: 'PUT'
                });
                if (response.ok) {
                    const result = await response.text();
                    updateProgress(result);
                    loadTasks(document.getElementById('task-filter').value);
                } else {
                    updateProgress(`Error al completar todas las tareas: ${response.status} ${response.statusText}`, true);
                }
            } catch (error) {
                updateProgress(`Error de conexión: ${error.message}`, true);
            }
        });
    }

    const clearCompletedButton = document.getElementById('clear-completed');
    if (clearCompletedButton) {
        clearCompletedButton.addEventListener('click', async () => {
            try {
                const response = await fetch('http://localhost:8080/api/tareas/completadas', {
                    method: 'DELETE'
                });
                if (response.ok) {
                    const result = await response.text();
                    updateProgress(result);
                    loadTasks(document.getElementById('task-filter').value);
                } else {
                    updateProgress(`Error al limpiar tareas completadas: ${response.status} ${response.statusText}`, true);
                }
            } catch (error) {
                updateProgress(`Error de conexión: ${error.message}`, true);
            }
        });
    }

    const taskForm = document.getElementById('task-form');
    if (taskForm) {
        taskForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const description = document.getElementById('task-description').value.trim();

            if (!description) {
                updateProgress('Por favor, escribe una tarea válida', true);
                return;
            }

            updateProgress('Añadiendo tarea...');
            try {
                const response = await fetch('http://localhost:8080/api/tareas', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ descripcion: description })
                });

                if (response.ok) {
                    const task = await response.json();
                    updateProgress(`Tarea "${task.descripcion}" añadida (ID: ${task.id})`);
                    document.getElementById('task-description').value = '';
                    loadTasks(document.getElementById('task-filter').value);
                } else {
                    updateProgress(`Error al añadir tarea: ${response.status} ${response.statusText}`, true);
                }
            } catch (error) {
                updateProgress(`Error de conexión: ${error.message}`, true);
            }
        });
    }

    const commandForm = document.getElementById('command-form');
    if (commandForm) {
        commandForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const command = document.getElementById('command-input').value.trim();

            if (!command) {
                updateProgress('Por favor, ingresa un comando válido', true);
                return;
            }

            updateProgress('Ejecutando comando...');
            try {
                const response = await fetch('http://localhost:8080/api/execute', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(command)
                });

                if (response.ok) {
                    const result = await response.text();
                    updateProgress(result);
                } else {
                    updateProgress(`Error al ejecutar comando: ${response.status} ${response.statusText}`, true);
                }
            } catch (error) {
                updateProgress(`Error de conexión: ${error.message}`, true);
            }
        });
    }
});