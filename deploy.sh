#!/bin/bash
echo "Starting deployment..."
echo "Contents of current directory:"
ls -la
echo "Contents of out directory:"
ls -la out/
echo "Copying files..."
cp -r out/* .
echo "Final contents:"
ls -la
echo "Deployment complete!"
