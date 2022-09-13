#!/bin/bash

echo "Login in docker..."
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin

echo "Pushing with tag ${TAG_VERSION}"
docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 -t balzaclang/balzac:${TAG_VERSION} --push .
