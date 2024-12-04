#!/bin/bash


echo "I'm here"

cd /root/rl_CSR && JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./gradlew --configure-on-demand -x check run
