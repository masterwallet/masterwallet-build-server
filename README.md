# masterwallet-build-server

enviroment properties to be defined:
```
RELEASE=1
SLACK_CHANNEL_URL=...
CDN_FOLDER=...
CDN_HTTP_ROOT=...
```

run just the build without server
```
sbt "runMain pro.masterwallet.Build"
```

run the server to watch for pushes
```
sbt run
```