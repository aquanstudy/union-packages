#!/usr/bin/python
# -*- coding: utf-8 -*-
# Author xiaohei
# Date 2020-04-30

import os,os.path
import shutil,errno
import sys
import time
import codecs
import argparse
import gzip
import file_utils
import gradle_utils
import aar_utils
import log_utils
import assets_merger

from xml.etree import ElementTree as ET
from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import ElementTree

try: input = raw_input
except NameError: pass

global_ignores = [".meta", ".DS_Store", ".svn", ".git"]



def pretty_xml(element, indent, newline, level=0):  # elemnt为传进来的Elment类，参数indent用于缩进，newline用于换行
    if len(element) > 0:  # 判断element是否有子元素    
        if (element.text is None) or element.text.isspace():
            element.text = newline + indent * (level + 1)
        else:
            element.text = newline + indent * (level + 1) + element.text.strip() + newline + indent * (level + 1)

    temp = list(element)
    for subelement in temp:
        if temp.index(subelement) < (len(temp) - 1):  # 如果不是list的最后一个元素，说明下一个行是同级别元素的起始，缩进应一致
            subelement.tail = newline + indent * (level + 1)
        else:  # 如果是list的最后一个元素， 说明下一行是母元素的结束，缩进应该少一个    
            subelement.tail = newline + indent * level
        pretty_xml(subelement, indent, newline, level=level + 1)  # 对子元素进行递归操作


def read_sdk_config(sdkDir, sdk):

    configFile = os.path.join(sdkDir, 'config.xml')

    sdkObj = {}
    sdkObj['sdk'] = sdk

    if not os.path.exists(configFile):
        log_utils.error("the config.xml is not exists of sdk %s.path:%s", sdkObj['sdk'], configFile)
        return False

    try:
        tree = ET.parse(configFile)
        configNode = tree.getroot()
    except Exception as e:
        log_utils.error("can not parse config.xml.path:%s", configFile)
        log_utils.exception(e)
        return False    

    extraRNodes = configNode.find("extraR")
    if extraRNodes != None and len(extraRNodes) > 0:
        if "extraRList" not in sdkObj:
            sdkObj["extraRList"] = []

        for rNode in extraRNodes:
            name = rNode.get('name')
            if name != None and len(name) > 0 and name not in sdkObj["extraRList"]:
                sdkObj["extraRList"].append(name)
                #log_utils.debug("add a new extra R package:"+name)

    if "dependencyList" not in sdkObj:
        sdkObj["dependencyList"] = []

    dependencyNodes = configNode.find('dependencies')
    if dependencyNodes != None and len(dependencyNodes) > 0:
        for rNode in dependencyNodes:
            name = rNode.get('name')
            if name != None and len(name) > 0:
                dependencyItem = dict()
                dependencyItem["name"] = name

                excludes = rNode.get('excludes')
                if excludes != None and len(excludes) > 0:
                    dependencyItem["excludes"] = excludes

                sdkObj["dependencyList"].append(dependencyItem)

    return sdkObj


def dump_sdk_config(sdkDir, sdkObj):

    configFile = os.path.join(sdkDir, 'config.xml')


    if not os.path.exists(configFile):
        log_utils.error("the config.xml is not exists of sdk %s.path:%s", sdkObj['sdk'], configFile)
        return False

    try:
        tree = ET.parse(configFile)
        configNode = tree.getroot()
    except Exception as e:
        log_utils.error("can not parse config.xml.path:%s", configFile)
        log_utils.exception(e)
        return False    

    dependencyNodes = configNode.find('dependencies')
    if dependencyNodes != None:
        configNode.remove(dependencyNodes)

    extraRNodes = configNode.find("extraR")
    if extraRNodes != None and len(extraRNodes) > 0:
        configNode.remove(extraRNodes)

    if 'extraRList' in sdkObj and len(sdkObj['extraRList']) > 0:
        extraRNodes = SubElement(configNode, 'extraR')
        for r in sdkObj['extraRList']:
            pNode = SubElement(extraRNodes, 'package')
            pNode.set('name', r)

    pretty_xml(configNode, '\t', '\n')  # 执行美化方法
    tree.write(configFile, 'UTF-8')

    return sdkObj


def copy_assets_in_jars(sdkDir):

    libDir = os.path.join(sdkDir,'libs')
    assetsDir = os.path.join(sdkDir, 'assets')
    if not os.path.exists(assetsDir):
        os.makedirs(assetsDir)

    targetTempDir = os.path.join(os.path.dirname(sdkDir), 'temp_libs')
    if not os.path.exists(targetTempDir):
        os.makedirs(targetTempDir)

    for f in os.listdir(libDir):
        fpath = os.path.join(libDir, f)
        if f.endswith(".jar") and os.path.isfile(fpath):
            libTempPath = os.path.join(targetTempDir, f+"_temp_dir")
            file_utils.unzip_file(fpath, libTempPath)

            assetsPath = os.path.join(libTempPath, 'assets')
            if not os.path.exists(assetsPath):
                continue

            assets_merger.merge(assetsPath, assetsDir)


def copy_local(clientPath, outputPath):

    log_utils.debug("begin copy local folder:")
    sourcePath = os.path.join(clientPath, 'config/local')
    targetPath = os.path.join(outputPath, 'config/local')

    if not os.path.exists(sourcePath):
        log_utils.error("the local path not exists:" + sourcePath)
        return False

    file_utils.del_file_folder(targetPath)

    file_utils.copy_files(sourcePath, targetPath)


def copy_scripts(clientPath, outputPath):

    log_utils.debug("begin copy scripts folder:")
    sourcePath = os.path.join(clientPath, 'scripts')
    if not os.path.exists(sourcePath):
        log_utils.error("the scripts path not exists:" + sourcePath)
        return False

    targetPath = os.path.join(outputPath, 'scripts')
    file_utils.del_file_folder(targetPath)

    file_utils.copy_files(sourcePath, targetPath)
        


def copy_tool(clientPath, outputPath):

    log_utils.debug("begin copy tool folder:")
    sourcePath = os.path.join(clientPath, 'tool')
    if not os.path.exists(sourcePath):
        log_utils.error("the tool path not exists:" + sourcePath)
        return False

    targetPath = os.path.join(outputPath, 'tool')
    file_utils.del_file_folder(targetPath)

    subs = ['win', 'mac']

    for sub in subs:
        file_utils.copy_files(os.path.join(sourcePath, sub), os.path.join(targetPath, sub))


def copy_sdk(clientPath, outputPath, sdk, isPlugin = False):

    sdkFolder = 'sdk'
    if isPlugin:
        sdkFolder = 'plugin'

    sourcePath = os.path.join(clientPath, 'config/' + sdkFolder + '/' + sdk)

    if not os.path.exists(sourcePath):
        log_utils.error("the sdk path not exists:" + sourcePath)
        return False

    workspacePath = os.path.join(outputPath, 'workspace/' + sdkFolder + '/' + sdk)
    if not os.path.isabs(workspacePath):
        workspacePath = os.path.abspath(workspacePath)

    file_utils.del_file_folder(workspacePath)
    if not os.path.exists(workspacePath):
        os.makedirs(workspacePath)

    # copy sdk folder
    workSdkPath = os.path.join(workspacePath, 'sdk')
    file_utils.copy_files(sourcePath, workSdkPath)
    # handle gradle and aar
    # copy temp proj
    gradleProjDir = os.path.join(clientPath, 'config/local/U8_Temp_AS_Project')
    targetProjDir = os.path.join(workspacePath, 'U8_Temp_AS_Project')
    file_utils.copy_files(gradleProjDir, targetProjDir)

    sdkObj = read_sdk_config(workSdkPath, sdk)

    if 'dependencyList' in sdkObj and len(sdkObj['dependencyList']) > 0:
        log_utils.debug("begin to handle gradle dependencies:")
        try:
            ret = gradle_utils.handle_dependencies(workSdkPath, sdkObj['dependencyList'])
            if not ret:
                log_utils.error("gradle handle failed for :" + sdk)
                return False
        except Exception as e:
            log_utils.error("can not parse config.xml.path:" + configFile)
            log_utils.exception(e)
            return False        

    # handle aar
    log_utils.debug("begin to handle aar for " + sdk)
    manifestNames = ['SDKManifest.xml', 'SDKManifest_portrait.xml', 'SDKManifest_landscape.xml']
    for m in manifestNames:
        if not os.path.exists(os.path.join(workSdkPath, m)):
            continue
        ret = aar_utils.handle_sdk_aars(sdkObj, workSdkPath, m)
        if not ret:
            log_utils.error("handle sdk aar files failed:", sdk)
            return False

    # copy assets in jar
    log_utils.debug("begin to copy assets in jars for sdk:" + sdk)
    copy_assets_in_jars(workSdkPath)

    # dump config.xml
    dump_sdk_config(workSdkPath, sdkObj)

    targetPath = os.path.join(outputPath, 'config/'+sdkFolder+'/' + sdk)
    file_utils.del_file_folder(targetPath)
    file_utils.copy_files(workSdkPath, targetPath)
    log_utils.debug("handle sdk success for :" + sdk)
    

def get_all_names(clientPath):

    names = []
    names.append('local')
    names.append('tool')
    names.append('scripts')

    sdkPath = os.path.join(clientPath, 'config/sdk')
    if os.path.exists(sdkPath):
        for f in os.listdir(sdkPath):
            if os.path.isfile(os.path.join(sdkPath, f)):
                continue
            names.append(f)

    pluginPath = os.path.join(clientPath, 'config/plugin')
    if os.path.exists(pluginPath):
        for f in os.listdir(pluginPath):
            if os.path.isfile(os.path.join(pluginPath, f)):
                continue
            names.append('plugin_' + f)

    return names


def do_copy(names, clientPath, outputPath):

    for name in names:
        if name == 'local':
            copy_local(clientPath, outputPath)
        elif name == 'tool' or name == 'tools':
            copy_tool(clientPath, outputPath)
        elif name == 'scripts' or name == 'script':
            copy_scripts(clientPath, outputPath)
        elif name.startswith('plugin_'):
            pluginName = name[7:]
            copy_sdk(clientPath, outputPath, pluginName, True)
        else:
            copy_sdk(clientPath, outputPath, name)


if __name__ == "__main__":

    clientPath = 'C:/2020-04-10-u8-code/PackageWebClient/client/android'
    outputPath = 'C:/2020-04-10-u8-code/PackageWebClient/sdk_output'


    selected = []
    while(True):
        sys.stdout.write(u"请选择导出项——local目录填local, scripts目录填scripts, 插件用plugin_开头加上插件目录名称,全部导出请输入*,多个用逗号分割：")
        sys.stdout.flush()

        target = input()

        if target == '*':
            selected = get_all_names(clientPath)
        else:
            for t in target.split(','):
                t = t.strip()
                selected.append(t)

        if len(selected) == 0:
            print(u"\n请重新输入！！\n")
        else:
            break

    do_copy(selected, clientPath, outputPath)

