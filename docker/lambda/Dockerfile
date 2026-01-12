FROM gradle:8.14-jdk21 AS build

WORKDIR /workspace

COPY . /workspace
RUN ./gradlew :app:bootJar --no-daemon

FROM public.ecr.aws/lambda/java:21

COPY --from=build /workspace/app/build/libs/*-SNAPSHOT.jar ${LAMBDA_TASK_ROOT}/app.jar

CMD ["org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest"]
