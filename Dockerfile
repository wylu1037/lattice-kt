FROM gradle:7-jdk8-jammy AS builder
WORKDIR /tmp
COPY . /tmp
RUN ./gradlew dokkaHtmlMultiModule

FROM nginx:latest as runner
WORKDIR /app
COPY --from=builder /tmp/build/dokka/htmlMultiModule/ /usr/share/nginx/html
