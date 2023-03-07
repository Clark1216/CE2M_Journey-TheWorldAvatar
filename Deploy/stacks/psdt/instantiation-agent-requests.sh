#!/bin/bash

## House 45 utilities
curl -X POST --header "Content-Type: application/json" -d "{'clientProperties':'TIMESERIES_CLIENTPROPERTIES'}" localhost:3838/historical-house45-utilities-agent/retrieve

## Thingspeak solar sensor
curl -X POST --header "Content-Type: application/json" -d "{\"agentProperties\":\"THINGSPEAK_AGENTPROPERTIES\",\"apiProperties\":\"THINGSPEAK_APIPROPERTIES\",\"clientProperties\":\"THINGSPEAK_CLIENTPROPERTIES\"}" http://localhost:3838/thingspeak-agent/retrieve

## Hugo Ball (NB iriPrefix can be changed to a full URI)
curl -X POST --header "Content-Type: application/json" -d "{'timeHeader':'TimeStamp', 'iriPrefix':'hugoball/', 'addTriple':'False'}" http://localhost:3838/historical-pump-data-instantiation-agent/run

## Pumping stations (NB iriPrefix can be changed to a full URI)
curl -X POST --header "Content-Type: application/json" -d "{'timeHeader':'Year', 'iriPrefix':'pump/', 'addTriple':'True', 'multiTSColIndex': '0'}" http://localhost:3838/historical-pump-data-instantiation-agent/run

## District heating
curl -X POST --header "Content-Type: application/json" -d "{\"endpoint\":\"http://psdt-access-agent:8080/districtheating\"}" http://localhost:3838/district-heating-agent/performheatupdate

## Sewage system
curl -X POST --header "Content-Type: application/json" -d "{\"endpoint\":\"http://psdt-access-agent:8080/sewagenetwork\"}"  http://localhost:3838/sewage-network-agent/performsewageupdate

## ZIMEN measurement station
curl -X POST --header "Content-Type: application/json" -d "{\"agentProperties\":\"HISTORICAL_AGENTPROPERTIES\",\"connectorProperties\":\"HISTORICAL_CONNECTORPROPERTIES\",\"clientProperties\":\"HISTORICAL_CLIENTPROPERTIES\"}" http://localhost:3838/historical-pirmasens-station-agent/retrieve

## Solarkataster
curl -X POST --header "Content-Type: application/json" -d "{'table':'solarthermie','chunk':50}" http://localhost:10101/solarkataster_agent/run
