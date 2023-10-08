# Amarr - Amule *arr Connector

This connector allows using amule as a download client for [Sonarr](https://sonarr.tv/)
and [Radarr](https://radarr.video/).
It works by emulating a torrent client, so Sonarr and Radarr will manage your downloads as if they were torrents.

## Installation

Amarr runs as a docker container. You can find it in [Docker Hub](https://hub.docker.com/r/vexdev/amarr).

It requires the following environment variables:

```
AMULE_HOST: amule # The host where amule is running, for docker containers it's usually the name of the container
AMULE_PORT: 4712 # The port where amule is listening with the EC protocol
AMULE_PASSWORD: secret # The password to connect to amule
AMARR_URL: http://amarr:8080 # The url where amarr will be listening, for docker containers it's usually the name of the container
```

Note: **AMARR_URL** is used to build the url of the torrent files, so it must be accessible from Sonarr/Radarr.

It also requires mounting the following volumes:

```
/finished # The directory where amule will download the finished files
```

The container exposes the port 8080, which is the port where amarr will expose the Torznab server for Sonarr/Radarr.

### Example docker-compose.yml

```yaml
version: '3.9'
amarr:
  image: vexdev/amarr:latest
  container_name: amarr
  environment:
    - AMULE_HOST=amule
    - AMULE_PORT=4712
    - AMULE_PASSWORD=secret
    - AMARR_URL=http://amarr:8080
  volumes:
    - /path/to/finished:/finished
  ports:
    - 8080:8080
```

## Radarr/Sonarr configuration

You need to configure Sonarr/Radarr to use amarr as a torrent indexer. You can do that by adding a new Torznab indexer
with the following settings:

```
Name: Any name you want
Url: http://amarr:8080
```

You can leave the rest of the settings as default.

You will need then to add the download client. You can do that by adding a new download client of type qBittorrent with
the following settings:

```
Name: Any name you want
Host: amarr # The host where amarr is running, for docker containers it's usually the name of the container
Port: 8080 # The port where amarr is listening
```

## TODO

- [ ] Publish EC library to maven
- [ ] Implement generation of torrent files
- [ ] Dockerize and publish to docker hub
- [ ] Add support for multiple categories by a local database
- [ ] Add support for percentage of completion of search
- [ ] Document the versions of aMule that are supported
