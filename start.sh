#!/bin/sh
cd /root/kicker-league-scanner
source .env
java -XX:+UseParallelGC -jar target/kicker-league-scanner-1.0.0-standalone.jar server

