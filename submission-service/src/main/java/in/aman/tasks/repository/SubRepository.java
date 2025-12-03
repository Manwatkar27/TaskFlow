package in.aman.tasks.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import in.aman.tasks.submissionModel.TaskSubmission;

@Repository
public interface SubRepository extends MongoRepository<TaskSubmission, String> {
    
   
    List<TaskSubmission> findByTaskId(String taskId);
}