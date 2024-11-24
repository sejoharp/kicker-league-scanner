# Makefile for managing the kicker-league-scanner Docker container

# Variables
IMAGE_NAME = kicker-league-scanner
TAR_FILE = kicker-league-scanner.tar.gz
DOCKER_COMPOSE_FILE = docker-compose.yaml

.PHONY: all build save load run compose

# Default target
all: build

build:
	@echo "Building the container..."
	docker build -t $(IMAGE_NAME) .

save:
	@echo "Saving container to file..."
	docker save $(IMAGE_NAME):latest | gzip > $(TAR_FILE)

load:
	@echo "Loading image from archive..."
	gunzip -c $(TAR_FILE) | docker load

run:
	@echo "Starting the container..."
	docker run -d -p 5000:80 --name $(IMAGE_NAME) --restart unless-stopped --volume /data/kicker-league-scanner/downloaded-matches:/app/downloaded-matches $(IMAGE_NAME)

compose:
	@echo "Starting the container with Docker Compose..."
	docker-compose -f $(DOCKER_COMPOSE_FILE) up -d
