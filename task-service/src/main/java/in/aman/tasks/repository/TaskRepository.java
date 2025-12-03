package in.aman.tasks.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import in.aman.tasks.TaskModel.Task;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

	public List<Task> findByAssignedUserId(String userId);
	
	public void deleteById(String id);
	
	public Task getTaskById(String id);
	
}
