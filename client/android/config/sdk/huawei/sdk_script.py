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
import log_utils

androidNS = 'http://schemas.android.com/apk/res/android'

def execute(channel, decompileDir, packageName):
	
    manifestFile = decompileDir + "/AndroidManifest.xml"

    file_utils.modifyFileContent(manifestFile, "${applicationId}", packageName)

    # appId = sdk_helper.getSdkParamByKey(channel, 'HuaWei_AppID')
    # cpId = sdk_helper.getSdkParamByKey(channel, 'HuaWei_CPID')

    # log_utils.debug("huawei appId:"+appId+";cpId:"+cpId)

    # sdk_helper.addOrUpdateMetaData(decompileDir, 'com.huawei.hms.client.appid', 'appid='+appId)
    # sdk_helper.addOrUpdateMetaData(decompileDir, 'com.huawei.hms.client.cpid', 'cpid='+cpId)
    
