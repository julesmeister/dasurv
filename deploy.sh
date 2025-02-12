#!/bin/bash
echo "Starting deployment..."

# Copy the out directory contents to the root
cp -r out/* .

# Copy the worker and routes
cp _worker.js .
cp _routes.json .

echo "Deployment complete!"
