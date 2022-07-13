##########################################
# Author: Wanni Xie (wx243@cam.ac.uk)    #
# Last Update Date: 12 July 2022         #
##########################################

"""This class defines the properties of DUKES data"""
from pathlib import Path

class PopulationDensityDataProperty:
    
    def __init__(self, version = 2019):
        self.VERSION = version
    
        """ File path """
        self.DataPath = str(Path(__file__).resolve().parent.parent) + "\Data files\PopulationDensity\\"
        self.PopulationDensityDataPath = self.DataPath + str(self.VERSION) + '\population_gbr.csv'

        """Header"""
        self.headerPopulationDensityData = ["Lat", "Lon", "Population"]
