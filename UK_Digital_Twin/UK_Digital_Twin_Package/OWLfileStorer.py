##########################################
# Author: Wanni Xie (wx243@cam.ac.uk)    #
# Last Update Date: 21 April 2021        #
##########################################

"""This module defines the functions to store the generated owl files"""
import os
from tkinter import Tk
from tkinter.filedialog import askdirectory

"""Read data files"""
def readFile(filepath):
    ContentArrays = []
    with open(filepath) as file:
        lines = file.readlines()
        for line in lines:
            content = line.split(',')
            ContentArrays.append(content)
    return ContentArrays  

"""Store the generated owl files"""
def storeGeneratedOWLs(graph, filepath):
    if os.access(filepath, os.W_OK):
        print('file ' + filepath + ' exists')
        with open(filepath, "r+") as f:
         f.truncate()     
        graph.serialize(destination = filepath, format="application/rdf+xml")
        print('**owl file updated')
    else:
        graph.serialize(destination = filepath, format="application/rdf+xml")
        print('##owl file created: ')
        print(filepath) 


"""This function allows the user to select a folder to store the generated .owls when the default path is invalided"""
# A window will pop up when this function is called
def selectStoragePath():
    # Suppresses the root window of GUI
    Tk().withdraw()
    print('Please select a folder')
    return askdirectory()

