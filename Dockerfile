FROM openjdk:11

RUN mkdir -p /build/libs
RUN chmod -R +r /build/libs

RUN mkdir -p /etc/secrets
RUN chmod -R +r /etc/secrets

RUN mkdir /app
COPY . /app
WORKDIR /app
COPY build/libs/*.jar /app/
