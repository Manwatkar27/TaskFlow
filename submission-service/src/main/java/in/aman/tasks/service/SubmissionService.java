package in.aman.tasks.service;

import java.util.List;
import in.aman.tasks.submissionModel.TaskSubmission;

public interface SubmissionService {
    
    TaskSubmission submitTask(String taskId, String githubLink, String userId, String jwt) throws Exception;
    
    TaskSubmission getTaskSubmissionById(String submissionId) throws Exception;
    
    List<TaskSubmission> getAllTaskSubmissions(); 
    
    // FIX: Changed 'Submission' to 'Submissions' (Added 's') to match your Implementation
    List<TaskSubmission> getTaskSubmissionsByTaskId(String taskId);
    
    TaskSubmission acceptDeclineSubmission(String id, String status) throws Exception;
}