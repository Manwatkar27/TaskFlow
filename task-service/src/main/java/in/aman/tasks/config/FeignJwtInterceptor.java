package in.aman.tasks.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignJwtInterceptor {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {

            @Override
            public void apply(RequestTemplate template) {

                ServletRequestAttributes attr =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attr == null) return;

                HttpServletRequest req = attr.getRequest();

                String authHeader = req.getHeader("Authorization");

                //  Copy JWT to Feign request
                if (authHeader != null) {
                    template.header("Authorization", authHeader);
                }
            }
        };
    }
}
