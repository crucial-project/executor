FROM openjdk:11-slim

ENV NAME=executor
ENV VERSION=3.0
ENV JAR=$NAME-${VERSION}.jar

ADD target/$JAR .
ADD target/lib lib
