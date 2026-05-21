package backend.manuhub.annotation;

import backend.manuhub.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {
                                	"code" : "INVALID_REQUEST_ERROR",
                                	"message" : "잘못된 요청입니다."
                                }
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "405",
                description = "허용되지 않는 HTTP 메소드",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {
                                 	"code" : "METHOD_NOT_ALLOWED_ERROR",
                                 	"message" : "허용되지 않는 HTTP 메서드입니다."
                                 }
                                """)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(value = """
                                {
                                	"code" : "INTERNAL_SERVER_ERROR",
                                	"message" : "서버 내부 오류입니다."
                                }
                                """)
                )
        )
})
public @interface CommonErrorResponses {
}
