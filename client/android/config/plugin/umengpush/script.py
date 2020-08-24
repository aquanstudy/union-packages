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

def execute(channel, pluginInfo, decompileDir, packageName):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    file_utils.modifyFileContent(manifestFile, '${applicationId}', packageName)

    if 'params' in pluginInfo and len(pluginInfo['params']) > 0:
        for param in pluginInfo['params']:
            name = param.get('name')
            if name == 'UM_PUSH_CHANNEL':
                param['value'] = channel['name']
                break


    #sdk_helper.handleAutoResDefined(decompileDir, 'android.support.v7.appcompat')

    sdk_helper.modifyRootApplicationExtends(decompileDir, 'com.u8.sdk.UmengProxyApplication')

    return 0

