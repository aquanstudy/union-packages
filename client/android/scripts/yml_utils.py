# -*- coding: utf-8 -*-
# Author:xiaohei
# CreateTime:2018-11-16
#
# All operations for apktool.yml
#
#

import os
import os.path
from xml.etree import ElementTree as ET
from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import ElementTree
from xml.dom import minidom
import log_utils
import file_utils


def parse_version_info(ymlPath):
    """
        parse versionCode and versionName in apktool.yml
    """

    if not os.path.exists(ymlPath):
        log_utils.warning("the apktool.yml is not exists " + ymlPath)
        return ("0", "1.0.0")

    ymlFile = open(ymlPath, 'r')
    lines = ymlFile.readlines()
    ymlFile.close()

    versionCode = "0"
    versionName = "1.0.0"

    for line in lines:
        if 'versionCode' in line:
            versionCode = line.replace('versionCode:', '').strip().replace("'", "")

        elif 'versionName' in line:
            versionName = line.replace('versionName:', '').strip().replace("'", "")

    return (versionCode, versionName)


def modify_version_info(ymlPath, newPackageName, versionCode, versionName, minSdkVersion, targetSdkVersion,
                        maxSdkVersion):
    """
        modify version info in apktool.yml
    """

    if not os.path.exists(ymlPath):
        log_utils.warning("the apktool.yml is not exists " + ymlPath)
        return

    minSdkStr = None
    targetSdkStr = None
    maxSdkStr = None

    if minSdkVersion != None:
        minSdkStr = "minSdkVersion: '" + minSdkVersion + "'"

    if targetSdkVersion != None:
        targetSdkStr = "targetSdkVersion: '" + targetSdkVersion + "'"

    if maxSdkVersion != None:
        maxSdkStr = "maxSdkVersion: '" + maxSdkVersion + "'"

    ymlFile = open(ymlPath, 'r')
    lines = ymlFile.readlines()
    ymlFile.close()

    newLines = []
    for line in lines:
        if 'versionCode' in line and versionCode is not None:
            newLines.append("  versionCode: '" + versionCode + "'\n")
        elif 'versionName' in line and versionName is not None:
            newLines.append("  versionName: " + versionName + "\n")
        elif 'sdkInfo' in line:
            continue
        elif 'minSdkVersion' in line:
            if minSdkVersion == None:
                minSdkStr = line.strip()
        elif 'targetSdkVersion' in line:
            if targetSdkVersion == None:
                targetSdkStr = line.strip()
        elif 'maxSdkVersion' in line:
            if maxSdkVersion == None:
                maxSdkStr = line.strip()
        elif 'renameManifestPackage' in line and ('null' not in line):
            newLines.append("  renameManifestPackage: " + packageName + "\n")

        else:
            newLines.append(line)

    if minSdkVersion != None or targetSdkVersion != None or maxSdkVersion != None:
        newLines.append('sdkInfo:\n')
        if minSdkVersion != None:
            newLines.append("  " + minSdkStr + "\n")

        if targetSdkVersion != None:
            newLines.append("  " + targetSdkStr + "\n")

        # if maxSdkVersion != None:
        #     newLines.append("  "+maxSdkStr + "\n")

    content = ''
    for line in newLines:
        content = content + line

    ymlFile = open(ymlPath, 'w')
    ymlFile.write(content)
    ymlFile.close()


def add_compress_regx(ymlPath, compressRegx):
    """
        remove matched compress types from doNotCompress tag
    """

    if compressRegx == None or len(compressRegx) == 0:
        return

    if not os.path.exists(ymlPath):
        log_utils.warning("the apktool.yml is not exists " + ymlPath)
        return

    ymlFile = open(ymlPath, 'r')
    lines = ymlFile.readlines()
    ymlFile.close()

    handlingCompress = False

    newLines = []
    for line in lines:

        if 'doNotCompress:' in line:
            handlingCompress = True
            newLines.append(line)
        elif handlingCompress and line.startswith('-'):
            currLine = line[1:].strip()
            matchs = [c for c in compressRegx if c == currLine]
            if len(matchs) <= 0:
                newLines.append(line)

        else:
            handlingCompress = False
            newLines.append(line)

    content = ''
    for line in newLines:
        content = content + line

    ymlFile = open(ymlPath, 'w')
    ymlFile.write(content)
    ymlFile.close()


def add_uncompress_regx(ymlPath, uncompressRegx):
    """
        add uncompress types into doNotCompress
    """

    if uncompressRegx == None or len(uncompressRegx) == 0:
        return

    if not os.path.exists(ymlPath):
        log_utils.warning("the apktool.yml is not exists " + uncompressRegx)
        return

    ymlFile = open(ymlPath, 'r')
    lines = ymlFile.readlines()
    ymlFile.close()

    handlingCompress = False

    existsUnCompressSet = list()

    for line in lines:

        if 'doNotCompress:' in line:
            handlingCompress = True
        elif handlingCompress and line.startswith('-'):
            currLine = line[1:].strip()
            existsUnCompressSet.append(currLine)

        else:
            handlingCompress = False

    appendItems = list()
    for uncompressItem in uncompressRegx:

        matchItems = [c for c in existsUnCompressSet if c == uncompressItem]

        if len(matchItems) > 0:
            continue

        appendItems.append(uncompressItem)

    if len(appendItems) > 0:

        newLines = list()
        for line in lines:

            if 'doNotCompress:' in line:
                newLines.append(line)

                for aitem in appendItems:
                    newLines.append("- " + aitem + "\n")

            else:
                newLines.append(line)

        content = ''
        for line in newLines:
            content = content + line

        ymlFile = open(ymlPath, 'w')
        ymlFile.write(content)
        ymlFile.close()


def modify_doNotCompress(ymlPath, uncompressRegx):
    """
        modify uncompress types into doNotCompress
    """

    if uncompressRegx == None or len(uncompressRegx) == 0:
        return

    if not os.path.exists(ymlPath):
        log_utils.warning("the apktool.yml is not exists " + uncompressRegx)
        return

    ymlFile = open(ymlPath, 'r')
    lines = ymlFile.readlines()
    ymlFile.close()

    if 'arsc' not in uncompressRegx:
        uncompressRegx.insert(0, 'arsc')

    newLines = list()
    inDoNotCompress = False
    for line in lines:

        if 'doNotCompress:' in line:
            newLines.append(line)
            inDoNotCompress = True

            for aitem in uncompressRegx:
                newLines.append("- " + aitem + "\n")

        elif line.strip().startswith('-') and inDoNotCompress:
            continue

        else:
            inDoNotCompress = False
            newLines.append(line)

    content = ''
    for line in newLines:
        content = content + line

    ymlFile = open(ymlPath, 'w')
    ymlFile.write(content)
    ymlFile.close()
