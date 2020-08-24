# -*- coding: utf-8 -*-
# Author:xiaohei
# CreateTime:2018-11-09
#
# aar handler
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
from PIL import Image
import image_utils
import log_utils
import assets_merger
from res_merger import ResourceMerger2
import manifest_merger
import manifest_utils

androidNS = 'http://schemas.android.com/apk/res/android'


class AARMerger(object):
    """aar operations"""

    TEMP_DIR_PREFIX = "temp-"

    def __init__(self, aarFile):
        super(AARMerger, self).__init__()
        self.aarFile = aarFile
        self.aarName = os.path.basename(self.aarFile)
        aarPath = os.path.dirname(self.aarFile)
        self.aarDir = os.path.join(aarPath, AARMerger.TEMP_DIR_PREFIX + self.aarName)
        self.needR = True
        self.packageName = ""

    def is_need_R(self):
        return self.needR

    def get_package_name(self):
        return self.packageName

    def merge(self, targetAndroidManifest, targetAssets, targetRes, targetLibs):
        ret = self.do_merge(targetAndroidManifest, targetAssets, targetRes, targetLibs)
        file_utils.del_file_folder(self.aarDir)
        file_utils.del_file_folder(self.aarFile)
        return ret

    def do_merge(self, targetAndroidManifest, targetAssets, targetRes, targetLibs):
        ret = self.unarchive()

        if not ret:
            return False

        ret = self.merge_manifest(targetAndroidManifest)

        if not ret:
            return False

        ret = self.merge_assets(targetAssets)

        if not ret:
            return False

        ret = self.merge_jars(targetLibs)

        if not ret:
            return False

        ret = self.merge_jni(targetLibs)

        if not ret:
            return False

        ret = self.merge_res(targetRes)

        if not ret:
            return False

        return True

    def unarchive(self):

        if not os.path.exists(self.aarFile):
            log_utils.error("the aar file not exists:" + self.aarFile)
            return False

        if os.path.exists(self.aarDir):
            file_utils.del_file_folder(self.aarDir)

        file_utils.unzip_file(self.aarFile, self.aarDir)

        return True

    def merge_assets(self, targetAssets):

        if not os.path.exists(self.aarDir):
            log_utils.warning("the aar is not unarchived:" + self.aarFile)
            return False

        assetPath = os.path.join(self.aarDir, "assets")

        if not os.path.exists(assetPath):
            log_utils.debug("aar assets merge completed. there is no assets folder in " + self.aarFile)
            return True

        assets_merger.merge(assetPath, targetAssets)

        return True

    def merge_jars(self, targetLibs):

        if not os.path.exists(self.aarDir):
            log_utils.warning("the aar is not unarchived:" + self.aarFile)
            return False

        classesPath = os.path.join(self.aarDir, "classes.jar")
        if os.path.exists(classesPath):
            targetPath = os.path.join(targetLibs, self.aarName + ".jar")
            file_utils.copy_file(classesPath, targetPath)
            log_utils.debug("classes.jar in aar " + self.aarFile + " copied to " + targetPath)

        libsPath = os.path.join(self.aarDir, "libs")

        if not os.path.exists(libsPath):
            log_utils.debug("aar libs merge completed. there is no libs folder in " + self.aarFile)
            return True

        for f in os.listdir(libsPath):

            if f.endswith(".jar"):

                targetName = self.aarName + "." + f
                targetName = targetName.replace(" ", "")  # //remove spaces in name

                targetPath = os.path.join(targetLibs, targetName)  # rename jar in aar libs folder with aar name prefix.

                if os.path.exists(targetPath):
                    log_utils.error(
                        "libs in aar " + self.aarFile + " merge failed. " + f + " already exists in " + targetLibs)
                    return False

                file_utils.copy_file(os.path.join(libsPath, f), targetPath)
                log_utils.debug(f + " in aar " + self.aarFile + " copied to " + targetLibs)

        return True

    def merge_jni(self, targetLibs):

        if not os.path.exists(self.aarDir):
            log_utils.warning("the aar is not unarchived:" + self.aarFile)
            return False

        jniPath = os.path.join(self.aarDir, "jni")

        if not os.path.exists(jniPath):
            log_utils.debug("aar jni merge completed. there is no jni folder in " + self.aarFile)
            return True

        for f in os.listdir(jniPath):

            cpuPath = os.path.join(jniPath, f)

            for c in os.listdir(cpuPath):
                cpuTargetPath = os.path.join(targetLibs, f, c)
                if os.path.exists(cpuTargetPath):
                    log_utils.error(
                        "jni in aar " + self.aarFile + " merge failed. " + c + " already exists in " + targetLibs)
                    return False

                file_utils.copy_file(os.path.join(cpuPath, c), cpuTargetPath)
                log_utils.debug(f + "/" + c + " in aar " + self.aarFile + " copied to " + targetLibs)

        return True

    def merge_res(self, targetRes):

        if not os.path.exists(self.aarDir):
            log_utils.warning("the aar is not unarchived:" + self.aarFile)
            return False

        resPath = os.path.join(self.aarDir, "res")

        if not os.path.exists(resPath):
            self.needR = False
            log_utils.debug("aar res merge completed. there is no res folder in " + self.aarFile)
            return True

        resFiles = file_utils.list_files(resPath, [], [])
        if len(resFiles) == 0:
            self.needR = False
            log_utils.debug("aar res merge completed. there is no res file in " + self.aarFile)
            return True

        resPaths = [resPath, targetRes]

        ResourceMerger2.merge(resPaths)
        log_utils.debug("res in aar " + self.aarFile + " merged into " + targetRes)

        return True

    def merge_manifest(self, targetManifest):

        if not os.path.exists(self.aarDir):
            log_utils.warning("the aar is not unarchived:" + self.aarFile)
            return False

        manifestPath = os.path.join(self.aarDir, "AndroidManifest.xml")

        if not os.path.exists(manifestPath):
            self.needR = False
            log_utils.debug("there is no AndroidManifest.xml in " + manifestPath)
            return True

        self.packageName = manifest_utils.get_package_name(manifestPath)

        return manifest_merger.merge2(manifestPath, targetManifest)


def add_extraR(aarPath, channel, packageName):
    if packageName == None or len(packageName) == 0:
        return

    if "extraRList" not in channel:
        channel["extraRList"] = []

    channel['extraRList'].append(packageName)
    log_utils.debug("add a new extra R.java in package:[" + packageName + "] in aar:" + aarPath)


def merge_sdk_aar(channel, aarPath, targetManifest, targetAssets, targetRes, targetLibs):
    merger = AARMerger(aarPath)
    ret = merger.merge(targetManifest, targetAssets, targetRes, targetLibs)

    if not ret:
        log_utils.error("aar handle failed. " + aarPath)
        return False

    if merger.is_need_R():
        add_extraR(aarPath, channel, merger.get_package_name())

    return True


def handle_sdk_aars(channel, sdkPath, manifestName):
    if not os.path.exists(sdkPath):
        log_utils.error("the sdk path not exists:" + sdkPath)
        return False

    targetAssets = os.path.join(sdkPath, 'assets')

    if not os.path.exists(targetAssets):
        os.makedirs(targetAssets)

    targetManifest = os.path.join(sdkPath, manifestName)  # manifest name in sdk folder

    if not os.path.exists(targetManifest):
        log_utils.error("target SDKManifest.xml not exists. this file should exists in sdk config folder")
        return False

    targetLibs = os.path.join(sdkPath, 'libs')

    if not os.path.exists(targetLibs):
        os.makedirs(targetLibs)

    targetRes = os.path.join(sdkPath, 'res')

    if not os.path.exists(targetRes):
        os.makedirs(targetRes)

    for f in os.listdir(sdkPath):

        if f.endswith(".aar"):

            aarPath = os.path.join(sdkPath, f)
            ret = merge_sdk_aar(channel, aarPath, targetManifest, targetAssets, targetRes, targetLibs)
            if not ret:
                return False

    for f in os.listdir(targetLibs):

        if f.endswith(".aar"):

            aarPath = os.path.join(targetLibs, f)
            ret = merge_sdk_aar(channel, aarPath, targetManifest, targetAssets, targetRes, targetLibs)

            if not ret:
                return False

    return True
