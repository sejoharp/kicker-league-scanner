<!-- TOC -->
  * [Installation](#installation)
  * [Usage](#usage)
    * [commands](#commands)
  * [Run as openrc background service](#run-as-openrc-background-service)
  * [Run as docker container](#run-as-docker-container)
  * [TODOs](#todos)
<!-- TOC -->

## Installation
1. Clone this repo.
2. Put already downloaded matches to the `match-directory-path` directory. Otherwise, the app will start from zero.

## Usage
```shell
./lein.sh run [global-options] command [command options] [arguments...]
```
### commands
The default parameters work for me. 
```shell
NAME:
 kicker-league-scanner - A command-line kicker stats scanner

USAGE:
 kicker-league-scanner [global-options] command [command options] [arguments...]

VERSION:
 1.0.0

COMMANDS:
   download, d          downloads all matches for the given season
   export, e            exports all matches to a given csv file
   upload, u            uploads all matches to nextcloud
   server, s            cronjob that downloads and uploads all matches to nextcloud

GLOBAL OPTIONS:
   -?, --help
```

call `kicker-league-scanner [command] --help` for more infos to the commands.

## Run as openrc background service
```shell
# create standalone jar
./lein.sh uberjar

# link start script to init system
ln -s klsd /etc/init.d/klsd

# activate default openrc level
openrc default

# register service
rc-update add klsd default

#start server
service klsd start
```

## Run as docker container
```shell
# build the container  
docker build -t kicker-league-scanner

# create docker archive by saving container to file  
docker save kicker-league-scanner:latest | gzip > kicker-league-scanner.tar.gz

# copy to target
# e.g. scp ...

# load image
gunzip -c kicker-league-scanner.tar.gz | docker load

# set environment variables in .env file
echo "KICKER_TARGET_DOMAIN=my.domain.com" >> .env
echo "KICKER_TARGET_USER=myuser" >> .env
echo "KICKER_TARGET_PASSWORD=secret" >> .env

# Put the .env file next to the docker-compose.yaml file.

# start with docker compose
docker-compose up -d

# start with plain docker
docker run \
-d \ 
-p 5000:80 \
--env-file .env \
--name kicker-league-scanner \
--restart unless-stopped \
--volume /data/kicker-league-scanner/downloaded-matches:/app/downloaded-matches \
kicker-league-scanner
```

## TODOs
[ ] publish docker image to ghcr.io
- https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#pushing-container-images
- https://docs.github.com/en/actions/use-cases-and-examples/publishing-packages/publishing-docker-images#publishing-images-to-github-packages

[ ] pull docker image from ghcr.io
- https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry#pulling-container-images

[ ] change author
- howto: https://gist.github.com/amalmurali47/77e8dc1f27c791729518701d2dec3680

[ ] add closable system:
- https://gist.github.com/andfadeev/176abae0a0d55b90492c67d2978ba6c0
- https://www.youtube.com/watch?v=a1TvDcDop2k
- https://medium.com/@maciekszajna/reloaded-workflow-out-of-the-box-be6b5f38ea98

[ ] build jar with github actions

[x] deploy to lxc with alpine linux and create a daemon with OpenRC

[x] expose state with timestamp via status page to monitor with updatekuma

[x] show new match titles on status page, to see weather updatig still works
