package backend.manuhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ManuHub API Docs")
                        .description("ManuHub API Docs")
                        .version("v1.0.0"))
                .servers(List.of(new Server()
                        .url("https://www.manuhub.kro.kr")
                        .description("Production")));
    }
}
