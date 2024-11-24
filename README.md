# kicker-league-scanner

FIXME: description

## Installation
1. Clone this repo.
2. Put already downloaded matches to the `match-directory-path` directory. Otherwise, the app will start from zero.

## Usage
```shell
.lein.sh run [global-options] command [command options] [arguments...]
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
1. create standalone jar: `./lein.sh uberjar`
1. link `klsd` to `/etc/init.d/klsd`
1. activate default openrc level: `openrc default`
1. register service: `rc-update add klsd default`
1. start server: `service klsd start`

## run as docker container
### build
build the container:  
`docker build -t kicker-league-scanner .`
### create docker archive
1. save container to file:  
`docker save kicker-league-scanner:latest | gzip > kicker-league-scanner.tar.gz`
1. copy to target
1. load image:  
`gunzip -c kicker-league-scanner.tar.gz | docker load`
### run
start the container:  
`docker run -d -p 5000:80 --name kicker-league-scanner --restart unless-stopped --volume /data/kicker-league-scanner/downloaded-matches:/app/downloaded-matches kicker-league-scanner`
### docker-compose.yaml
start the container:  
`docker-compose up -d`


## TODOs 

[ ] build jar with github actions

[ ] change author
- howto: https://gist.github.com/amalmurali47/77e8dc1f27c791729518701d2dec3680

[ ] add closable system:
- https://gist.github.com/andfadeev/176abae0a0d55b90492c67d2978ba6c0
- https://www.youtube.com/watch?v=a1TvDcDop2k
- https://medium.com/@maciekszajna/reloaded-workflow-out-of-the-box-be6b5f38ea98

[x] deploy to lxc with alpine linux and create a daemon with OpenRC

[x] expose state with timestamp via status page to monitor with updatekuma

[x] show new match titles on status page, to see weather updatig still works
