# -*- coding: utf-8 -*-
# Author:tik
# CreateTime:2017-07-27
#
# helper functions for sdk_script.py  
#
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

androidNS = 'http://schemas.android.com/apk/res/android'


def jar2dex(srcDir, dstDir, dextool="dx.jar"):
    """
        compile jar files to dex.
    """

    dexToolPath = file_utils.getFullToolPath("/lib/dx.jar")
    heapSize = config_utils.get_jvm_heap_size()
    cmd = file_utils.getJavaCMD() + ' -jar -Xms%sm -Xmx%sm "%s" --dex --multi-dex --output="%s" ' % (
        heapSize, heapSize, dexToolPath, dstDir)

    num = 0

    for f in os.listdir(srcDir):
        if f.endswith(".jar"):
            num = num + 1
            cmd = cmd + " " + os.path.join(srcDir, f)

    libsPath = os.path.join(srcDir, "libs")
    if os.path.exists(libsPath):

        for f in os.listdir(libsPath):
            if (not f.startswith(".")) and f.endswith(".jar"):
                num = num + 1
                cmd = cmd + " " + os.path.join(srcDir, "libs", f)

    if num == 0:
        # no jar
        log_utils.warning("no need to do jar2dex. there is no jars in " + srcDir)
        return True

    return file_utils.exec_cmd(cmd)


def dex2smali(dexFile, targetdir, dextool="baksmali.jar"):
    """
        Transfer the dex to smali.
    """

    if not os.path.exists(dexFile):
        log_utils.error("the dexfile is not exists. path:%s", dexFile)
        return 1

    if not os.path.exists(targetdir):
        os.makedirs(targetdir)

    smaliTool = file_utils.getFullToolPath(dextool)

    dexPath = os.path.dirname(dexFile)
    dexName = os.path.basename(dexFile)
    (dexBaseName, ext) = os.path.splitext(dexName)

    dexIndex = 2
    dexFilePath = os.path.join(dexPath, dexName)
    while os.path.exists(dexFilePath):

        cmd = '"%s" -jar "%s" -o "%s" "%s"' % (file_utils.getJavaCMD(), smaliTool, targetdir, dexFilePath)
        ret = file_utils.execFormatCmd(cmd)
        if ret:
            return 1

        dexFilePath = os.path.join(dexPath, dexBaseName + str(dexIndex) + ext)
        dexIndex = dexIndex + 1

    # for k in range(1, 10):

    #     baseName = dexBaseName

    #     if k > 1:
    #         baseName = baseName + str(k)

    #     baseName = baseName + ext
    #     dexFilePath = os.path.join(dexPath, baseName)

    #     cmd = '"%s" -jar "%s" -o "%s" "%s"' % (file_utils.getJavaCMD(), smaliTool, targetdir, dexFilePath)
    #     ret = file_utils.execFormatCmd(cmd)
    #     if ret:
    #         return 1 

    # if os.path.exists(dexFilePath):

    #     if platform.system() == 'Darwin' or platform.system() == 'Linux':
    #         cmd = '"%s" -jar "%s" -o "%s" "%s"' % (file_utils.getJavaCMD(), smaliTool, targetdir, dexFilePath)
    #         ret = file_utils.execFormatCmd(cmd)
    #         if ret:
    #             return 1
    #     else:
    #         cmd = '"%s" -jar "%s" disassemble -o "%s" "%s"' % (file_utils.getJavaCMD(), smaliTool, targetdir, dexFilePath)
    #         ret = file_utils.execFormatCmd(cmd)
    #         if ret:
    #             return 1

    return 0


def dexes2smali(dexDir, targetdir, dextool="baksmali.jar"):
    """
        Transfer all dex in dexDir to smali
    """

    if not os.path.exists(dexDir):
        log_utils.error("the dexDir is not exists:" + dexDir)
        return 1

    files = file_utils.list_files(dexDir, [], [])
    for f in files:
        if not f.endswith(".dex"):
            continue

        ret = dex2smali(f, targetdir, dextool)

        if not ret:
            return False

    return True


def getSdkParamByKey(channel, key):
    if "params" in channel:
        params = channel['params']
        for p in params:
            if p['name'] == key:
                return p['value']

    return None


def changeSdkParamValByKey(channel, key, val):
    if "params" in channel:
        params = channel['params']
        for p in params:
            if p['name'] == key:
                p['value'] = val

    return None


def getSuperClassNameInSmali(decompileDir, smaliPath):
    f = open(smaliPath, 'r')
    lines = f.readlines()
    f.close()

    for line in lines:

        if line.strip().startswith('.super'):
            line = line[6:].strip()
            return line[1:-1].replace('/', '.')

    return None


def findSmaliPathOfClass(decompileDir, className):
    log_utils.debug("findSmaliPathOfClass:%s", className)

    className = className.replace(".", "/")

    for i in range(1, 10):
        smaliPath = "smali"
        if i > 1:
            smaliPath = smaliPath + str(i)

        path = decompileDir + "/" + smaliPath + "/" + className + ".smali"

        log_utils.debug(path)

        if os.path.exists(path):
            return path

    return None


def findApplicationClass(decompileDir):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return None

    applicationClassName = applicationNode.get(key)

    return applicationClassName


def findRootApplicationSmali(decompileDir):
    applicationClassName = findApplicationClass(decompileDir)

    if applicationClassName is None:
        log_utils.debug("findRootApplicationSmali: applicationClassName:%s", applicationClassName)
        return None

    return findRootApplicationRecursively(decompileDir, applicationClassName)


def findRootApplicationRecursively(decompileDir, applicationClassName):
    smaliPath = findSmaliPathOfClass(decompileDir, applicationClassName)

    if smaliPath is None or not os.path.exists(smaliPath):
        log_utils.debug("smaliPath not exists or get failed.%s", smaliPath)
        return None

    superClass = getSuperClassNameInSmali(decompileDir, smaliPath)
    if superClass is None:
        return None

    if superClass == 'android.app.Application':
        return smaliPath
    else:
        return findRootApplicationRecursively(decompileDir, superClass)


def modifyRootApplicationExtends(decompileDir, applicationClassName):
    applicationSmali = findRootApplicationSmali(decompileDir)
    if applicationSmali is None:
        log_utils.error("the applicationSmali get failed.")
        return

    log_utils.debug("modifyRootApplicationExtends: root application smali:%s", applicationSmali)

    modifyApplicationExtends(decompileDir, applicationSmali, applicationClassName)


# 将U8Application改为继承指定的applicationClassName
def modifyApplicationExtends(decompileDir, applicationSmaliPath, applicationClassName):
    log_utils.debug("modify Application extends %s; %s", applicationSmaliPath, applicationClassName)

    applicationClassName = applicationClassName.replace(".", "/")

    f = open(applicationSmaliPath, 'r')
    lines = f.readlines()
    f.close()

    result = ""
    for line in lines:

        if line.strip().startswith('.super'):
            result = result + '\n' + '.super L' + applicationClassName + ';\n'
        elif line.strip().startswith('invoke-direct') and 'android/app/Application;-><init>' in line:
            result = result + '\n' + '      invoke-direct {p0}, L' + applicationClassName + ';-><init>()V'
        elif line.strip().startswith('invoke-super'):
            if 'attachBaseContext' in line:
                result = result + '\n' + '      invoke-super {p0, p1}, L' + applicationClassName + ';->attachBaseContext(Landroid/content/Context;)V'
            elif 'onConfigurationChanged' in line:
                result = result + '\n' + '      invoke-super {p0, p1}, L' + applicationClassName + ';->onConfigurationChanged(Landroid/content/res/Configuration;)V'
            elif 'onCreate' in line:
                result = result + '\n' + '      invoke-super {p0}, L' + applicationClassName + ';->onCreate()V'
            elif 'onTerminate' in line:
                result = result + '\n' + '      invoke-super {p0}, L' + applicationClassName + ';->onTerminate()V'
            else:
                result = result + line

        else:
            result = result + line

    f = open(applicationSmaliPath, 'w')
    f.write(result)
    f.close()

    return 0


def removeManifestComponents(decompileDir, typeName, componentNameLst):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return

    activityNodeLst = applicationNode.findall(typeName)
    if activityNodeLst is None:
        return

    for activityNode in activityNodeLst:

        name = activityNode.get(key)
        if name in componentNameLst:
            applicationNode.remove(activityNode)
            log_utils.debug("remove " + name + " from AndroidManifest.xml")

    tree.write(manifestFile, 'UTF-8')
    return


# 删除AndroidManifest.xml中指定的组件，比如activity,service,provider等
# typeName:组件类型， 比如activity,service,provider,receiver
# name：组件名称， 比如com.u8.sdk.UniLoginActivity
def removeMinifestComponentByName(decompileDir, typeName, componentName):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return

    activityNodeLst = applicationNode.findall(typeName)
    if activityNodeLst is None:
        return

    for activityNode in activityNodeLst:

        name = activityNode.get(key)
        if name == componentName:
            applicationNode.remove(activityNode)
            break

    tree.write(manifestFile, 'UTF-8')

    log_utils.debug("remove " + componentName + " from AndroidManifest.xml")

    return componentName


def removePermissionByName(decompileDir, typeName, componentName):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    pList = root.findall(typeName)
    if pList is None:
        return None

    for pNode in pList:

        name = pNode.get(key)
        if name == componentName:
            root.remove(pNode)
            break

    tree.write(manifestFile, 'UTF-8')

    log_utils.debug("remove permission " + componentName + " from AndroidManifest.xml")

    return componentName


# 获取指定java文件中的package值
def getJavaPackage(javaFile):
    if not os.path.exists(javaFile):
        log_utils.error("getJavaPackage failed. java file is not exists." + javaFile)
        return ""

    f = open(javaFile, 'r')
    lines = f.readlines()
    f.close()

    for l in lines:
        l = l.strip()
        if l.startswith('package'):
            l = l.replace('package', '')
            l = l.replace(';', '')
            return l.strip()

    return ""


# 将指定java文件中的类的package值给修改为指定的值
def replaceJavaPackage(javaFile, newPackageName):
    if not os.path.exists(javaFile):
        log_utils.error("getJavaPackage failed. java file is not exists." + javaFile)
        return 1

    f = open(javaFile, 'r')
    lines = f.readlines()
    f.close()

    content = ""
    for l in lines:
        c = l.strip()
        if c.startswith('package'):
            content = content + 'package ' + newPackageName + ';\r\n'
        else:
            content = content + l

    f = open(javaFile, 'wb')
    f.write(content)
    f.close()

    return 0


# 修改或者替换meta-data  目前只是替换application节点下面的meta-data
def addOrUpdateMetaData(decompileDir, metaDataKey, metaDataVal):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'
    valKey = '{' + androidNS + '}value'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        log_utils.error("application node is not exists in AndroidManifest.xml")
        return ''

    bFound = False
    metadataNodes = applicationNode.findall('meta-data')

    if metadataNodes != None:

        for mnode in metadataNodes:
            keyName = mnode.get(key)
            if keyName == metaDataKey:
                bFound = True
                mnode.set(valKey, metaDataVal)
                break

    if not bFound:
        metaNode = SubElement(applicationNode, 'meta-data')
        metaNode.set(key, metaDataKey)
        metaNode.set(valKey, metaDataVal)

    tree.write(manifestFile, 'UTF-8')


# 删除游戏启动activity,可以设置需要过滤的activity（只能设置一个，一般是SDKManifest.xml中设置了一个渠道要求的启动Activity， 然后调用该方法的时候， 把该activity设置为过滤的activity）
def removeStartActivity(decompileDir, ignoreActivity):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'
    valKey = '{' + androidNS + '}value'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        log_utils.error("application node is not exists in AndroidManifest.xml")
        return ''

    activityNodeLst = applicationNode.findall('activity')
    if activityNodeLst is None:
        log_utils.error("activity node is not exists in AndroidManifest.xml")
        return ''

    activityName = ''

    for activityNode in activityNodeLst:

        activityName = activityNode.get(key)
        if ignoreActivity != None and activityName == ignoreActivity:
            continue

        bMain = False
        intentNodeLst = activityNode.findall('intent-filter')
        if intentNodeLst is None:
            break

        for intentNode in intentNodeLst:
            bFindAction = False
            bFindCategory = False

            actionNodeLst = intentNode.findall('action')
            if actionNodeLst is None:
                break
            for actionNode in actionNodeLst:
                if actionNode.attrib[key] == 'android.intent.action.MAIN':
                    bFindAction = True
                    break

            categoryNodeLst = intentNode.findall('category')
            if categoryNodeLst is None:
                break
            for categoryNode in categoryNodeLst:
                if categoryNode.attrib[key] == 'android.intent.category.LAUNCHER':
                    bFindCategory = True
                    break

            if bFindAction and bFindCategory:
                bMain = True
                intentNode.remove(actionNode)
                intentNode.remove(categoryNode)
                break

        if bMain:
            activityName = activityNode.attrib[key]
            break

    tree.write(manifestFile, 'UTF-8')

    return activityName


def getStartActivity(decompileDir):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'
    valKey = '{' + androidNS + '}value'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        log_utils.error("application node is not exists in AndroidManifest.xml")
        return ''

    activityNodeLst = applicationNode.findall('activity')
    if activityNodeLst is None:
        log_utils.error("activity node is not exists in AndroidManifest.xml")
        return ''

    activityName = ''

    for activityNode in activityNodeLst:

        activityName = activityNode.get(key)

        bMain = False
        intentNodeLst = activityNode.findall('intent-filter')
        if intentNodeLst is None:
            break

        for intentNode in intentNodeLst:
            bFindAction = False
            bFindCategory = False

            actionNodeLst = intentNode.findall('action')
            if actionNodeLst is None:
                break
            for actionNode in actionNodeLst:
                if actionNode.attrib[key] == 'android.intent.action.MAIN':
                    bFindAction = True
                    break

            categoryNodeLst = intentNode.findall('category')
            if categoryNodeLst is None:
                break
            for categoryNode in categoryNodeLst:
                if categoryNode.attrib[key] == 'android.intent.category.LAUNCHER':
                    bFindCategory = True
                    break

            if bFindAction and bFindCategory:
                bMain = True
                break

        if bMain:
            activityName = activityNode.attrib[key]
            return activityName

    return None


# 将指定的java文件编译成smali， 并合并到smali目录中
def compileJava2Smali(channel, decompileDir, packageName, className, dependencyLibs):
    sdkDir = decompileDir + '/../sdk/' + channel['sdk']
    if not os.path.exists(sdkDir):
        file_utils.printF("The sdk temp folder is not exists. path:" + sdkDir)
        return 1

    extraFilesPath = sdkDir + '/extraFiles'
    # relatedJar = os.path.join(extraFilesPath, 'vivoUnionSDK.jar')
    # relatedJar2 = os.path.join(extraFilesPath, 'libammsdk.jar')

    if not os.path.exists(extraFilesPath):
        log_utils.error("compileJava2Smali failed. please put java file and related jars in extraFiles folder")
        return 1

    javaFile = os.path.join(extraFilesPath, className + '.java')
    replaceJavaPackage(javaFile, packageName)

    splitdot = ';'
    if platform.system() == 'Darwin' or platform.system() == 'Linux':
        splitdot = ':'

    cmd = '"%sjavac" -source 1.7 -target 1.7 "%s" -classpath ' % (file_utils.getJavaBinDir(), javaFile)

    for lib in dependencyLibs:
        cmd = cmd + '"' + os.path.join(extraFilesPath, lib) + '"' + splitdot

    cmd = cmd + '"' + file_utils.getFullToolPath('android.jar') + '"'

    ret = file_utils.execFormatCmd(cmd)
    if ret:
        return 1

    packageDir = packageName.replace('.', '/')
    srcDir = sdkDir + '/tempDex'
    classDir = srcDir + '/' + packageDir

    if not os.path.exists(classDir):
        os.makedirs(classDir)

    classFiles = file_utils.list_files_with_ext(extraFilesPath, [], '.class')
    for cf in classFiles:
        targetClassFilePath = os.path.join(classDir, os.path.basename(cf))
        file_utils.copy_file(cf, targetClassFilePath)

    targetDexPath = os.path.join(sdkDir, className + '.dex')

    dxTool = file_utils.getFullToolPath("/lib/dx.jar")

    cmd = file_utils.getJavaCMD() + ' -jar -Xmx512m -Xms512m "%s" --dex --output="%s" "%s"' % (
        dxTool, targetDexPath, srcDir)

    ret = file_utils.execFormatCmd(cmd)

    if ret:
        return 1

    ret = dex2smali(targetDexPath, decompileDir + '/smali', "baksmali.jar")

    if ret:
        return 1

    return 0


# 替换AndroidManifest.xml中的application节点的android:name为指定的类
def replaceApplicationClass(decompileDir, applicationClass):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return 1

    applicationNode.set(key, applicationClass)

    tree.write(manifestFile, 'UTF-8')

    return 0


# 替换AndroidManifest.xml中android:installLocation属性
def setInstallLocation(decompileDir, locationVal):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    locationKey = '{' + androidNS + '}installLocation'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    root.attrib[locationKey] = locationVal

    tree.write(manifestFile, 'UTF-8')

    return 0


# 设置application节点上面的属性
def setPropOnApplicationNode(decompileDir, key, val):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return 1

    applicationNode.set(key, val)

    tree.write(manifestFile, 'UTF-8')


def handleAutoResDefined(decompileDir, packageName):
    resPath = decompileDir + "/res"

    files = []

    file_utils.list_files(resPath, files, [])

    for f in files:

        if (not f.endswith(".xml")):
            continue

        file_utils.modifyFileContent(f, 'http://schemas.android.com/apk/res-auto',
                                     'http://schemas.android.com/apk/lib/' + packageName)
