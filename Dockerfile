FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .

COPY . .

RUN mvn dependency:go-offline

## Assuming your main class is Node and is part of the "node" package
#CMD ["mvn", "exec:java", "-Dexec.mainClass=node.Node"]
