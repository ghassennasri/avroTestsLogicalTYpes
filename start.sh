#!/bin/bash


# Start main services
docker-compose up -d zookeeper broker schema-registry control-center akhq

# Wait for 2 minutes to ensure other services are fully up and running
echo "Waiting for 2 minutes for all services to be fully up..."
sleep 120

# Now, build and start the app-test service with or without debug mode
echo "Starting app-test service..."
docker-compose up -d app-test
#docker-compose logs -f app-test

echo "All services are up."
