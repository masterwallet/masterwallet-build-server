# masterwallet-build-server

enviroment properties to be defined:
```
RELEASE=1
SLACK_CHANNEL_URL=...
BUILD_ROOT=/opt/builds
DIST_ROOT=/mnt/dist.masterwallet.pro
DIST_HTTP_ROOT=http://dist.masterwallet.pro
```

run just the build without server
```
sbt "runMain pro.masterwallet.Build"
```

run the server to watch for pushes
```
sbt run
```