package todolist

import com.fasterxml.jackson.databind.ObjectMapper
import spark.Request
import spark.Route
import spark.Spark.halt

class TaskController(
        private val objectMapper: ObjectMapper,
        private val taskRepository: TaskRepository) {

    private val Request.task: Task?
        get() {
            val id = params("id").toLongOrNull()
            return id?.let(taskRepository::findById)
        }

    fun index(): Route = Route { _, _ ->
        taskRepository.findAll()
    }

    fun create(): Route = Route { request, response ->
        val taskCreateRequest: TaskCreateRequest =
                objectMapper.readValue(request.bodyAsBytes()) ?: throw halt(400)
        val task = taskRepository.create(taskCreateRequest.content)
        response.status(201)
        task
    }

    fun show(): Route = Route { request, _ ->
        request.task ?: throw halt(404)
    }

    fun update(): Route = Route { request, response ->
        val taskUpdateRequest: TaskUpdateRequest =
                objectMapper.readValue(request.bodyAsBytes()) ?: throw halt(400)
        val task = request.task ?: throw halt(404)
        val newTask = task.copy(
                content = taskUpdateRequest.content ?: task.content,
                done = taskUpdateRequest.done ?: task.done)
        taskRepository.update(newTask)
        response.status(204)
    }

    fun destroy(): Route = Route { request, response ->
        val task = request.task ?: throw halt(404)
        taskRepository.delete(task)
        response.status(204)
    }
}