package in.aman.tasks.submission_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "in.aman.tasks")
@EnableEurekaClient
@EnableFeignClients(basePackages = "in.aman.tasks")
@EnableMongoRepositories(basePackages = "in.aman.tasks.repository")
public class SubmissionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubmissionServiceApplication.class, args);
    }
}