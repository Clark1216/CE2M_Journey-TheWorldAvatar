##########################################
# Author: Wanni Xie (wx243@cam.ac.uk)    #
# Last Update Date: 20 April 2021        #
##########################################

"""This module declare the properties of generating UK Power Plant A-boxes"""

class UKPowerPlant:
    
    """Default path of storing owl file """
    StoreGeneratedOWLs = "C:\\Users\\wx243\\Desktop\\KGB\\My project\\1 Ongoing\\4 UK Digital Twin\\A_Box\\UK_Power_Plant\\"
    
    """Node keys"""
    RealizationAspectKey = "PowerGenerator_"
    RequirementsAspectKey = "DesignCapacity_"
    BuiltYearKey = "YearOfBuilt_"
    OwnerKey = "Owner_"
    CoordinateSystemKey = "CoordinateSystem_"
    LongitudeKey = "y_coordinate_" 
    LantitudeKey = "x_coordinate_" 
    
    
    valueKey = "value_"
