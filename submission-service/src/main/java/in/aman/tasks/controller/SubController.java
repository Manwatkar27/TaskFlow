package in.aman.tasks.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.aman.tasks.service.SubmissionService;
import in.aman.tasks.service.TaskService;
import in.aman.tasks.service.UserService;
import in.aman.tasks.submissionModel.TaskSubmission;
import in.aman.tasks.submissionModel.UserDTO;

@RestController
@RequestMapping("/api/submissions")
public class SubController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private UserService userService;

    // ✅ Utility method to ensure correct Bearer format
    private String buildBearerToken(String jwt) throws Exception {
        if (jwt == null || jwt.isBlank()) {
            throw new Exception("JWT token required");
        }
        return jwt.startsWith("Bearer ") ? jwt : "Bearer " + jwt;
    }

    // ✅ SUBMIT TASK
    @PostMapping
    public ResponseEntity<TaskSubmission> submitTask(
            @RequestParam String task_id,
            @RequestParam String github_link,
            @RequestHeader("Authorization") String jwt) throws Exception {

        String token = buildBearerToken(jwt);

        // ✅ Call Feign with valid token
        UserDTO user = userService.getUserProfileHandler(token);

        TaskSubmission sub =
                submissionService.submitTask(
                        task_id,
                        github_link,
                        user.getId(),
                        token);
        
        return new ResponseEntity<>(sub, HttpStatus.CREATED);
    }

    // ✅ GET SUBMISSION BY ID
    @GetMapping("/{id}")
    public ResponseEntity<TaskSubmission> getTaskSubmissionById(@PathVariable String id) throws Exception {

        TaskSubmission sub = submissionService.getTaskSubmissionById(id);

        return new ResponseEntity<>(sub, HttpStatus.OK);
    }

    // ✅ GET ALL SUBMISSIONS
    @GetMapping
    public ResponseEntity<List<TaskSubmission>> getAllTaskSubmissions() throws Exception {

        List<TaskSubmission> submissions =
                submissionService.getAllTaskSubmissions();

        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    // ✅ GET SUBMISSIONS BY TASK ID
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskSubmission>> getTaskSubmissionsByTaskId(
            @PathVariable String taskId) throws Exception {

        List<TaskSubmission> submissions =
                submissionService.getTaskSubmissionsByTaskId(taskId);

        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    // ✅ ACCEPT / DECLINE SUBMISSION
    @PutMapping("/{id}")
    public ResponseEntity<TaskSubmission> acceptOrDeclineTaskSubmission(
            @PathVariable String id,
            @RequestParam("status") String status) throws Exception {

        TaskSubmission submission =
                submissionService.acceptDeclineSubmission(id, status);

        return new ResponseEntity<>(submission, HttpStatus.OK);
    }
}
