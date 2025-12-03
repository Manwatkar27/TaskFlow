package in.aman.tasks.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.aman.tasks.enums.TaskStatus;
import in.aman.tasks.repository.TaskRepository;
import in.aman.tasks.TaskModel.Task; 

@Service
public class TaskServiceImplementation implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public TaskServiceImplementation(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task create(Task task, String requestRole) throws Exception {
        if (!requestRole.equals("ROLE_ADMIN")) {
            throw new Exception("Only admin can create tasks");
        }

        task.setStatus(TaskStatus.PENDING);
        task.setCreateAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Override
    public Task getTaskById(String id) throws Exception {
        return taskRepository.findById(id)
                .orElseThrow(() -> new Exception("Task not found with id " + id));
    }

    @Override
    public List<Task> getAllTasks(TaskStatus taskStatus) {
        List<Task> allTasks = taskRepository.findAll();

        List<Task> filteredTasks = allTasks.stream()
                .filter(task -> taskStatus == null || task.getStatus().name().equalsIgnoreCase(taskStatus.toString()))
                .collect(Collectors.toList());
        return filteredTasks;
    }

    @Override
    public Task updateTask(String id, Task updatedTask, String userId) throws Exception {
        Task existingTask = getTaskById(id);

        if (updatedTask.getTitle() != null) {
            existingTask.setTitle(updatedTask.getTitle());
        }
        if (updatedTask.getImageUrl() != null) {
            existingTask.setImageUrl(updatedTask.getImageUrl());
        }
        if (updatedTask.getDescription() != null) {
            existingTask.setDescription(updatedTask.getDescription());
        }
        if (updatedTask.getStatus() != null) {
            existingTask.setStatus(updatedTask.getStatus());
        }
        if (updatedTask.getDeadline() != null) {
            existingTask.setDeadline(updatedTask.getDeadline());
        }
        return taskRepository.save(existingTask);
    }

    @Override
    public void deleteTask(String id) throws Exception {
        getTaskById(id); // Ensure task exists before deleting
        taskRepository.deleteById(id);
    }

    @Override
    public Task assignedToUser(String userId, String taskId) throws Exception {
        Task task = getTaskById(taskId);
        task.setAssignedUserId(userId);
        task.setStatus(TaskStatus.ASSIGNED);

        return taskRepository.save(task);
    }

    @Override
    public List<Task> assignedUsersTask(String userId, TaskStatus taskStatus) {
        List<Task> allTasks = taskRepository.findByAssignedUserId(userId);

        return allTasks.stream()
                .filter(task -> taskStatus == null || task.getStatus() == taskStatus)
                .collect(Collectors.toList());
    }

    @Override
    public Task completeTask(String taskId) throws Exception {
        Task task = getTaskById(taskId);
        task.setStatus(TaskStatus.DONE);
        return taskRepository.save(task);
    }

    @Override
    public List<Task> getAllTasks(TaskStatus taskStatus, String sortByDeadline, String sortByCreatedAt) {
        List<Task> allTasks = taskRepository.findAll();
        List<Task> filteredTasks = allTasks.stream()
                .filter(task -> taskStatus == null || task.getStatus().name().equalsIgnoreCase(taskStatus.toString()))
                .collect(Collectors.toList());

        if (sortByDeadline != null && !sortByDeadline.isEmpty()) {
            filteredTasks.sort(Comparator.comparing(Task::getDeadline));
        } else if (sortByCreatedAt != null && !sortByCreatedAt.isEmpty()) {
            filteredTasks.sort(Comparator.comparing(Task::getCreateAt));
        }
        return filteredTasks;
    }

    // This was the missing method causing your error!
    @Override
    public List<Task> assignedUsersTask(String userId, TaskStatus status, String sortByDeadline, String sortByCreatedAt) {
        List<Task> allTasks = taskRepository.findByAssignedUserId(userId);
        List<Task> filteredTasks = allTasks.stream()
                .filter(task -> status == null || task.getStatus().name().equalsIgnoreCase(status.toString()))
                .collect(Collectors.toList());

        if (sortByDeadline != null && !sortByDeadline.isEmpty()) {
            filteredTasks.sort(Comparator.comparing(Task::getDeadline));
        } else if (sortByCreatedAt != null && !sortByCreatedAt.isEmpty()) {
            filteredTasks.sort(Comparator.comparing(Task::getCreateAt));
        }
        return filteredTasks;
    }
    
}