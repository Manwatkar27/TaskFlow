package in.aman.tasks.controller;

import in.aman.tasks.enums.TaskStatus;
import in.aman.tasks.service.TaskService;
import in.aman.tasks.service.UserService;
import in.aman.tasks.TaskModel.Task;
import in.aman.tasks.TaskModel.UserDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private TaskService taskService;
    private UserService userService;

    @Autowired
    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    // ✅ Utility method to ensure Bearer prefix
    private String buildBearerToken(String jwt) throws Exception {
        if (jwt == null || jwt.isBlank()) {
            throw new Exception("JWT token required...");
        }
        return jwt.startsWith("Bearer ") ? jwt : "Bearer " + jwt;
    }

    // ✅ CREATE TASK
    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody Task task,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        String token = buildBearerToken(jwt);

        UserDTO user = userService.getUserProfileHandler(token);
        Task createdTask = taskService.create(task, user.getRole());

        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // ✅ GET TASK BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable String id,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        buildBearerToken(jwt); // validate presence only

        Task task = taskService.getTaskById(id);

        return task != null
                ? new ResponseEntity<>(task, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ✅ GET ASSIGNED USER TASKS
    @GetMapping("/user")
    public ResponseEntity<List<Task>> getAssignedUsersTask(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String sortByDeadline,
            @RequestParam(required = false) String sortByCreatedAt
    ) throws Exception {

        String token = buildBearerToken(jwt);

        UserDTO user = userService.getUserProfileHandler(token);

        List<Task> tasks =
                taskService.assignedUsersTask(
                        user.getId(), status, sortByDeadline, sortByCreatedAt);

        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ GET ALL TASKS
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String sortByDeadline,
            @RequestParam(required = false) String sortByCreatedAt
    ) throws Exception {

        buildBearerToken(jwt); // validate JWT

        List<Task> tasks =
                taskService.getAllTasks(status, sortByDeadline, sortByCreatedAt);

        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    // ✅ ASSIGN TASK TO USER
    @PutMapping("/{id}/user/{userId}/assigned")
    public ResponseEntity<Task> assignedTaskToUser(
            @PathVariable String id,
            @PathVariable String userId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        String token = buildBearerToken(jwt);

        userService.getUserProfileHandler(token);

        Task task = taskService.assignedToUser(userId, id);

        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    // ✅ UPDATE TASK
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String id,
            @RequestBody Task req,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        String token = buildBearerToken(jwt);

        UserDTO user = userService.getUserProfileHandler(token);

        Task task = taskService.updateTask(id, req, user.getId());

        return task != null
                ? new ResponseEntity<>(task, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ✅ DELETE TASK
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        try {
            taskService.deleteTask(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ✅ COMPLETE TASK
    @PutMapping("/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable String id) throws Exception {

        Task task = taskService.completeTask(id);

        return new ResponseEntity<>(task, HttpStatus.NO_CONTENT);
    }
}
