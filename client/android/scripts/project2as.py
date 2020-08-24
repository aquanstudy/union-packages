# -*- coding: utf-8 -*-
# Author:tik
# CreateTime:2014-10-25
#
# eclipse projects to as projects
#
#

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
import argparse
import shutil
import time


def copy_files(src, dest):
    if not os.path.exists(src):
        print("the src is not exists.path:" + src)
        return

    if os.path.isfile(src):
        copy_file(src, dest)
        return

    for f in os.listdir(src):
        sourcefile = os.path.join(src, f)
        targetfile = os.path.join(dest, f)
        if os.path.isfile(sourcefile):
            copy_file(sourcefile, targetfile)
        else:
            copy_files(sourcefile, targetfile)


def copy_file(src, dest):
    sourcefile = src
    destfile = dest
    if not os.path.exists(sourcefile):
        return

    destdir = os.path.dirname(destfile)
    if not os.path.exists(destdir):
        os.makedirs(destdir)

    shutil.copy(src, dest)


def projects2as(sourcePath, targetPath):
    tempProjPath = os.path.join(targetPath, 'U8SDK_ASTemplate')

    if not os.path.exists(tempProjPath):
        print("U8SDK_ASTemplate folder not exists")
        return

    settingsContent = ""
    for f in os.listdir(sourcePath):
        if not f.startswith('U8SDK_'):
            continue

        fpath = os.path.join(sourcePath, f)
        tpath = os.path.join(targetPath, f)

        if os.path.exists(tpath):
            print(tpath + " already exists.")
            continue

        copy_files(tempProjPath, tpath)

        srcPath = os.path.join(fpath, 'src')
        srcTargetPath = os.path.join(tpath, 'src/main/java')

        copy_files(srcPath, srcTargetPath)

        libPath = os.path.join(fpath, 'libs')
        libTargetPath = os.path.join(tpath, 'libs')

        copy_files(libPath, libTargetPath)

        configPath = os.path.join(fpath, 'config.xml')
        configTargetPath = os.path.join(tpath, 'config.xml')
        copy_files(configPath, configTargetPath)

        manifestPath = os.path.join(fpath, 'SDKManifest.xml')
        manifestTargetPath = os.path.join(tpath, 'SDKManifest.xml')
        copy_files(manifestPath, manifestTargetPath)

        aidlPath = os.path.join(fpath, 'aidl')
        if os.path.exists(aidlPath):
            aidlTargetPath = os.path.join(tpath, 'src/main/aidl')
            copy_files(aidlPath, aidlTargetPath)

        settingsContent += "\n" + "include ':" + f + "'"

        print(f + " handle success.")

    settingsPath = os.path.join(targetPath, 'settings.gradle')
    if os.path.exists(settingsPath):
        f = open(settingsPath, 'r')
        data = str(f.read())
        f.close()

        data += settingsContent

        f = open(settingsPath, 'w')
        f.write(data)
        f.close()
        print("settings.gradle handle success.")


if __name__ == "__main__":
    sourcePath = "./U8SDK_Projects"
    targetPath = "./U8SDK_Projects_AS/U8SDK_Projects"

    projects2as(sourcePath, targetPath)
