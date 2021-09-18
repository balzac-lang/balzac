#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

echo "Pushing with tag 'latest'"
docker push balzaclang/balzac:latest

echo "Pushing with tag ${LATEST_VERSION}"
docker push balzaclang/balzac:${LATEST_VERSION}
