package com.nokcha.efbe.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EF API")
                        .version("v1")
                        .description("EF 백엔드 API 문서"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }

    // 공통 예시 응답
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                    if (!operation.getResponses().containsKey("200")) {
                        operation.getResponses().addApiResponse("200", new ApiResponse()
                                .description("성공")
                                .content(createSuccessContent()));
                    }

                    // 생성 성공은 POST 일 때만 의미 있음
                    if (httpMethod == PathItem.HttpMethod.POST
                            && !operation.getResponses().containsKey("201")) {
                        operation.getResponses().addApiResponse("201", new ApiResponse()
                                .description("생성 성공")
                                .content(createCreatedContent()));
                    }

                    if (!operation.getResponses().containsKey("400")) {
                        operation.getResponses().addApiResponse("400", new ApiResponse()
                                .description("잘못된 요청")
                                .content(createBadRequestContent()));
                    }

                    if (!operation.getResponses().containsKey("500")) {
                        operation.getResponses().addApiResponse("500", new ApiResponse()
                                .description("서버 내부 오류")
                                .content(createInternalServerErrorContent()));
                    }
                }));
    }

    // 성공 응답 예시
    private Content createSuccessContent() {
        return new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType()
                        .schema(createSuccessSchema())
                        .addExamples("success", new Example()
                                .summary("성공 응답 예시")
                                .value("""
                                        {
                                          "code": 200,
                                          "message": "요청이 성공했습니다.",
                                          "data": {
                                            "id": 1,
                                            "nickname": "ef_user"
                                          }
                                        }
                                        """))
        );
    }

    // 생성 성공 응답 예시 (POST 전용)
    private Content createCreatedContent() {
        return new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType()
                        .schema(createCreatedSchema())
                        .addExamples("created", new Example()
                                .summary("생성 성공 응답 예시")
                                .value("""
                                        {
                                          "code": 201,
                                          "message": "생성되었습니다.",
                                          "data": {
                                            "id": 1
                                          }
                                        }
                                        """))
        );
    }

    // 잘못된 요청 응답 예시
    private Content createBadRequestContent() {
        return new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType()
                        .schema(createErrorSchema())
                        .addExamples("badRequest", new Example()
                                .summary("잘못된 요청 예시")
                                .value("""
                                        {
                                          "code": 400,
                                          "httpStatus": "Bad Request",
                                          "errorMessage": {
                                            "loginId": "아이디는 필수입니다. 요청받은 값: null"
                                          }
                                        }
                                        """))
        );
    }

    // 서버 오류 응답 예시
    private Content createInternalServerErrorContent() {
        return new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType()
                        .schema(createErrorSchema())
                        .addExamples("serverError", new Example()
                                .summary("서버 오류 예시")
                                .value("""
                                        {
                                          "code": 500,
                                          "httpStatus": "Internal Server Error",
                                          "errorMessage": "예상하지 못한 오류가 발생했습니다."
                                        }
                                        """))
        );
    }

    // 성공 응답 스키마
    private Schema<?> createSuccessSchema() {
        return new ObjectSchema()
                .addProperty("code", new IntegerSchema().example(200))
                .addProperty("message", new StringSchema().example("요청이 성공했습니다."))
                .addProperty("data", new ObjectSchema()
                        .addProperty("id", new IntegerSchema().example(1))
                        .addProperty("nickname", new StringSchema().example("ef_user")));
    }

    // 생성 성공 응답 스키마 (POST)
    private Schema<?> createCreatedSchema() {
        return new ObjectSchema()
                .addProperty("code", new IntegerSchema().example(201))
                .addProperty("message", new StringSchema().example("생성되었습니다."))
                .addProperty("data", new ObjectSchema()
                        .addProperty("id", new IntegerSchema().example(1)));
    }

    // 실패 응답 스키마
    private Schema<?> createErrorSchema() {
        return new ObjectSchema()
                .addProperty("code", new IntegerSchema().example(400))
                .addProperty("httpStatus", new StringSchema().example("Bad Request"))
                .addProperty("errorMessage", new ObjectSchema()
                        .additionalProperties(new StringSchema())
                        .example("{\"field\":\"에러 메시지\"}"));
    }
}
