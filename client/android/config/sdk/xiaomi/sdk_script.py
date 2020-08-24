import file_utils
import os
import os.path
from xml.etree import ElementTree as ET
from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import ElementTree
import os
import os.path
import zipfile
import re
import subprocess
import platform
from xml.dom import minidom
import codecs
import sys
import sdk_helper

androidNS = 'http://schemas.android.com/apk/res/android'

def execute(channel, decompileDir, packageName):
	
    manifestFile = decompileDir + "/AndroidManifest.xml"

    file_utils.modifyFileContent(manifestFile, "${applicationId}", packageName)


    # manifestFile = file_utils.getFullPath(manifestFile)
    # ET.register_namespace('android', androidNS)
    # key = '{' + androidNS + '}launchMode'
    # nameKey = '{' + androidNS + '}name'
    # locationKey = '{'+androidNS+'}installLocation'

    # tree = ET.parse(manifestFile)
    # root = tree.getroot()

    # root.attrib[locationKey] = 'auto'

    # applicationNode = root.find('application')
    # if applicationNode is None:
    #     return 1

    # activityNodeLst = applicationNode.findall('activity')
    # if activityNodeLst is None:
    #     return 1

    # for activityNode in activityNodeLst:
    #     bMain = False
    #     intentNodeLst = activityNode.findall('intent-filter')
    #     if intentNodeLst is None:
    #         break

    #     for intentNode in intentNodeLst:
    #         bFindAction = False
    #         bFindCategory = False

    #         actionNodeLst = intentNode.findall('action')
    #         if actionNodeLst is None:
    #             break
    #         for actionNode in actionNodeLst:
    #             if actionNode.attrib[nameKey] == 'android.intent.action.MAIN':
    #                 bFindAction = True
    #                 break

    #         categoryNodeLst = intentNode.findall('category')
    #         if categoryNodeLst is None:
    #             break
    #         for categoryNode in categoryNodeLst:
    #             if categoryNode.attrib[nameKey] == 'android.intent.category.LAUNCHER':
    #                 bFindCategory = True
    #                 break

    #         if bFindAction and bFindCategory:
                
    #             activityNode.set(key, 'standard')               

    #             break

    # tree.write(manifestFile, 'UTF-8')    
