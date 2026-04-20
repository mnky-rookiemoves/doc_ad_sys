# ── Stage 1: Build ──────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first (cache Maven deps)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy src
COPY src ./src

# Build WAR, skip tests
RUN mvn clean package -DskipTests

# ── Stage 2: Run ────────────────────────────────────────
FROM tomcat:10.1-jdk17

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy built WAR into Tomcat
COPY --from=build /app/target/doc_ad_sys.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]