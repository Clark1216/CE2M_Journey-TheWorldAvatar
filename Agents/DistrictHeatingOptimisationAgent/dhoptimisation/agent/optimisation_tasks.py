################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 25 Sep 2023                            #
################################################

# The purpose of this module is to provide the actual optimisation logic and
# methods for the DHOptimisationAgent

import pandas as pd
import CoolProp.CoolProp as CP

from dhoptimisation.utils.logger import logger
from dhoptimisation.datamodel.iris import *

