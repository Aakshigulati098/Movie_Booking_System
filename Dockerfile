FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew
RUN ./gradlew dependencies

COPY src src

RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-jammy

# Set timezone
ENV TZ=Asia/Kolkata
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 9090

CMD ["java", "-jar", \
     "-Duser.timezone=Asia/Kolkata", \
     "-Dspring.profiles.active=docker", \
     "app.jar"]