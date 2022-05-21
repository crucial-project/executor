#!/bin/bash

# see Dockerfile for env. variables 

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

CLASSPATH="${DIR}/*:${DIR}/lib/*"

JVM="-Xms16m -Xmx64m ${JVM_EXTRA}"

CMD="java -ea -cp \"${CLASSPATH}\" ${JVM} org.crucial.executor.k8s.KubernetesHandler ${INPUT}"

bash -c "$CMD"