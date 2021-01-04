#!/bin/bash

curl -X PUT -H "Content-Type: application/json" -d '{"value": 152.123}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.1"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 155.456}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.2"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 150.789}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.3"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 148.012}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.4"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 325.345}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.1"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 360.678}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.2"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 326.901}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.3"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 345.234}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.4"

sleep 60

# mote 1.1 passe de éteint à allumé
curl -X PUT -H "Content-Type: application/json" -d '{"value": 352.123}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.1"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 155.456}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.2"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 150.789}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.3"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 148.012}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.4"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 325.345}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.1"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 360.678}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.2"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 326.901}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.3"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 345.234}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.4"

sleep 60

# pas de changements
curl -X PUT -H "Content-Type: application/json" -d '{"value": 352.123}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.1"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 155.456}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.2"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 150.789}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.3"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 148.012}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.4"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 325.345}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.1"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 360.678}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.2"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 326.901}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.3"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 345.234}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.4"

sleep 60

# mote 2.x passent de allumés à éteints
curl -X PUT -H "Content-Type: application/json" -d '{"value": 352.123}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.1"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 155.456}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.2"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 150.789}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.3"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 148.012}' "http://localhost:8080/iotlab/rest/data/1/light1/last/1.4"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 125.345}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.1"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 160.678}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.2"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 126.901}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.3"
curl -X PUT -H "Content-Type: application/json" -d '{"value": 145.234}' "http://localhost:8080/iotlab/rest/data/1/light1/last/2.4"

