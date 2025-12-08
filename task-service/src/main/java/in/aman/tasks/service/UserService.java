package in.aman.tasks.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import in.aman.tasks.TaskModel.UserDTO;


	//connect with taskUserService microService
	@FeignClient(name = "USER-SERVICE")
	public interface UserService { 

	    @GetMapping("/api/users/profile")
	    public UserDTO getUserProfileHandler(@RequestHeader("Authorization") String jwt);
}
