#!/bin/bash

cd ..
mvn clean compile assembly:single
cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar codalab
cd codalab
