"""
# Author: qhouyee #

This module provides the root tile and its bounding boxes for all tilesets.
"""

# Standard library imports
from pathlib import Path

# Third party imports
import pandas as pd
from py4jps import agentlogging

# Self imports
import agent.app as state
import agent.config.config as properties
from agent.ifc2tileset.schema import Tileset
from agent.ifc2tileset.tile_helper import make_tileset, make_root_tile, compute_bbox

# Retrieve logger
logger = agentlogging.get_logger("dev")


def append_tileset_schema_and_metadata(tileset: Tileset, building_iri: str):
    """
    Append tileset schema.py class and metadata to tileset

    Arguments:
        tileset - the root tileset generated as a python dictionary
        building_iri - data IRI of the building
    """
    # Append definition of class and its properties to schema
    tileset["schema"] = {"classes": {
        "TilesetMetaData": {
            "name": "Tileset metadata",
            "description": "A metadata class for the tileset",
            "properties": {
                "buildingIri": {
                    "description": "Data IRI of the building",
                    "type": "STRING"
                }
            }
        }
    }}

    # Append specific tileset values to the core metadata class
    tileset["metadata"] = {
        "class": "TilesetMetaData",
        "properties": {
            "buildingIri": building_iri
        }
    }


def gen_root_content(building_iri: str, asset_data: pd.DataFrame):
    """
    Add the root content of building and background furniture to tileset
    If there are no assets, the tileset generated in this function is sufficient for visualisation

    Arguments:
        building_iri - data IRI of the building
        asset_data - dataframe containing mappings for asset metadata
    Returns:
        The tileset generated as a python dictionary
    """
    # Respective filepaths
    building_file_path = "./data/gltf/building.gltf"
    bpath = Path(building_file_path)
    furniture_file_path = "./data/gltf/furniture.gltf"
    fpath = Path(furniture_file_path)

    # In a special case where there is no building and furniture, no root content is added
    if bpath.is_file():
        building_content = {"uri": state.asset_url + "building.gltf"}

        # If there are furniture, use the multiple nomenclature
        if fpath.is_file():
            bbox = compute_bbox(["./data/glb/building.glb", "./data/glb/furniture.glb"])
            furniture_content = {"uri": state.asset_url + "furniture.gltf"}

            # Tileset Nomenclature for multiple geometry files = contents:[{}]
            root_tile = make_root_tile(bbox=bbox, contents=[furniture_content, building_content])
        else:
            bbox = compute_bbox("./data/glb/building.glb")

            # Tileset Nomenclature for 1 geometry file = content:{}
            root_tile = make_root_tile(bbox=bbox, content=building_content)
    else:
        # In the scenario where there is no building and no furniture, the root bbox should enclose all assets
        bbox = compute_bbox([f"./data/glb/{file}.glb" for file in asset_data["file"]]) \
            if not asset_data.empty \
            else properties.bbox_root
        root_tile = make_root_tile(bbox=bbox)

    tileset = make_tileset(root_tile)
    append_tileset_schema_and_metadata(tileset, building_iri)

    return tileset
