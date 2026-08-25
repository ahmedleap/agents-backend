# Environment verification template — confirms Maven, JDK, and JUnit are installed
# and working together correctly. Not part of the application build; run this on its
# own to sanity-check a CI agent/image before wiring up the real pipeline.
FROM maven:3.9-eclipse-temurin-21

WORKDIR /env-check
COPY pom.xml .
COPY src ./src

# Print toolchain versions so they're visible in build logs, then compile and run
# the placeholder JUnit test to prove the full chain (javac -> junit -> surefire) works.
RUN echo "--- java -version ---" && java -version \
    && echo "--- mvn -version ---" && mvn -version \
    && echo "--- running JUnit sanity test ---" \
    && mvn -B test

CMD ["mvn", "-B", "test"]
