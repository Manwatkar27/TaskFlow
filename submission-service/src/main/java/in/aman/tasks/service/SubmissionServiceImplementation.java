package in.aman.tasks.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import in.aman.tasks.repository.SubRepository;
import in.aman.tasks.submissionModel.TaskDTO;
import in.aman.tasks.submissionModel.TaskSubmission;

@Service
public class SubmissionServiceImplementation implements SubmissionService {

    @Autowired
    private SubRepository subRepository;

    @Autowired
    private TaskService taskService;

    @Override
    public TaskSubmission submitTask(String taskId, String githubLink, String userId, String jwt) throws Exception {
        TaskDTO task = null;
        try {
            // Verify task exists using Feign Client
            task = taskService.getTaskById(taskId, jwt);
        } catch (Exception e) {
            // Log the error to see if it's 403 (Forbidden) or 404 (Not Found)
            e.printStackTrace(); 
            // Optional: If you want to bypass the error for testing, uncomment next line
            // return saveSubmission(taskId, githubLink, userId); 
            throw new Exception("Failed to validate task: " + e.getMessage());
        }
        
        if (task != null) {
            return saveSubmission(taskId, githubLink, userId);
        }
        throw new Exception("Task not found with id: " + taskId);
    }

    // Helper method to save submission
    private TaskSubmission saveSubmission(String taskId, String githubLink, String userId) {
        TaskSubmission submission = new TaskSubmission();
        submission.setTaskId(taskId);
        submission.setUserId(userId);
        submission.setGithubLink(githubLink);
        submission.setSubmissionTime(LocalDateTime.now());
        return subRepository.save(submission);
    }

    @Override
    public TaskSubmission getTaskSubmissionById(String submissionId) throws Exception {
        return subRepository.findById(submissionId)
                .orElseThrow(() -> new Exception("Task Submission not found with id " + submissionId));
    }

    @Override
    public List<TaskSubmission> getAllTaskSubmissions() {
        return subRepository.findAll();
    }

    @Override
    public List<TaskSubmission> getTaskSubmissionsByTaskId(String taskId) {
        return subRepository.findByTaskId(taskId);
    }

    @Override
    public TaskSubmission acceptDeclineSubmission(String id, String status) throws Exception {
        TaskSubmission submission = getTaskSubmissionById(id);
        submission.setStatus(status);
        if (status.equals("ACCEPT")) {
            // If accepted, call Task Service to mark task as DONE
            try {
                taskService.completeTask(submission.getTaskId());
            } catch (Exception e) {
                e.printStackTrace(); // Log if Task Service fails to update status
            }
        }
        return subRepository.save(submission);
    }
}