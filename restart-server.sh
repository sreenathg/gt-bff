#!/bin/bash

# Script to restart the GT BFF server
# Usage: ./restart-server.sh

set -e  # Exit on any error

# Step 0: Clear console
clear
echo "🔄 Starting GT BFF server restart process..."

# Step 1: Kill any process running on port 8081
echo "📍 Checking for processes on port 8081..."
PID=$(lsof -ti:8081 || true)
if [ ! -z "$PID" ]; then
    echo "🛑 Found process $PID running on port 8081. Killing it..."
    kill -9 $PID
    sleep 2
    echo "✅ Process killed successfully"
else
    echo "✅ No process found running on port 8081"
fi

# Step 1.5: Clean up logs
echo "🧹 Cleaning up logs..."
rm -rf logs/*
echo "✅ Logs cleaned successfully"

# Step 2: Clean and build the project
echo "🔨 Running 'mvn clean install'..."
mvn clean install -Djacoco.skip=true

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed! Exiting..."
    exit 1
fi

# Step 3: Start the server with local profile
echo "🚀 Starting server with local profile..."
echo "📝 Server will be available at: http://localhost:8081"
echo "📊 Swagger UI will be available at: http://localhost:8081/swagger-ui.html"
echo ""
echo "💡 Press Ctrl+C to stop the server"
echo ""

mvn spring-boot:run -Dspring-boot.run.profiles=local