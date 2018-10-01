#!/bin/bash
sbt docker && docker run -d -p8029:8029 --name build-server -t pro.masterwallet/masterwallet-build-server
