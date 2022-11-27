# Description

This repository contains several (helper) scripts to support the finalisation of a MVP of the King'sLynn use case required for the Derivation Paper. This is **NOT** an agent; however, multiple parts of the scripts developed here will be migrated into agents actually used in the (not simplified/mocked) King's Lynn use case.

The details provided here are mainly for documentation of the overall workflow to enable the use case MVP and ensure reproducibility.


# Requirements

- The following scripts have been tested with Python >3.9
- To use `py4jps` one also needs [Java 11] installed
- As this instantiation uses Blazegraph and PostgreSQL running in Docker containers, you need to have Docker installed on your machine. Details on how to set up a [Docker environment] can be found in the TWA wiki. Furthermore, access to the [CMCL Docker image registry] is required.


## Installation of required packages

It is highly recommended to use a [virtual environment], which can be created as follows:

`(Windows)`
```cmd
$ python -m venv deriv_venv
$ deriv_venv\Scripts\activate.bat
(deriv_venv) $
```

To install required packages, run the following command:

```bash
# build and install
(deriv_venv) $ python -m pip install -r requirements.txt
```

## Spinning up Docker Stack

A [docker-compose_stack.yml] file is provided to spin up a stack with a Blazegraph and a PostgreSQL container. Both PostgreSQL and Blazegraph use volumes to ensure data persistence. To spin up the stack, run the following command from the same directory where this README is located:
```bash
# Spin up container stack
docker-compose -f "docker-compose_stack.yml" up -d
```

# Workflow

## 1. Data Preparation

Upload previously instantiated properties required for the use case. Either download the [consolidated and labeled triples] and place them into the [data] folder before running [data_preparation.py] as main script or follow steps 1.1. - 1.4 below.

## 1.1 Consolidate previously exported triples

This minimum demonstration example is based on two previously exported sets of triples: One instantiation of properties (i.e. buildings and flats) includes their geospatial location (as points) and the other contains previous sales transactions (if available). Both files can be found in the [kg_data folder] on Dropbox. The data in both files have been consolidated by matching properties based on their identifiers, which matches for buildings with available EPC, and hence address and sales transaction data. The [consolidated triples] can also be found on Dropbox and the SPARQL `matching_query` is provided in the [resources] folder (for reference). **Please note** that the _triples_file_ in [data_preparation.py] needs to be updated accordingly.

## 1.2 Instantiate consolidated triples

Before starting the instantiation, ensure that the properties in [configs.py] match the settings in the `docker-compose_stack.yml` file. Then download the [consolidated triples] file and place it into the [data] folder (filename to be specified in [data_preparation.py]).

Then simply run [data_preparation.py] as main script.

## 1.3 Identify buildings within flood polygon

QGIS is used to identify buildings within the flood warning polygon of interest. The QGIS project file as well as the required (geospatial) input files can be found in the [geospatial_analysis folder] on Dropbox:
- `flood-areas.geojson`: one flood warning polygon as GeoJSON file (same as version-controlled version in [data] folder here) 
- `building_locations.csv`: point locations of instantiated properties as created by `extract_property_locations` method in [data_preparation.py]
- `affected_property_iris.csv`: one-column csv file of all buildings within the flood warning polygon (same as version-controlled version in [data] folder here) 

Rough QGIS workflow to identify buildings within flood polygon and create `affected_property_iris.csv`:

1. Add `flood-areas.geojson` and `building_locations.csv` as vector layers
2. Create points from csv by (settings to use: 'EPSG:4326', 'XFIELD' : 'longitude', 'YFIELD' : 'latitude', 'ZFIELD' : ''):
    > Processing Toolbox > Create points layer from table >
3. Identify properties within flood polygon by:
    > Processing Toolbox > Extract by location
4. Export affected properties as csv

## 1.4 Label affected properties in Blazegraph

There are 222 buildings being affected by the (hypothetical) flood event. Running the `attach_labels` function in [data_preparation.py] will attach an `affected` label (specified in `data_preparation.py`) to all property IRIs listed in the [affected_property_iris] csv file. This will be used to mock the geospatial queries to obtain buildings within a flood polygon.

## 2. Initialise Property Price Index

Running the [data_preparation.py] module as main script also initialises the Property Price Index (PPI) in both KG and RDB. The initially uploaded PPI data only includes values until August 2022 (i.e. excluding the most recent month at time of writing, September 2022). To update the instantiated Property Price Index and hence trigger a second cascade of derivations, run [property_price_index.py] as main script.


# Authors #
Markus Hofmeister (mh807@cam.ac.uk), November 2022


<!-- Links -->
[Java 11]: https://adoptium.net/en-GB/temurin/releases/?version=11
[virtual environment]: https://docs.python.org/3/tutorial/venv.html
[Docker environment]: https://github.com/cambridge-cares/TheWorldAvatar/wiki/Docker%3A-Environment
[CMCL Docker image registry]: https://github.com/cambridge-cares/TheWorldAvatar/wiki/Docker%3A-Image-registry

<!-- Data -->
[kg_data folder]: https://www.dropbox.com/home/CoMo%20shared/mh807/DerivationPaper/kg_data
[geospatial_analysis folder]: https://www.dropbox.com/home/CoMo%20shared/mh807/DerivationPaper/geospatial_analysis
[consolidated triples]: https://www.dropbox.com/home/CoMo%20shared/mh807/DerivationPaper/kg_data?preview=consolidated_properties.nt
[consolidated and labeled triples]: https://www.dropbox.com/home/CoMo%20shared/mh807/DerivationPaper/kg_data?preview=consolidated_and_labeled_properties.nt

[resources]: resources
[configs.py]: configs.py
[data]: data
[data_preparation.py]: data_preparation.py
[property_price_index.py]: property_price_index.py
[affected_property_iris]: data/affected_property_iris.csv