#!/bin/bash
echo "Starting deployment..."

# Ensure we're in the right directory
pwd
echo "Current directory contents:"
ls -la

# Check if out directory exists
if [ ! -d "out" ]; then
    echo "Error: 'out' directory not found!"
    exit 1
fi

echo "Contents of out directory:"
ls -la out/

echo "Copying files..."
# Create a public directory if it doesn't exist
mkdir -p public

# Copy all files from out to public
cp -rv out/* public/

echo "Contents of public directory:"
ls -la public/

echo "Deployment complete!"
