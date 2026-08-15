#!/bin/bash

docker build \
  --progress=plain \
  -t authorization-app \
  -f Dockerfile.local .