"""
# Author: qhouyee, picas9dan #

This module provides helper methods to generate separate tilesets and write to a json file.
"""

# Standard library imports
import json
from pathlib import Path
from typing import List, Union, Optional, Iterable
import os

# Third-party imports
import numpy as np
import trimesh
from py4jps import agentlogging

# Self imports
import agent.app as state
from agent.ifc2tileset.schema import Tileset, Tile
from agent.kgutils.const import ID_VAR, IRI_VAR, NAME_VAR

# Retrieve logger
logger = agentlogging.get_logger("dev")

def append_content_metadata_schema(tileset: Tileset):
    """Appends the schema of content metadata for building and asset to the tileset."""

    tileset["schema"] = {"classes": {
        "ContentMetaData": {
            "name": "Content metadata",
            "description": "A metadata class for all content including building and individual assets",
            # Store all content and asset information here even if they are not used
            "properties": {
                NAME_VAR: {
                    "description": "Name of the asset/building",
                    "type": "STRING"
                },
                ID_VAR: {
                    "description": "Unique identifier generated in IFC",
                    "type": "STRING"
                },
                IRI_VAR: {
                    "description": "Data IRI of the asset/building",
                    "type": "STRING"
                }
            }
        }
    }}

def make_root_tile(bbox: Optional[List[float]] = None, geometry_file_paths: Optional[List[str]] = []):
    """Generates a root tile with the provided arguments and default values. The root tile will only
    include geometry content for non-asset elements like the building, furniture, and solar panels.

    Args:
        bbox (optional): A 12-element list that represents Next tileset's boundingVolume.box property. Defaults to None.
        geometry_file_paths (optional): A list of geometry file paths if available to be appended. Defaults to an empty list.

    Returns:
        A root tile.
    """
    bounding_volume = {"box": bbox} if bbox is not None else {}
    root_tile = Tile(
        boundingVolume=bounding_volume,
        geometricError=512,
        refine="ADD",
    )

    # If there are geometry contents available
    if geometry_file_paths:
        # And if there is only one item in the list, use the "content" nomenclature
        if len(geometry_file_paths) == 1:
            root_tile["content"] = {"uri": geometry_file_paths[0]}
        else:
            # If there are more than one item, use the "contents" nomenclature with a list
            # Sample format: {"contents": [{"uri":"path1"}, {"uri":"path2"}]}
            root_tile["contents"] = []
            for path in geometry_file_paths:
                root_tile["contents"].append({"uri": path})

    return root_tile


def make_tileset(root_tile: Tile):
    """Generates a tileset that wraps around the provided root tile, with default properties.

    Args:
        root_tile: A tile.

    Returns:
        A tileset.
    """
    return Tileset(
        asset={"version": "1.1"},
        geometricError=1024,
        root=root_tile
    )


def y_up_to_z_up(x_min: float, y_min: float, z_min: float, x_max: float, y_max: float, z_max: float):
    """Transforms a bounding box's extreme coordinates in the y-up system to the z-up system.
    """
    return x_min, -z_max, y_min, x_max, -z_min, y_max


PathLike = Union[str, bytes, os.PathLike]


def compute_bbox(gltf: Union[PathLike, Iterable[PathLike]], offset: float = 0):
    """Computes Next tileset bbox for a given glTF/glb file(s).

    The y-up coordinate system of glTF will be transformed to the z-up system of Next tileset.

    Args:
        gltf: A path or a list of paths to glTF/glb file(s).
        offset: Amount of x- and y-offsets.

    Returns:
        A 12-element list that represents Next tileset's boundingVolume.box property.
    """
    if not isinstance(gltf, list):
        gltf = [gltf]

    meshes = [trimesh.load(file, force="mesh") for file in gltf]
    vertices = np.vstack([mesh.vertices for mesh in meshes])

    mins = vertices.min(axis=0)
    maxs = vertices.max(axis=0)

    # Converts the y-up coordinate system of glTF to the z-up coordinate of Next tileset
    x_min, y_min, z_min, x_max, y_max, z_max = y_up_to_z_up(*mins, *maxs)

    x_min -= offset
    y_min -= offset
    x_max += offset
    y_max += offset

    return [
        (x_min + x_max) / 2, (y_min + y_max) / 2, (z_min + z_max) / 2,
        (x_max - x_min) / 2, 0, 0,
        0, (y_max - y_min) / 2, 0,
        0, 0, (z_max - z_min) / 2
    ]


def gen_solarpanel_tileset():
    """Generates and write the tileset for solar panel into 3D Tiles Next format if it exists.
    """
    solarpanel_file_path = "./data/glb/solarpanel.glb"
    solarpath = Path(solarpanel_file_path)

    if not solarpath.is_file():
        return

    bbox = compute_bbox(solarpath)
    root_tile = make_root_tile(
        bbox=bbox, geometry_file_paths=[state.asset_url + "solarpanel.glb"])
    tileset = make_tileset(root_tile)

    jsonwriter(tileset, "tileset_solarpanel")


def gen_sewagenetwork_tileset():
    """Generates and writes the tileset for sewage network into 3D Tiles Next format if it exists.
    """
    sewage_file_path = "./data/glb/sewagenetwork.glb"
    sewagepath = Path(sewage_file_path)

    if not sewagepath.is_file():
        return

    bbox = compute_bbox(sewagepath)
    root_tile = make_root_tile(
        bbox=bbox,  geometry_file_paths=[state.asset_url + "sewagenetwork.glb"])
    tileset = make_tileset(root_tile)

    jsonwriter(tileset, "tileset_sewage")


def jsonwriter(tileset: Tileset, filename: str):
    """Writes a tileset object into 3D Tiles Next file in JSON format.

    Args:
        tileset: A tileset object.
        filename: The output tileset's filename.
    """
    filepath = os.path.join("data", filename + ".json")

    with open(filepath, 'w', encoding="utf-8") as outfile:
        json.dump(tileset, outfile)

    logger.info(filename + ".json have been generated")
