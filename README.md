# Amarr - Amule *arr Connector
![Docker Image Version (latest semver)](https://img.shields.io/docker/v/vexdev/amarr)
[![semantic-release: angular](https://img.shields.io/badge/semantic--release-angular-e10079?logo=semantic-release)](https://github.com/semantic-release/semantic-release)


This connector allows using amule as a download client for [Sonarr](https://sonarr.tv/)
and [Radarr](https://radarr.video/).
It works by emulating a torrent client, so Sonarr and Radarr will manage your downloads as if they were torrents.

Makes use of [jAmule](https://github.com/vexdev/jamule) to connect to amule.

## Installation

Amarr runs as a docker container. You can find it in [Docker Hub](https://hub.docker.com/r/vexdev/amarr).

It requires the following environment variables:

```
AMULE_HOST: amule # The host where amule is running, for docker containers it's usually the name of the container
AMULE_PORT: 4712 # The port where amule is listening with the EC protocol
AMULE_PASSWORD: secret # The password to connect to amule
AMARR_URL: http://amarr:8080 # The url where amarr will be listening, for docker containers it's usually the name of the container

Optional parameters:
AMULE_FINISHED_PATH: /finished # The directory where amule will download the finished files
AMARR_LOG_LEVEL: INFO # The log level of amarr, defaults to INFO
```

Note: **AMARR_URL** is used to build the url of the torrent files, so it must be accessible from Sonarr/Radarr.

It also requires mounting the following volumes:

```
/config # The directory where amarr will store its configuration, must be persistent
```

The container exposes the port 8080, which is the port where amarr will expose the Torznab server for Sonarr/Radarr.

### Example docker-compose.yml

```yaml
amarr:
  image: vexdev/amarr:latest
  container_name: amarr
  environment:
    - AMULE_HOST=amule
    - AMULE_PORT=4712
    - AMULE_PASSWORD=secret
  volumes:
    - /path/to/amarr/config:/config
  ports:
    - 8080:8080
```

## Radarr/Sonarr configuration

### Configure amarr as a torrent indexer

You need to configure Sonarr/Radarr to use amarr as a torrent indexer. You can do that by adding a new Torznab indexer
with the following settings:

```
Name: Any name you want
Url: http://amarr:8080
```

You can leave the rest of the settings as default for now, we will come back to them later.

### Configure amarr as a download client

You will need then to add the download client. You can do that by adding a new download client of type qBittorrent with
the following settings:

```
! Ensure you pressed the "Show advanced settings" button
Name: Any name you want
Host: amarr # The host where amarr is running, for docker containers it's usually the name of the container
Port: 8080 # The port where amarr is listening
Priority: 50 # This is the lowest possible priority, so Sonarr/Radarr will prefer other download clients
```

### Configure amarr as a preferred download client for its indexer

You need to configure Sonarr/Radarr to prefer amarr as a download client for the indexer we created before.
You can do that by going to the **indexer settings** and setting the following values for Amarr:

```
! Ensure you pressed the "Show advanced settings" button
Download Client: The name you gave to amarr in the previous step
```

### Supported amule versions

Amarr is currently using jAmule to connect to amule, which only supports amule versions 2.3.1 to 2.3.3.
Amarr has been especially tested with the latest released version of Adunanza.