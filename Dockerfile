FROM eclipse-temurin:21-jre
EXPOSE 8080
ADD backend/target/Event-Buddy.jar Event-Buddy.jar
ENTRYPOINT ["java","-jar","Event-Buddy.jar"]