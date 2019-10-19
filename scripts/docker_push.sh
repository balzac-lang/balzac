#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

docker push balzaclang/balzac:latest
docker push balzaclang/balzac-arm32v7:latest
docker push balzaclang/balzac-arm64v8:latest
