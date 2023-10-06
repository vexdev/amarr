# Amule *arr Connector

This connector allows using amule as a download client for [Sonarr](https://sonarr.tv/) and [Radarr](https://radarr.video/).
It works by emulating a torrent client, so Sonarr and Radarr will manage your downloads as if they were torrents.

## Installation

This connector runs as a docker container. You can find it in [Docker Hub](https://hub.docker.com/r/vexdev/amule-arr-connector).

It requires the following environment variables:
```
AMULE_HOST: amule # The host where amule is running, for docker containers it's usually the name of the container
AMULE_PORT: 4712 # The port where amule is listening with the EC protocol
AMULE_PASSWORD: secret # The password to connect to amule
```

It also requires mounting the following volumes:
```
/requested # The directory where radarr/sonarr will put the requested torrent files
/finished # The directory where amule will download the finished files
```

The container exposes the port 8080, which is the port where the connector will expose the Torznab server for Sonarr/Radarr.

### Example docker-compose.yml

```yaml
version: '3.9'
amule-arr-connector:
    image: vexdev/amule-arr-connector:latest
    container_name: amule-arr-connector
    environment:
        - AMULE_HOST=amule
        - AMULE_PORT=4712
        - AMULE_PASSWORD=secret
    volumes:
        - /path/to/requested:/requested
        - /path/to/finished:/finished
    ports:
        - 8080:8080
```

## Radarr/Sonarr configuration

You need to configure Sonarr/Radarr to use the connector as a torrent indexer. You can do that by adding a new Torznab indexer with the following settings:
```
Name: Any name you want
Url: http://amule-arr-connector:8080
```
You can leave the rest of the settings as default.

You will need then to add the download client. You can do that by adding a new download client of type Torrent Blackhole with the following settings:
```
Name: Any name you want
Torrent Folder: /path/to/requested
Watch Folder: /path/to/finished
```