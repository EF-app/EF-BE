# syntax=docker/dockerfile:1

# ============================================================
#  EF-BE 프로덕션 이미지 (Spring Boot 4 / Java 21)
#  멀티스테이지: build(JDK) → runtime(JRE) 로 최종 이미지 슬림화.
#  같은 이미지가 EC2 / ECS Fargate 어디서든 동일하게 실행됨.
# ============================================================

# ---------- build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 1) 의존성 레이어 캐시 — 빌드 스크립트/래퍼만 먼저 복사해두면
#    src 만 바뀌었을 때 의존성 다운로드 레이어를 재사용한다.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 2) 소스 복사 후 실행 가능 jar 빌드 (테스트는 CI 에서 별도 실행)
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test \
 && rm -f build/libs/*-plain.jar          # 실행 불가한 plain jar 제거 → 아래 COPY 가 단일 jar 만 잡음

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# 비루트 유저로 실행 (컨테이너 보안 기본)
RUN groupadd -r appuser && useradd -r -g appuser -u 1001 appuser

# KST 고정 — cron / LocalDate.now() 의 JVM 타임존 어긋남 방지 (배포 환경 TZ 무관하게 Asia/Seoul)
ENV TZ=Asia/Seoul
# 컨테이너에 할당된 메모리의 75% 까지 힙 사용 + 타임존 JVM 레벨 명시
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Seoul"
# 기본 운영 프로필 (docker run -e 로 덮어쓸 수 있음)
ENV SPRING_PROFILES_ACTIVE=prod

# 실행 가능 부트 jar 만 복사 (plain jar 는 build stage 에서 이미 제거됨)
COPY --from=build /app/build/libs/*.jar app.jar

USER appuser
EXPOSE 8080

# JAVA_OPTS 를 쉘이 전개하도록 sh -c 사용
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar app.jar"]
