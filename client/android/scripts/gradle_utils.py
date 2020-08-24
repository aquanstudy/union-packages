# -*- coding: utf-8 -*-
# Author:xiaohei
# CreateTime:2019-04-23
#
# handle gradle dependencies
#
#

import file_utils
import os
import os.path
import config_utils
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
import shutil
import time
import log_utils


def parse_excludes(excludes):
    result = ""
    log_utils.debug("excludes:" + excludes)

    for t in excludes.split(','):
        t = t.strip()

        excludeItem = t.split(':')
        log_utils.debug("excludes:" + str(excludeItem))
        result += '\n\t\texclude group: \"' + excludeItem[0] + '\"'

        if len(excludeItem) > 1:
            result += ", module: \"" + excludeItem[1] + '\"'

    return result


def modify_gradle_dependencies(projPath, dependencyList):
    dependencyStr = ""

    for d in dependencyList:

        name = d['name']
        excludes = None

        if 'excludes' in d:
            excludes = d['excludes']

        name = name.strip()
        if excludes == None:
            dependencyStr += "scm '" + name + "'\n\t"
        else:
            dependencyStr += "scm('" + name + "'){\n\t" + parse_excludes(excludes)
            dependencyStr += "\n\t}\n\t"

    buildPath = os.path.join(projPath, 'app/build.gradle')
    if not os.path.exists(buildPath):
        log_utils.error("build.gradle is not exists: %s", buildPath)
        return False

    file_utils.modifyFileContent(buildPath, "${U8_DEPENDENCIES_PLACEHOLDER}", dependencyStr)
    return True


def modify_gradle_dependencies_path(projPath, path):
    buildPath = os.path.join(projPath, 'app/build.gradle')
    if not os.path.exists(buildPath):
        log_utils.error("build.gradle is not exists: %s", buildPath)
        return False

    file_utils.modifyFileContent(buildPath, "${U8_SDK_LIBS_FOLDER}", path)
    return True


def exec_gradle_task(tempProjDir):
    lastExecDir = os.getcwd()
    os.chdir(tempProjDir)

    cmd = "./gradlew copyDeps"
    if platform.system() == 'Windows':
        cmd = "gradlew copyDeps"

    ret = file_utils.exec_cmd(cmd)

    os.chdir(lastExecDir)

    return ret


def handle_dependencies(sdkDir, dependencyList):
    tempProjDir = os.path.join(os.path.dirname(sdkDir), "U8_Temp_AS_Project")

    if not os.path.exists(tempProjDir):
        log_utils.error("gradle temp project not exists:%s", tempProjDir)
        return False

    ret = modify_gradle_dependencies(tempProjDir, dependencyList)
    if not ret:
        return False

    ret = modify_gradle_dependencies_path(tempProjDir, file_utils.formatPath(os.path.join(sdkDir, "libs")))
    if not ret:
        return False

    ret = exec_gradle_task(tempProjDir)

    if not ret:
        return False

    return ret
