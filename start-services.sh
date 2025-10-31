#!/bin/bash

# RapidCart Microservices Startup Script

echo "🚀 Starting RapidCart Microservices..."

# Function to check if a service is healthy
check_service_health() {
    local service_name=$1
    local health_url=$2
    local max_attempts=10
    local attempt=1

    echo "⏳ Waiting for $service_name to be healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$health_url" > /dev/null 2>&1; then
            echo "✅ $service_name is healthy!"
            return 0
        fi
        
        echo "🔄 Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 10
        ((attempt++))
    done
    
    echo "❌ $service_name failed to become healthy after $max_attempts attempts"
    return 1
}

check_postgres() {
    local max_attempts=10
    local attempt=1
    echo "⏳ Waiting for PostgreSQL to be ready..."
    while [ $attempt -le $max_attempts ]; do
        # Uses service name 'postgres'; adjust if different
        if docker compose exec -T postgres pg_isready -q; then
            echo "✅ PostgreSQL is ready!"
            return 0
        fi
        echo "🔄 Attempt $attempt/$max_attempts - PostgreSQL not ready yet..."
        sleep 5
        ((attempt++))
    done
    echo "❌ PostgreSQL failed to become ready after $max_attempts attempts"
    return 1
}

# Stop any existing containers
echo "🧹 Stopping existing containers..."
docker compose down

# Build and start services
echo "🔨 Building and starting services..."
docker compose up --build -d 

# Wait for services to be healthy
echo "🔍 Checking service health..."

check_postgres || {
    echo "❌ PostgreSQL failed to start. Check logs with: docker compose logs postgres"
    exit 1
}

# Check application services
check_service_health "Product Service" "http://localhost:8081/actuator/health" || {
    echo "❌ Product Service failed to start. Check logs with: docker compose logs product-service"
    exit 1
}

check_service_health "Order Service" "http://localhost:8082/actuator/health" || {
    echo "❌ Order Service failed to start. Check logs with: docker compose logs order-service"
    exit 1
}

check_service_health "Notification Service" "http://localhost:8083/actuator/health" || {
    echo "❌ Notification Service failed to start. Check logs with: docker compose logs notification-service"
    exit 1
}

echo ""
echo "🎉 All services are up and running!"
echo ""
echo "📋 Service URLs:"
echo "   • Product Service:      http://localhost:8081"
echo "   • Product Service API:  http://localhost:8081/swagger-ui/index.html"
echo "   • Order Service:        http://localhost:8082"
echo "   • Order Service API:    http://localhost:8082/swagger-ui/index.html"
echo "   • Notification Service: http://localhost:8083"
echo "   • Notification API:     http://localhost:8083/swagger-ui/index.html"
echo "   • RabbitMQ Management:  http://localhost:15672 (guest/guest)"
echo ""
echo "📊 Useful commands:"
echo "   • View logs:           docker compose logs -f"
echo "   • Stop services:       docker compose down"
echo "   • View status:         docker compose ps"
echo ""
echo "✨ RapidCart is ready for use!"
