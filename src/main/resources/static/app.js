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
            // Asegurarse de que completada esté definido
            tasks = tasks.map(task => ({
                ...task,
                completada: task.completada !== undefined ? task.completada : false
            }));

            // Actualizar el contador
            updateTaskCount(tasks);

            // Filtrar tareas según el filtro seleccionado
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
            taskList.innerHTML = ''; // Limpiar la lista
            tasks.forEach(task => {
                const li = document.createElement('li');
                li.className = 'list-group-item task-item';
                li.innerHTML = `
                    <span class="${task.completada ? 'task-completed' : ''}">
                        ${task.descripcion} (Creada: ${task.fecha})
                    </span>
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

            // Añadir eventos a los botones de editar
            document.querySelectorAll('.edit-task').forEach(button => {
                button.addEventListener('click', (event) => {
                    const id = event.target.getAttribute('data-id');
                    const taskItem = event.target.closest('.task-item');
                    const taskSpan = taskItem.querySelector('span');
                    const currentDescription = taskSpan.innerText.split(' (Creada:')[0];

                    // Crear un formulario de edición
                    const editForm = document.createElement('form');
                    editForm.className = 'edit-form d-flex align-items-center';
                    editForm.innerHTML = `
                        <input type="text" class="form-control me-2" value="${currentDescription}" required>
                        <button type="submit" class="btn btn-success btn-sm me-1">Guardar</button>
                        <button type="button" class="btn btn-secondary btn-sm cancel-edit">Cancelar</button>
                    `;

                    // Reemplazar el contenido del task-item con el formulario
                    taskItem.innerHTML = '';
                    taskItem.appendChild(editForm);

                    // Manejar el envío del formulario
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
                                loadTasks(document.getElementById('task-filter').value); // Recargar con el filtro actual
                            } else {
                                updateProgress(`Error al actualizar la tarea: ${response.status} ${response.statusText}`, true);
                                loadTasks(document.getElementById('task-filter').value); // Recargar para restaurar
                            }
                        } catch (error) {
                            updateProgress(`Error de conexión: ${error.message}`, true);
                            loadTasks(document.getElementById('task-filter').value); // Recargar para restaurar
                        }
                    });

                    // Manejar la cancelación
                    editForm.querySelector('.cancel-edit').addEventListener('click', () => {
                        loadTasks(document.getElementById('task-filter').value); // Recargar para restaurar
                    });
                });
            });

            // Añadir eventos a los botones de alternar
            document.querySelectorAll('.toggle-task').forEach(button => {
                button.addEventListener('click', async (event) => {
                    const id = event.target.getAttribute('data-id');
                    try {
                        const response = await fetch(`http://localhost:8080/api/tareas/${id}/toggle`, {
                            method: 'PUT'
                        });
                        if (response.ok) {
                            updateProgress(`Estado de la tarea con ID ${id} cambiado`);
                            loadTasks(document.getElementById('task-filter').value); // Recargar con el filtro actual
                        } else {
                            updateProgress(`Error al cambiar el estado de la tarea: ${response.status} ${response.statusText}`, true);
                        }
                    } catch (error) {
                        updateProgress(`Error de conexión: ${error.message}`, true);
                    }
                });
            });

            // Añadir eventos a los botones de eliminación
            document.querySelectorAll('.delete-task').forEach(button => {
                button.addEventListener('click', async (event) => {
                    const id = event.target.getAttribute('data-id');
                    try {
                        const response = await fetch(`http://localhost:8080/api/tareas/${id}`, {
                            method: 'DELETE'
                        });
                        if (response.ok) {
                            updateProgress(`Tarea con ID ${id} eliminada`);
                            loadTasks(document.getElementById('task-filter').value); // Recargar con el filtro actual
                        } else {
                            updateProgress(`Error al eliminar tarea: ${response.status} ${response.statusText}`, true);
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

// Esperar a que el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', () => {
    // Cargar tareas al iniciar
    loadTasks();

    // Añadir evento al filtro
    const taskFilter = document.getElementById('task-filter');
    if (taskFilter) {
        taskFilter.addEventListener('change', (event) => {
            loadTasks(event.target.value);
        });
    } else {
        console.error("Elemento con ID 'task-filter' no encontrado en el DOM.");
    }

    // Añadir evento al botón de completar todas
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
                    loadTasks(document.getElementById('task-filter').value); // Recargar con el filtro actual
                } else {
                    updateProgress(`Error al completar todas las tareas: ${response.status} ${response.statusText}`, true);
                }
            } catch (error) {
                updateProgress(`Error de conexión: ${error.message}`, true);
            }
        });
    } else {
        console.error("Elemento con ID 'complete-all' no encontrado en el DOM.");
    }

    // Añadir evento al botón de limpiar completadas
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
                    loadTasks(document.getElementById('task-filter').value); // Recargar con el filtro actual
                } else {
                    updateProgress(`Error al limpiar tareas completadas: ${response.status} ${response.statusText}`, true);
                }
            } catch (error) {
                updateProgress(`Error de conexión: ${error.message}`, true);
            }
        });
    } else {
        console.error("Elemento con ID 'clear-completed' no encontrado en el DOM.");
    }

    // Manejar el formulario de tareas
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
                    loadTasks(document.getElementById('task-filter').value); // Recargar con el filtro actual
                } else {
                    updateProgress(`Error al añadir tarea: ${response.status} ${response.statusText}`, true);
                }
            } catch (error) {
                updateProgress(`Error de conexión: ${error.message}`, true);
            }
        });
    } else {
        console.error("Elemento con ID 'task-form' no encontrado en el DOM.");
    }

    // Manejar el formulario de navegación
    const navigateForm = document.getElementById('navigate-form');
    if (navigateForm) {
        navigateForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            const url = document.getElementById('navigate-url').value.trim();
            const query = document.getElementById('navigate-query').value.trim();

            if (!url || !query) {
                updateProgress('Por favor, ingresa una URL y un término de búsqueda', true);
                return;
            }

            updateProgress('Iniciando navegación...');
            try {
                const response = await fetch(`http://localhost:8080/api/navegar?url=${encodeURIComponent(url)}&query=${encodeURIComponent(query)}`, {
                    method: 'GET'
                });

                if (response.ok) {
                    const result = await response.text();
                    if (result.startsWith('USER_INTERVENTION_REQUIRED:')) {
                        // Extraer el mensaje y el sessionId
                        const parts = result.split('|SESSION_ID:');
                        const message = parts[0].replace('USER_INTERVENTION_REQUIRED:', '');
                        const sessionId = parts.length > 1 ? parts[1] : null;

                        updateProgress(message, true);

                        // Mostrar un botón para que el usuario indique que ha resuelto el problema
                        const progressArea = document.getElementById('progress-area');
                        const continueButton = document.createElement('button');
                        continueButton.className = 'btn btn-primary mt-2';
                        continueButton.innerText = message.includes('No se encontró un campo de búsqueda')
                            ? 'He ingresado el término de búsqueda manualmente, continuar'
                            : 'He resuelto el pop-up/CAPTCHA, continuar';
                        continueButton.addEventListener('click', async () => {
                            updateProgress('Continuando navegación...');
                            try {
                                // Reintentar la navegación con el sessionId
                                const retryResponse = await fetch(`http://localhost:8080/api/navegar?url=${encodeURIComponent(url)}&query=${encodeURIComponent(query)}&sessionId=${sessionId}&continueAfterIntervention=true`, {
                                    method: 'GET'
                                });
                                if (retryResponse.ok) {
                                    const retryResult = await retryResponse.text();
                                    if (retryResult.startsWith('USER_INTERVENTION_REQUIRED:')) {
                                        const newParts = retryResult.split('|SESSION_ID:');
                                        const newMessage = newParts[0].replace('USER_INTERVENTION_REQUIRED:', '');
                                        updateProgress(newMessage, true);
                                    } else {
                                        updateProgress(retryResult || 'Navegación completada');
                                    }
                                } else {
                                    updateProgress(`Error al continuar navegación: ${retryResponse.status} ${retryResponse.statusText}`, true);
                                }
                            } catch (error) {
                                updateProgress(`Error de conexión: ${error.message}`, true);
                            }
                            continueButton.remove(); // Eliminar el botón después de continuar
                        });
                        progressArea.appendChild(continueButton);
                    } else {
                        updateProgress(result || 'Navegación completada');
                    }
                } else {
                    updateProgress(`Error al navegar: ${response.status} ${response.statusText}`, true);
                }
            } catch (error) {
                updateProgress(`Error de conexión: ${error.message}`, true);
            }
        });
    } else {
        console.error("Elemento con ID 'navigate-form' no encontrado en el DOM.");
    }
});