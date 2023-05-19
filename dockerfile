FROM maven:3.9.2-eclipse-temurin-20-alpine
RUN mkdir ./feed && mkdir /root/.m2
COPY . /feed
WORKDIR /feed
ENV TZ=America/Fortaleza
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN mvn clean package -DskipTests
RUN mv target/*.jar target/app.jar
ENTRYPOINT ["java","-jar","target/app.jar"]