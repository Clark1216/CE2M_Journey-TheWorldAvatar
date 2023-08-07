#!/bin/bash

#
# Use this script to run the example visualisation. Note that this uses the version of the
# DTVF currently listed in the "../library/VERSION" file, which may mean you have to
# build and push the "dtvf-base-image" first.
#

# Read the version
VERSION=`cat -s "../library/VERSION" 2>/dev/null`
echo "Determined DTVF version as: $VERSION"

# Write the '.env' file
echo "TAG=$VERSION" > .env
echo "Written .env file"

# Run docker compose build command
echo "Running the example Mapbox visualisation..."
docker compose -f docker-compose.yml up -d

# Clean up
rm .env