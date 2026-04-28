# TIBCO EMS Docker Setup

This directory contains Docker configuration for running TIBCO EMS (Enterprise Message Service) 8.5.1 for the JMS-based messaging tutorial examples.

## Prerequisites

- Docker and Docker Compose installed
- Access to the TIBCO EMS Docker image file
- Sufficient disk space (~2GB minimum)

## Setup Instructions

### 1. Download the TIBCO EMS Docker Image

The Docker image for TIBCO EMS 8.5.1 is distributed as a tar.gz archive and must be downloaded from Google Drive:

1. Go to: https://drive.google.com/drive/u/0/folders/18e7ftxFOYxLuAA489T4O-9PcYSNbvEgD
2. Download `docker_image_ems_8.5.1.tar.gz`
3. Place the file in this directory (`messaging/docker/`)

### 2. Load the Docker Image

Once the tar.gz file is in place, load it into Docker:

```bash
cd messaging/docker
docker load -i docker_image_ems_8.5.1.tar.gz
```

Verify the image is loaded:

```bash
docker images | grep ems
```

You should see output similar to:
```
ems                    8.5.1      <image-id>   <created>   <size>
```

### 3. Start Services

Launch TIBCO EMS:

```bash
docker-compose up -d
```

Verify the service is running:

```bash
docker-compose ps
```

You should see the `ems` container in "Up" status.

### 4. Verify TIBCO EMS is Ready

Check the TIBCO EMS logs to confirm it started successfully:

```bash
docker-compose logs -f ems
```

Look for messages indicating the server is ready to accept connections.

## Service Configuration

### TIBCO EMS Server

- **Container name**: `ems`
- **Image**: `ems:8.5.1`
- **Port**: `7222` (TCP, mapped to host)
- **Volume**: `./tibemssrv/` (local bind mount for persistent EMS data and configuration)
- **Network**: `camel-network` (bridge network for container communication)

## Connection Details

### From Host Machine

```
tcp://localhost:7222
```

### From Other Docker Containers

```
tcp://ems:7222
```

## Docker Compose Structure

The `docker-compose.yml` file defines:

- **Service**: TIBCO EMS container with port mapping and bind mount
- **Volume**: Bind mount `./tibemssrv/` for persistent EMS data and configuration
- **Network**: `camel-network` bridge network for inter-container communication

## Stopping Services

To stop the services while preserving data:

```bash
docker-compose down
```

To stop services and clean up the local `tibemssrv/` directory (data will be lost):

```bash
docker-compose down
rm -rf tibemssrv/
```

## Troubleshooting

### Image fails to load

- Verify the tar.gz file is in the `messaging/docker/` directory
- Check file integrity: `ls -lh docker_image_ems_8.5.1.tar.gz`
- Try re-downloading from Google Drive if the file is corrupted

### EMS container fails to start

- Check logs: `docker-compose logs ems`
- Verify the `tibemssrv/` directory exists and has write permissions: `ls -la tibemssrv/`
- Try removing and recreating services:
  ```bash
  docker-compose down
  docker-compose up -d
  ```

### Port conflicts

If port 7222 is already in use, modify the port mapping in `docker-compose.yml`:

```yaml
ports:
  - "7223:7222"  # Access EMS on port 7223
```

### Permission issues

- Ensure Docker daemon is running and you have permissions to use Docker
- On macOS/Linux, you may need to use `sudo` or add your user to the docker group

## Next Steps

Once TIBCO EMS is running, you can:

1. Configure Camel routes to connect to TIBCO EMS on `tcp://localhost:7222`
2. Run the messaging examples from the `chapter3/` directory:
   - `chapter3/jms-sender` — Produces messages to TIBCO EMS
   - `chapter3/jms-receiver` — Consumes messages from TIBCO EMS

For more details on building and running Camel examples, refer to the parent `README.md` and individual project READMEs.
