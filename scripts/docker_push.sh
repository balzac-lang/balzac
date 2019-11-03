#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

echo "Pushing with tag 'latest'"
docker push balzaclang/balzac:latest
docker push balzaclang/balzac-arm32v7:latest
docker push balzaclang/balzac-arm64v8:latest

echo "Pushing with tag ${LATEST_VERSION}"
docker push balzaclang/balzac:${LATEST_VERSION}
docker push balzaclang/balzac-arm32v7:${LATEST_VERSION}
docker push balzaclang/balzac-arm64v8:${LATEST_VERSION}
