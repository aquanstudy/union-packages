# -*- coding: utf-8 -*-
# Author:xiaohei
# CreateTime:2018-11-19
#
# core build steps
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
import smali_utils
from res_merger import ResourceMerger
import manifest_merger
import manifest_utils
import apk_helper
import assets_merger
import progress_utils
import global_pre_script
import yml_utils
import aar_utils
import gradle_utils
import permission_utils
import temp_utils

androidNS = 'http://schemas.android.com/apk/res/android'


def delete_debug_res(decompileDir):
    """
        remove debug resources in base apk
    """

    filelist = ["colors", "dimens", "ids", "public", "strings", "styles"]

    for f in filelist:
        fpath = decompileDir + "/res/values/" + f + ".xml"
        if os.path.exists(fpath):
            tree = ET.parse(fpath)
            root = tree.getroot()
            for node in list(root):
                item = {}
                attribName = node.attrib.get('name')
                if attribName is None:
                    continue

                if attribName.lower().startswith("x_"):
                    root.remove(node)
                    log_utils.debug("remove debug res index name:" + attribName + " from" + fpath)

            tree.write(fpath, "UTF-8")

    resPath = decompileDir + "/res"

    filelist = file_utils.list_files(resPath, [], [])
    for f in filelist:
        if os.path.basename(f).lower().startswith("x_"):
            file_utils.del_file_folder(f)
            log_utils.debug("remove debug res file:" + f)

    devConfig = decompileDir + "/assets/u8_developer_config.properties"
    pluginConfig = decompileDir + "/assets/u8_plugin_config.xml"
    permissionConfig = decompileDir + "/assets/u8_permissions.xml"

    file_utils.del_file_folder(devConfig)
    file_utils.del_file_folder(pluginConfig)
    file_utils.del_file_folder(permissionConfig)

    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    appNode = root.find('application')
    if appNode != None:
        activityLst = appNode.findall('activity')
        key = '{' + androidNS + '}name'
        if activityLst != None and len(activityLst) > 0:
            for aNode in activityLst:
                activityName = aNode.attrib[key]
                if activityName == 'com.u8.sdk.impl.activities.LoginActivity':
                    appNode.remove(aNode)
                elif activityName == 'com.u8.sdk.impl.activities.PayActivity':

                    appNode.remove(aNode)

    tree.write(manifestFile, "UTF-8")


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


def jar2dex_with_d8(srcDir, dstDir, nodesugaring=True):
    """
        compile jar files to dex with d8.jar
    """

    libsPath = os.path.join(srcDir, "libs")
    dexToolPath = file_utils.getFullToolPath("/lib/d8.jar")
    androidPath = file_utils.getFullToolPath("android.jar")
    heapSize = config_utils.get_jvm_heap_size()
    cmd = file_utils.getJavaCMD() + ' -jar -Xms%sm -Xmx%sm "%s" --output "%s" --lib "%s" --classpath "%s" ' % (
        heapSize, heapSize, dexToolPath, dstDir, androidPath, libsPath)

    num = 0

    for f in os.listdir(srcDir):
        if f.endswith(".jar"):
            num = num + 1
            cmd = cmd + " " + os.path.join(srcDir, f)

    if os.path.exists(libsPath):

        for f in os.listdir(libsPath):
            if (not f.startswith(".")) and f.endswith(".jar"):
                num = num + 1
                cmd = cmd + " " + os.path.join(srcDir, "libs", f)

    if num == 0:
        # no jar
        log_utils.warning("no need to do jar2dex_with_d8. there is no jars in " + srcDir)
        return True

    return file_utils.exec_cmd(cmd)


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


def dex2smali(dexFile, targetdir, dextool="baksmali.jar"):
    """
        Transfer the dex to smali.
    """

    if not os.path.exists(dexFile):
        log_utils.error("the dexfile is not exists. path:%s", dexFile)
        return False

    if not os.path.exists(targetdir):
        os.makedirs(targetdir)

    smaliTool = file_utils.getFullToolPath(dextool)

    dexPath = os.path.dirname(dexFile)

    dexPath = file_utils.formatPath(dexPath)
    if "/assets" in dexPath:
        # assets目录下的dex不做处理
        log_utils.warning("dex2smali ignored a dex file in assets folder:" + dexPath)
        return True

    dexName = os.path.basename(dexFile)
    (dexBaseName, ext) = os.path.splitext(dexName)

    dexIndex = 2
    dexFilePath = os.path.join(dexPath, dexName)

    while os.path.exists(dexFilePath):

        cmd = '"%s" -jar "%s" -o "%s" "%s"' % (file_utils.getJavaCMD(), smaliTool, targetdir, dexFilePath)
        ret = file_utils.exec_cmd(cmd)
        if not ret:
            return False

        dexFilePath = os.path.join(dexPath, dexBaseName + str(dexIndex) + ext)
        dexIndex = dexIndex + 1

    # for k in range(1, 10):

    #     baseName = dexBaseName

    #     if k > 1:
    #         baseName = baseName + str(k)

    #     baseName = baseName + ext
    #     dexFilePath = os.path.join(dexPath, baseName)

    #     cmd = '"%s" -jar "%s" -o "%s" "%s"' % (file_utils.getJavaCMD(), smaliTool, targetdir, dexFilePath)
    #     ret = file_utils.exec_cmd(cmd)
    #     if not ret:
    #         return False 

    # if os.path.exists(dexFilePath):

    #     if platform.system() == 'Darwin' or platform.system() == 'Linux':

    #         cmd = '"%s" -jar "%s" -o "%s" "%s"' % (file_utils.getJavaCMD(), smaliTool, targetdir, dexFilePath)
    #         ret = file_utils.exec_cmd(cmd)
    #         if not ret:
    #             return False

    #     else:

    #         cmd = '"%s" -jar "%s" disassemble -o "%s" "%s"' % (file_utils.getJavaCMD(), smaliTool, targetdir, dexFilePath)
    #         ret = file_utils.exec_cmd(cmd)
    #         if not ret:
    #             return False

    return True


def copy_root_res_to_apk(apkfile, decompileDir):
    aapt = file_utils.getFullToolPath("aapt")

    igoreFiles = ['AndroidManifest.xml', 'apktool.yml', 'smali', 'res', 'original', 'lib', 'build', 'assets', 'unknown']

    smaliIndex = 2
    otherSmaliFolder = "smali_classes" + str(smaliIndex)
    while os.path.exists(os.path.join(decompileDir, otherSmaliFolder)):
        igoreFiles.append("smali_classes" + str(smaliIndex))
        smaliIndex = smaliIndex + 1
        otherSmaliFolder = "smali_classes" + str(smaliIndex)

    igoreFileFullPaths = []

    for ifile in igoreFiles:
        fullpath = os.path.join(decompileDir, ifile)
        igoreFileFullPaths.append(fullpath)

    addFiles = []

    addFiles = file_utils.list_files(decompileDir, addFiles, igoreFileFullPaths)

    if len(addFiles) <= 0:
        return

    addCmd = '"%s" add "%s"'
    for f in addFiles:
        fname = f[(len(decompileDir) + 1):]
        addCmd = addCmd + ' ' + fname

    addCmd = addCmd % (aapt, apkfile)

    currPath = os.getcwd()

    os.chdir(decompileDir)
    file_utils.exec_cmd(addCmd)
    os.chdir(currPath)


def copy_assets_in_jars(sdkDir):
    libDir = os.path.join(sdkDir, 'libs')
    assetsDir = os.path.join(sdkDir, 'assets')
    if not os.path.exists(assetsDir):
        os.makedirs(assetsDir)

    targetTempDir = os.path.join(os.path.dirname(sdkDir), 'temp_libs')
    if not os.path.exists(targetTempDir):
        os.makedirs(targetTempDir)

    for f in os.listdir(libDir):
        fpath = os.path.join(libDir, f)
        if f.endswith(".jar") and os.path.isfile(fpath):
            libTempPath = os.path.join(targetTempDir, f + "_temp_dir")
            file_utils.unzip_file(fpath, libTempPath)

            assetsPath = os.path.join(libTempPath, 'assets')
            if not os.path.exists(assetsPath):
                continue

            assets_merger.merge(assetsPath, assetsDir)


def handle_sdk(game, channel, sdkDir, decompileDir, dependencyList):
    if len(dependencyList) > 0:
        # handle dependency
        log_utils.debug("begin handle gradle dependencies of sdk %s. path:%s", channel['sdk'], sdkDir)
        log_utils.debug("dependency list:")
        for l in dependencyList:
            log_utils.debug(str(l))

        ret = gradle_utils.handle_dependencies(sdkDir, dependencyList)

        if not ret:
            log_utils.error("handle sdk %s dependencies failed. sdkDir:%s", channel['sdk'], sdkDir)
            return False

    manifestName = manifest_utils.get_sdk_manfiest_name(game, sdkDir)

    ret = aar_utils.handle_sdk_aars(channel, sdkDir, manifestName)

    if not ret:
        log_utils.error("handle sdk %s aar files failed.", channel['sdk'])
        return False

    manifestFrom = os.path.join(sdkDir, manifestName)
    manifestTo = os.path.join(decompileDir, 'AndroidManifest.xml')

    log_utils.info("The sdk manifest file is %s", manifestFrom)

    # parse proxy application
    manifest_utils.parse_proxy_application(channel, manifestFrom)

    # copy assets in jars
    copy_assets_in_jars(sdkDir)

    # merge jars into smali
    # ret = jar2dex(sdkDir, sdkDir)
    # 这里使用d8，导致魅族单机sdk生成的dex文件，在转换为smali之后，生成的annotation注解有重复的，导致apktool rebuild的时候出错
    ret = jar2dex_with_d8(sdkDir, sdkDir)
    if not ret:
        return False

    smaliDir = os.path.join(decompileDir, 'smali')
    ret = dexes2smali(sdkDir, smaliDir)
    if not ret:
        return False

        # merge manifest
    ret = manifest_merger.merge2(manifestFrom, manifestTo)

    if ret:
        log_utils.info("merge manifest file success.")
    else:
        log_utils.info("merge manifest file failed.")
        return False

        # copy so
    copyFrom = os.path.join(sdkDir, 'libs')
    copyTo = os.path.join(decompileDir, 'lib')
    file_utils.copy_files(copyFrom, copyTo, ['.jar', '.aar'])

    # copy assets
    copyFrom = os.path.join(sdkDir, 'assets')
    copyTo = os.path.join(decompileDir, 'assets')
    assets_merger.merge(copyFrom, copyTo)

    # copy root
    copyFrom = os.path.join(sdkDir, 'root')
    file_utils.copy_files(copyFrom, decompileDir)

    return True


def copy_channel_resources(game, channel, decompileDir):
    """
        Copy channel resources to decompile folder. for example icon resources, assets and so on.
    """

    resPath = "games/game" + str(game['appID']) + "/channels/" + str(channel['id'])
    resPath = file_utils.getFileFullPath(resPath)
    if not os.path.exists(resPath):
        log_utils.warning("the channel %s special res path is not exists. %s", channel['id'], resPath)
        return

    assetsPath = os.path.join(resPath, 'assets')
    libsPath = os.path.join(resPath, 'libs')
    resourcePath = os.path.join(resPath, 'res')

    targetAssetsPath = os.path.join(decompileDir, 'assets')
    targetLibsPath = os.path.join(decompileDir, 'lib')
    targetResourcePath = os.path.join(decompileDir, 'res')

    file_utils.copy_files(assetsPath, targetAssetsPath)
    file_utils.copy_files(libsPath, targetLibsPath)
    file_utils.copy_files(resourcePath, targetResourcePath)


def copy_game_resources(game, decompileDir):
    """
        Copy game res files to apk.
    """

    resPath = "games/game" + str(game['appID']) + "/res"
    resPath = file_utils.getFileFullPath(resPath)
    if not os.path.exists(resPath):
        log_utils.warning("the game %s has no extra res folder", game['name'])
        return

    assetsPath = os.path.join(resPath, 'assets')
    libsPath = os.path.join(resPath, 'libs')
    resourcePath = os.path.join(resPath, 'res')

    targetAssetsPath = os.path.join(decompileDir, 'assets')
    targetLibsPath = os.path.join(decompileDir, 'lib')
    targetResourcePath = os.path.join(decompileDir, 'res')

    file_utils.copy_files(assetsPath, targetAssetsPath)
    file_utils.copy_files(libsPath, targetLibsPath)
    file_utils.copy_files(resourcePath, targetResourcePath)


def copy_game_root_resources(game, decompileDir):
    """
        Copy game root files to apk. the files will be in the root path of apk
    """

    resPath = "games/game" + str(game['appID']) + "/root"
    resPath = file_utils.getFileFullPath(resPath)

    if not os.path.exists(resPath):
        log_utils.info("the game %s has no root folder", game['name'])
        return

    targetResPath = decompileDir
    file_utils.copy_files(resPath, targetResPath)


def add_splash_screen(workDir, game, channel, decompileDir):
    """
        if the splash attrib is not zero ,then set the splash activity
        if the splash_copy_to_unity attrib is set, then copy the splash img to unity res fold ,replace the default splash.png.

    """

    if 'splash' not in channel or channel['splash'] == None or len(channel['splash']) <= 0 or channel['splash'] == '0':
        log_utils.debug("no splash configed in channel. just ignore");
        return

    isLandscape = False
    if game["orientation"] == 'landscape':
        isLandscape = True

    resTargetPath = decompileDir + "/res"

    if int(channel['splash']) == 1:
        # default splash path
        log_utils.debug("now to append splash with sdk default splash image")
        relPath = "21"
        if isLandscape:
            relPath = "11"

        resPath = workDir + "/sdk/" + channel['sdk'] + "/splash/" + relPath + "/drawable"
        file_utils.copy_files(resPath, os.path.join(resTargetPath, "drawable"))

    elif int(channel['splash']) == 2:
        # custom splash path
        log_utils.debug("now to append splash with custom splash image")
        resPath = "games/game" + str(channel["gameID"]) + "/channels/" + str(channel["id"]) + "/custom_splash"
        resPath = file_utils.getFileFullPath(resPath)
        targetPath = resTargetPath + "/drawable-xxhdpi"  # 默认上传到xxhdpi下面，大小1920*1080
        file_utils.copy_files(resPath, targetPath)

    splashPath = file_utils.getSplashPath()
    smaliPath = splashPath + "/smali"
    smaliTargetPath = decompileDir + "/smali"

    file_utils.copy_files(smaliPath, smaliTargetPath)

    splashLayoutPath = splashPath + "/u8_splash.xml"
    splashTargetPath = decompileDir + "/res/layout/u8_splash.xml"
    file_utils.copy_file(splashLayoutPath, splashTargetPath)

    # remove original launcher activity of the game
    activityName = manifest_utils.remove_start_activity(os.path.join(decompileDir, 'AndroidManifest.xml'))

    # append the launcher activity with the splash activity
    manifest_utils.append_splash_activity(os.path.join(decompileDir, 'AndroidManifest.xml'), isLandscape)

    # if SplashActivity.smali may exists in smali, smali_classes2, smali_classes3...
    retryNum = 1
    while retryNum > 0:

        curPath = smaliTargetPath

        if retryNum > 1:
            curPath = smaliTargetPath + "_classes" + str(retryNum)

        if not os.path.exists(curPath):
            retryNum = -1
        else:
            retryNum = retryNum + 1
            splashActivityPath = curPath + "/com/u8/sdk/SplashActivity.smali"
            file_utils.modifyFileContent(splashActivityPath, '{U8SDK_Game_Activity}', activityName)

    log_utils.info("modify splash file success.")


def handle_channel_sdk(game, channel, workDir, decompileDir):
    sdkSourceDir = file_utils.getFullPath('config/sdk/' + channel['sdk'])
    sdkDestDir = workDir + "/sdk/" + channel['sdk']
    file_utils.copy_files(sdkSourceDir, sdkDestDir)

    if len(channel["dependencyList"]) > 0:
        # copy gradle temp project
        gradleProjDir = file_utils.getFullPath("config/local/U8_Temp_AS_Project")
        targetProjDir = workDir + "/sdk/U8_Temp_AS_Project"
        file_utils.copy_files(gradleProjDir, targetProjDir)

    return handle_sdk(game, channel, sdkDestDir, decompileDir, channel['dependencyList'])


def handle_manifest_placeholders(game, channel, decompileDir, packageName):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    file_utils.modifyFileContent(manifestFile, '${applicationId}', packageName)
    file_utils.modifyFileContent(manifestFile, '${u8ApplicationId}', packageName)


def handle_third_plugins(workDir, decompileDir, game, channel, packageName):
    """
        handle third plugins like talkingdata, bugly,etc.
    """

    pluginsFolder = file_utils.getFullPath('config/plugin')
    gamePluginFolder = file_utils.getFileFullPath('games/game' + str(game['appID']) + '/plugin')
    plugins = channel.get('third-plugins')

    if plugins == None or len(plugins) <= 0:
        log_utils.info("the channel %s has no supported plugins.", channel['name'])
        return True

    # copy all resources to temp folder.
    for plugin in plugins:
        pluginName = plugin['name']
        pluginSourceFolder = os.path.join(pluginsFolder, pluginName)
        if not os.path.exists(pluginSourceFolder):
            log_utils.warning("the plugin %s config folder is not exists", pluginName)
            continue

        pluginTargetFolder = workDir + "/plugins/" + pluginName + "/plugin"
        file_utils.copy_files(pluginSourceFolder, pluginTargetFolder)

        gamePluginSourceFolder = os.path.join(gamePluginFolder, pluginName)
        if os.path.exists(gamePluginSourceFolder):
            # log_utils.warning("the plugin %s is not configed in the game %s", pluginName, game['name'])
            file_utils.copy_files(gamePluginSourceFolder, pluginTargetFolder)

        if len(plugin["dependencyList"]) > 0:
            # copy gradle temp project
            gradleProjDir = file_utils.getFullPath("config/local/U8_Temp_AS_Project")
            targetProjDir = workDir + "/plugins/" + pluginName + "/U8_Temp_AS_Project"
            file_utils.copy_files(gradleProjDir, targetProjDir)

            # handle plugins
    pluginNum = 0
    for plugin in plugins:

        pluginName = plugin['name']
        pluginFolder = workDir + "/plugins/" + pluginName + "/plugin"

        if not os.path.exists(pluginFolder):
            log_utils.warning("the plugin %s temp folder is not exists", pluginName)
            continue

        ret = handle_sdk(game, channel, pluginFolder, decompileDir, plugin['dependencyList'])

        if not ret:
            log_utils.error("plugin %s handle failed.", pluginName)
            return False

        pluginNum += 1

    log_utils.info("Total plugin num:%s;success handle num:%s", str(len(plugins)), str(pluginNum))

    return True


def merge_resources(workDir, decompileDir, channel):
    """ 
        merge resources of channel sdk, plugin sdks and base apk
    """

    baseResPath = os.path.join(decompileDir, 'res')
    channelResPath = os.path.join(workDir, 'sdk/' + channel['sdk'] + '/res')

    pluginPaths = list()
    pluginFolders = os.path.join(workDir, 'plugins')

    if os.path.exists(pluginFolders):
        for f in os.listdir(pluginFolders):
            pluginPaths.append(os.path.join(pluginFolders, f + "/plugin/res"))

    ResourceMerger.merge(baseResPath, channelResPath, pluginPaths)


def remove_duplicate_drawable_res(path1, path2):
    if not os.path.exists(path1) or not os.path.exists(path2):
        return

    duplicatedFiles = []

    for f1 in os.listdir(path1):
        for f2 in os.listdir(path2):
            if f1 == f2:
                log_utils.warning("%s duplicated", os.path.join(path2, f2))
                duplicatedFiles.append(os.path.join(path2, f2))
                break

    for d in duplicatedFiles:
        file_utils.del_file_folder(d)


def check_drawable_v4(decompileDir):
    resPath = decompileDir + '/res'
    if not os.path.exists(resPath):
        return

    # check drawable resource
    ldpiPath = decompileDir + '/res/drawable-ldpi'
    mdpiPath = decompileDir + '/res/drawable-mdpi'
    hdpiPath = decompileDir + '/res/drawable-hdpi'
    xhdpiPath = decompileDir + '/res/drawable-xhdpi'
    xxhdpiPath = decompileDir + '/res/drawable-xxhdpi'
    xxxhdpiPath = decompileDir + '/res/drawable-xxxhdpi'

    remove_duplicate_drawable_res(ldpiPath, ldpiPath + "-v4")
    remove_duplicate_drawable_res(mdpiPath, mdpiPath + "-v4")
    remove_duplicate_drawable_res(hdpiPath, hdpiPath + "-v4")
    remove_duplicate_drawable_res(xhdpiPath, xhdpiPath + "-v4")
    remove_duplicate_drawable_res(xxhdpiPath, xxhdpiPath + "-v4")
    remove_duplicate_drawable_res(xxxhdpiPath, xxxhdpiPath + "-v4")


def copy_extra_R(decompileDir, channel, newPackageName):
    """
        copy the new generated R.java to sdk extra package

        first:add a new param in sdk channel config <param name="extraR" value="the package need to generate R. em. com.facebook" />

        for those sdk which used R.*.* directly in code.


    """

    newPackageNames = []

    if "extraR" in channel:
        newPackageNames.extend(channel['extraR'].split(","))

    if "extraRList" in channel:
        newPackageNames.extend(channel["extraRList"])

    plugins = channel.get('third-plugins')
    if (plugins != None and len(plugins) > 0):
        for plugin in plugins:
            pluginParams = plugin['params']
            if pluginParams != None and len(pluginParams) > 0:
                for pp in pluginParams:
                    pname = pp['name']
                    if pname == 'extraR':
                        prnames = pp['value'].split(",")
                        newPackageNames.extend(prnames)
                        break

            if "extraRList" in plugin:
                newPackageNames.extend(plugin["extraRList"])

    if newPackageNames == None or len(newPackageNames) <= 0:
        log_utils.debug("the sdk %s has no extraR config.", channel['sdk'])
        return True

    newPackageNames = list(set(newPackageNames))  # 去重

    sdkPath = os.path.dirname(decompileDir) + "/sdk"
    resPath = os.path.join(sdkPath, channel['sdk'] + "/res")

    tempPath = os.path.dirname(decompileDir)
    tempPath = tempPath + "/temp"
    genPath = os.path.join(tempPath, "gen")

    rPath = newPackageName.replace('.', '/')
    rPath = os.path.join(genPath, rPath)
    rPath = os.path.join(rPath, "R.java")

    if not os.path.exists(rPath):
        log_utils.error("copy extra R failed. the R.java is not exists:%s", rPath)
        return False

    for k in range(len(newPackageNames)):

        packageName = newPackageNames[k]

        tempPath = os.path.join(sdkPath, 'extraTemp' + str(k))
        log_utils.debug("generate sdk R: the temp path is %s", tempPath)

        if not os.path.exists(tempPath):
            os.makedirs(tempPath)

        targetResPath = os.path.join(tempPath, "res")
        file_utils.copy_files(resPath, targetResPath)

        genPath = os.path.join(tempPath, "gen")
        if not os.path.exists(genPath):
            os.makedirs(genPath)

        trPath = packageName.replace('.', '/')

        smaliTRPath = os.path.join(decompileDir, 'smali', trPath, 'R.smali')
        if os.path.exists(smaliTRPath):
            log_utils.warning("ignored R.java copy. the R.smali already exists in :" + smaliTRPath)
            continue

        trPath = os.path.join(genPath, trPath)
        if not os.path.exists(trPath):
            os.makedirs(trPath)

        targetRPath = os.path.join(trPath, "R.java")

        file_utils.copy_file(rPath, targetRPath)

        file_utils.modifyFileContent(targetRPath, newPackageName, packageName, False)

        cmd = '"%sjavac" -source 1.7 -target 1.7 -encoding UTF-8 "%s"' % (file_utils.getJavaBinDir(), targetRPath)
        ret = file_utils.exec_cmd(cmd)
        if not ret:
            return False

        dexToolPath = file_utils.getFullToolPath("/lib/dx.jar")

        heapSize = config_utils.get_jvm_heap_size()
        targetDexPath = os.path.join(tempPath, "classes.dex")
        cmd = file_utils.getJavaCMD() + ' -jar -Xmx%sm -Xms%sm "%s" --dex --output="%s" "%s"' % (
            heapSize, heapSize, dexToolPath, targetDexPath, genPath)

        ret = file_utils.exec_cmd(cmd)
        if not ret:
            return False

        smaliPath = os.path.join(decompileDir, "smali")
        ret = dex2smali(targetDexPath, smaliPath)
        if not ret:
            return False

    return True


def generate_R(decompileDir, resPath, manifestPath, genPath, targetDexPath, newPackageName, useAppt2):
    """
        generate R.java for the newPackageName
    """

    if not os.path.exists(resPath):
        log_utils.debug("%s not exists ", resPath)
        return False

    androidPath = file_utils.getFullToolPath("android.jar")

    if useAppt2:

        ret = apk_helper.generate_rindex_v2(os.path.dirname(decompileDir), genPath, resPath, manifestPath)

        if not ret:
            log_utils.debug("resource package failed with aapt2. now try to package with aapt")
            return apk_helper.generate_rindex_v1(genPath, resPath, manifestPath)

    else:

        ret = apk_helper.generate_rindex_v1(genPath, resPath, manifestPath)

        if not ret:
            log_utils.debug("resource package failed with aapt. now try to package with aapt2")
            return apk_helper.generate_rindex_v2(os.path.dirname(decompileDir), genPath, resPath, manifestPath)

    rPath = newPackageName.replace('.', '/')
    rPath = os.path.join(genPath, rPath)
    rPath = os.path.join(rPath, "R.java")

    cmd = '"%sjavac" -source 1.7 -target 1.7 -encoding UTF-8 "%s"' % (file_utils.getJavaBinDir(), rPath)
    ret = file_utils.exec_cmd(cmd)
    if not ret:
        return False

    dexToolPath = file_utils.getFullToolPath("/lib/dx.jar")

    heapSize = config_utils.get_jvm_heap_size()
    cmd = file_utils.getJavaCMD() + ' -jar -Xmx%sm -Xms%sm "%s" --dex --output="%s" "%s"' % (
        heapSize, heapSize, dexToolPath, targetDexPath, genPath)

    ret = file_utils.exec_cmd(cmd)
    if not ret:
        return False

    smaliPath = os.path.join(decompileDir, "smali")
    ret = dex2smali(targetDexPath, smaliPath, "baksmali.jar")

    return ret


def generate_new_R(game, newPackageName, decompileDir, channel):
    """
        Use all new resources to generate the new R.java, and compile it ,then copy it to the target smali dir
    """

    check_drawable_v4(decompileDir)

    tempPath = os.path.dirname(decompileDir)
    tempPath = tempPath + "/temp"
    log_utils.debug("generate R:the temp path is %s", tempPath)
    if os.path.exists(tempPath):
        file_utils.del_file_folder(tempPath)
    if not os.path.exists(tempPath):
        os.makedirs(tempPath)

    resPath = os.path.join(decompileDir, "res")
    targetResPath = os.path.join(tempPath, "res")
    file_utils.copy_files(resPath, targetResPath)

    genPath = os.path.join(tempPath, "gen")
    if not os.path.exists(genPath):
        os.makedirs(genPath)

    manifestPath = os.path.join(decompileDir, "AndroidManifest.xml")
    targetDexPath = os.path.join(tempPath, "classes.dex")

    useAppt2 = False
    if "aapt2" in game and game["aapt2"] == "true":
        useAppt2 = True

    ret = generate_R(decompileDir, targetResPath, manifestPath, genPath, targetDexPath, newPackageName, useAppt2)
    if not ret:
        return False

    return copy_extra_R(decompileDir, channel, newPackageName)


def write_develop_info(game, channel, decompileDir):
    developConfigFile = os.path.join(decompileDir, "assets")
    if not os.path.exists(developConfigFile):
        os.makedirs(developConfigFile)

    developConfigFile = os.path.join(developConfigFile, "u8_developer_config.properties")
    config_utils.write_developer_properties(game, channel, developConfigFile)


def write_plugin_info(channel, decompileDir):
    developConfigFile = os.path.join(decompileDir, "assets")
    if not os.path.exists(developConfigFile):
        os.makedirs(developConfigFile)

    developConfigFile = os.path.join(developConfigFile, "u8_plugin_config.xml")
    config_utils.write_plugin_configs(channel, developConfigFile)


def write_meta_info(channel, decompileDir):
    """
        write all channel config with [bWriteInManifest] flag into AndroidManifest.xml
    """

    manifestFile = os.path.join(decompileDir, 'AndroidManifest.xml')
    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    key = '{' + androidNS + '}name'
    val = '{' + androidNS + '}value'

    appNode = root.find('application')
    if appNode is None:
        return

    metaDataList = appNode.findall('meta-data')

    if metaDataList != None:
        for metaDataNode in metaDataList:
            keyName = metaDataNode.attrib[key]
            for child in channel['params']:
                if keyName == child['name'] and child['bWriteInManifest'] == '1':
                    log_utils.warning("the meta-data node %s repeated. remove it .", keyName)
                    appNode.remove(metaDataNode)

            if 'third-plugins' in channel and channel['third-plugins'] != None and len(channel['third-plugins']) > 0:

                for cPlugin in channel['third-plugins']:
                    if 'params' in cPlugin and cPlugin['params'] != None and len(cPlugin['params']) > 0:
                        for child in cPlugin['params']:
                            if keyName == child['name'] and child['bWriteInManifest'] == '1':
                                log_utils.warning("the meta-data node %s repeated. remove it .", keyName)
                                appNode.remove(metaDataNode)

    existKeys = dict()
    for child in channel['params']:
        if child['bWriteInManifest'] != None and child['bWriteInManifest'] == '1':

            keyName = child['name']
            keyVal = child['value']
            if keyName in existKeys:
                log_utils.warning("the meta-data node %s repeated. exists value:%s; new value:%s", keyName,
                                  existKeys[keyName], keyVal)
                continue

            metaNode = SubElement(appNode, 'meta-data')
            metaNode.set(key, keyName)
            metaNode.set(val, keyVal)
            existKeys[keyName] = keyVal

    if 'third-plugins' in channel and channel['third-plugins'] != None and len(channel['third-plugins']) > 0:

        for cPlugin in channel['third-plugins']:
            if 'params' in cPlugin and cPlugin['params'] != None and len(cPlugin['params']) > 0:
                for child in cPlugin['params']:
                    if child['bWriteInManifest'] != None and child['bWriteInManifest'] == '1':

                        keyName = child['name']
                        keyVal = child['value']

                        if keyName in existKeys:
                            log_utils.warning("the meta-data node %s repeated. exists value:%s; new value:%s", keyName,
                                              existKeys[keyName], keyVal)
                            continue

                        metaNode = SubElement(appNode, 'meta-data')
                        metaNode.set(key, child['name'])
                        metaNode.set(val, child['value'])
                        existKeys[keyName] = keyVal

    if 'U8_APPLICATION_PROXY_NAME' in channel:
        metaNode = SubElement(appNode, 'meta-data')
        metaNode.set(key, "U8_APPLICATION_PROXY_NAME")
        metaNode.set(val, channel['U8_APPLICATION_PROXY_NAME'])

    tree.write(manifestFile, 'UTF-8')
    log_utils.info("The manifestFile meta-data write successfully")


def write_log_info(game, manifestFile):
    """
        write log config into AndroidManifest.xml
    """

    if 'log' not in game:
        log_utils.warning("the log config is not in games.xml of game: %s", game['name'])
        return

    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    key = '{' + androidNS + '}name'
    val = '{' + androidNS + '}value'
    appNode = root.find('application')

    metaDataList = appNode.findall('meta-data')

    if metaDataList and len(metaDataList) > 0:

        for metaDataNode in metaDataList:
            keyName = metaDataNode.attrib[key]
            for lKey in game['log']:
                if keyName == lKey:
                    log_utils.warning("the meta-data node %s repeated. remove it .", keyName)
                    appNode.remove(metaDataNode)

    for lKey in game['log']:
        metaNode = SubElement(appNode, 'meta-data')
        metaNode.set(key, lKey)
        metaNode.set(val, game['log'][lKey])

    tree.write(manifestFile, 'UTF-8')


def do_plugin_script(channel, pluginInfo, decompileDir, packageName, sdkTempDir):
    sdkScript = os.path.join(sdkTempDir, 'script.py')

    if not os.path.exists(sdkScript):
        log_utils.debug("script.py not exists in plugin :" + sdkTempDir)
        return True

    sys.path.append(sdkTempDir)

    import script

    try:
        ret = script.execute(channel, pluginInfo, decompileDir, packageName)
    except NameError, e:
        log_utils.error(e.message)
    else:
        print("plugin script in scope")

    del sys.modules['script']
    sys.path.remove(sdkTempDir)

    if ret == 0:
        return True

    return False


def do_plugin_scripts(workDir, decompileDir, channel, packageName):
    plugins = channel.get('third-plugins')

    if plugins == None or len(plugins) <= 0:
        log_utils.debug("there is no plugins in channel")
        return True

    for plugin in plugins:
        pluginName = plugin['name']

        pluginTargetFolder = workDir + "/plugins/" + pluginName + "/plugin"

        if not os.path.exists(pluginTargetFolder):
            log_utils.debug("plugin folder not exists:" + pluginTargetFolder)
            continue

        ret = do_plugin_script(channel, plugin, decompileDir, packageName, pluginTargetFolder)

        if not ret:
            return False

    return True


def do_channel_script(channel, workDir, decompileDir, packageName, game):
    sdkTempDir = os.path.join(workDir, 'sdk/' + channel['sdk'])

    sdkScript = os.path.join(sdkTempDir, "sdk_script.py")

    if not os.path.exists(sdkScript):
        return True

    sys.path.append(sdkTempDir)

    import sdk_script

    try:
        ret = sdk_script.execute(channel, decompileDir, packageName)
    except NameError, e:
        log_utils.error(e.message)
    else:
        print("channel script in scope")

    del sys.modules['sdk_script']
    sys.path.remove(sdkTempDir)

    if ret == 0:
        return True

    return False


def do_game_pre_script(game, channel, decompileDir, packageName):
    scriptDir = file_utils.getFileFullPath("games/game" + str(game['appID']) + "/scripts")

    if not os.path.exists(scriptDir):
        log_utils.info(
            "the game pre script is not exists. if you have some specail logic, you can do it in games/[yourgame]/scripts/post_script.py")
        return

    sdkScript = os.path.join(scriptDir, "pre_script.py")

    if not os.path.exists(sdkScript):
        log_utils.info(
            "the game pre script is not exists. if you have some specail logic, you can do it in games/[yourgame]/scripts/pre_script.py")
        return

    sys.path.append(scriptDir)

    import pre_script

    log_utils.info("now to execute pre_script.py of game %s ", game['name'])

    try:
        pre_script.execute(game, channel, decompileDir, packageName)
    except NameError, e:
        log_utils.error(e.message)
    else:
        print("game pre script in scope")

    del sys.modules['pre_script']
    sys.path.remove(scriptDir)

    return ret


def do_game_post_script(game, channel, decompileDir, packageName):
    scriptDir = file_utils.getFileFullPath("games/game" + str(game['appID']) + "/scripts")

    if not os.path.exists(scriptDir):
        log_utils.info(
            "the game post script is not exists. if you have some specail logic, you can do it in games/[yourgame]/scripts/post_script.py")
        return True

    sdkScript = os.path.join(scriptDir, "post_script.py")

    if not os.path.exists(sdkScript):
        log_utils.info(
            "the game post script is not exists. if you have some specail logic, you can do it in games/[yourgame]/scripts/post_script.py")
        return True

    sys.path.append(scriptDir)

    import post_script

    log_utils.info("now to execute post_script.py of game %s ", game['name'])
    try:
        ret = post_script.execute(game, channel, decompileDir, packageName)
    except NameError, e:
        log_utils.error(e.message)
    else:
        print("game post script in scope")

    del sys.modules['post_script']
    sys.path.remove(scriptDir)

    if ret == 0:
        return True

    return False


def append_channel_icon_mark(game, channel, decompileDir):
    """
        append icon mark on game icon.
    """

    iconType = 1

    if 'iconType' in channel:
        iconType = channel['iconType']

    if iconType == 1:
        # auto add mark into icon

        gameIconPath = 'games/game' + str(game['appID']) + '/icon/icon.png'
        gameIconPath = file_utils.getFileFullPath(gameIconPath)
        if not os.path.exists(gameIconPath):
            log_utils.error("the game %s icon is not exists:%s", game['name'], gameIconPath)
            return

        if 'icon' not in channel:
            log_utils.warning("the channel %s of game %s do not config icon in config.xml,no icon mark.",
                              channel['name'], game['name'])
            return

        rlImg = Image.open(gameIconPath)
        markType = channel['icon']
        markName = 'none'
        if markType == 'rb':
            markName = 'right-bottom'
        elif markType == 'rt':
            markName = 'right-top'
        elif markType == 'lt':
            markName = 'left-top'
        elif markType == 'lb':
            markName = 'left-bottom'

        markPath = 'config/sdk/' + channel['sdk'] + '/icon_marks/' + markName + '.png'
        markPath = file_utils.getFullPath(markPath)

        log_utils.debug("icon mark path:" + markPath)

        if not os.path.exists(markPath):
            log_utils.warning("the icon mark %s is not exists of sdk %s.no icon mark.", markPath, channel['name'])
        else:
            markIcon = Image.open(markPath)
            rlImg = image_utils.append_icon_mark(rlImg, markIcon, (0, 0))
            log_utils.debug("generate icon with corner successfully")


    elif iconType == 2:
        # use custom icon
        customIconPath = 'games/game' + str(channel['gameID']) + '/channels/' + str(
            channel['id']) + '/custom_icon/icon.png'
        customIconPath = file_utils.getFileFullPath(customIconPath)
        if not os.path.exists(customIconPath):
            log_utils.error("custom icon path not exists:" + customIconPath)
            return
        rlImg = Image.open(customIconPath)

    # ldpiSize = (36, 36)
    # mdpiSize = (48, 48)
    # hdpiSize = (72, 72)
    # xhdpiSize = (96, 96)
    # xxhdpiSize = (144,144)
    # xxxhdpiSize = (192, 192)

    ldpiSize = (36, 36)
    mdpiSize = (48, 48)
    hdpiSize = (192, 192)
    xhdpiSize = (256, 256)
    xxhdpiSize = (384, 384)
    xxxhdpiSize = (512, 512)

    xxxhdpiIcon = rlImg.resize(xxxhdpiSize, Image.ANTIALIAS)
    xxhdpiIcon = rlImg.resize(xxhdpiSize, Image.ANTIALIAS)
    xhdpiIcon = rlImg.resize(xhdpiSize, Image.ANTIALIAS)
    hdpiIcon = rlImg.resize(hdpiSize, Image.ANTIALIAS)
    mdpiIcon = rlImg.resize(mdpiSize, Image.ANTIALIAS)
    ldpiIcon = rlImg.resize(ldpiSize, Image.ANTIALIAS)

    ldpiPath = decompileDir + '/res/drawable-ldpi'
    mdpiPath = decompileDir + '/res/drawable-mdpi'
    hdpiPath = decompileDir + '/res/drawable-hdpi'
    xhdpiPath = decompileDir + '/res/drawable-xhdpi'
    xxhdpiPath = decompileDir + '/res/drawable-xxhdpi'
    xxxhdpiPath = decompileDir + '/res/drawable-xxxhdpi'

    if not os.path.exists(ldpiPath):
        os.makedirs(ldpiPath)

    if not os.path.exists(mdpiPath):
        os.makedirs(mdpiPath)

    if not os.path.exists(hdpiPath):
        os.makedirs(hdpiPath)

    if not os.path.exists(xhdpiPath):
        os.makedirs(xhdpiPath)

    if not os.path.exists(xxhdpiPath):
        os.makedirs(xxhdpiPath)

    if not os.path.exists(xxxhdpiPath):
        os.makedirs(xxxhdpiPath)

    gameIconName = 'u8_app_icon.png'

    ldpiIcon.save(os.path.join(ldpiPath, gameIconName), 'PNG')
    if os.path.exists(ldpiPath + "-v4"):
        ldpiIcon.save(os.path.join(ldpiPath + "-v4", gameIconName), 'PNG')
    mdpiIcon.save(os.path.join(mdpiPath, gameIconName), 'PNG')
    if os.path.exists(mdpiPath + "-v4"):
        mdpiIcon.save(os.path.join(mdpiPath + "-v4", gameIconName), 'PNG')
    hdpiIcon.save(os.path.join(hdpiPath, gameIconName), 'PNG')
    if os.path.exists(hdpiPath + "-v4"):
        hdpiIcon.save(os.path.join(hdpiPath + "-v4", gameIconName), 'PNG')
    xhdpiIcon.save(os.path.join(xhdpiPath, gameIconName), 'PNG')
    if os.path.exists(xhdpiPath + "-v4"):
        xhdpiIcon.save(os.path.join(xhdpiPath + "-v4", gameIconName), 'PNG')

    xxhdpiIcon.save(os.path.join(xxhdpiPath, gameIconName), 'PNG')
    if os.path.exists(xxhdpiPath + "-v4"):
        xxhdpiIcon.save(os.path.join(xxhdpiPath + "-v4", gameIconName), 'PNG')

    xxxhdpiIcon.save(os.path.join(xxxhdpiPath, gameIconName), 'PNG')
    if os.path.exists(xxxhdpiPath + "-v4"):
        xxxhdpiIcon.save(os.path.join(xxxhdpiPath + "-v4", gameIconName), 'PNG')

    manifestFile = decompileDir + "/AndroidManifest.xml"

    manifest_utils.set_game_icon(manifestFile, "@drawable/u8_app_icon")


def check_cpu_support(game, decompileDir):
    libsPath = os.path.join(decompileDir, 'lib')

    if not os.path.exists(libsPath):
        return

    isfilter = ("cpuSupport" in game) and len(game["cpuSupport"]) > 0

    filters = None
    if isfilter:
        filters = game["cpuSupport"].split('|')

        for f in os.listdir(libsPath):
            if f not in filters:
                file_utils.del_file_folder(os.path.join(decompileDir, 'lib/' + f))

    # make sure so in armeabi and armeabi-v7a is same
    armeabiPath = os.path.join(decompileDir, 'lib/armeabi')
    armeabiv7aPath = os.path.join(decompileDir, 'lib/armeabi-v7a')

    if os.path.exists(armeabiPath) and os.path.exists(armeabiv7aPath):

        for f in os.listdir(armeabiPath):
            fv7 = os.path.join(armeabiv7aPath, f)
            if not os.path.exists(fv7):
                shutil.copy2(os.path.join(armeabiPath, f), fv7)

        for fv7 in os.listdir(armeabiv7aPath):
            f = os.path.join(armeabiPath, fv7)
            if not os.path.exists(f):
                shutil.copy2(os.path.join(armeabiv7aPath, fv7), f)


def modify_game_name(game, channel, decompileDir):
    """
        modify app name
    """

    gameName = None
    if 'gameName' in channel and len(channel['gameName']) > 0:
        gameName = channel['gameName']
    elif 'gameName' in game and len(game['gameName']) > 0:
        gameName = game['gameName']

    if gameName == None or len(gameName) <= 0:
        log_utils.debug("no need to modify game name.")
        return

    manifestFile = decompileDir + "/AndroidManifest.xml"

    manifest_utils.modify_app_name(manifestFile, gameName)


def add_local_jars(workDir, decompileDir):
    u8sdkJarPath = file_utils.getFullPath('config/local')
    if not os.path.exists(u8sdkJarPath):
        log_utils.error("the config/local is not exists. no jars to copy.")
        return True

    targetPath = workDir + "/local"
    if not os.path.exists(targetPath):
        os.makedirs(targetPath)

    file_utils.copy_files(u8sdkJarPath, targetPath, None, ['android-support-multidex.jar', 'U8_Temp_AS_Project'])

    ret = jar2dex(targetPath, targetPath)

    if not ret:
        return False

    smaliPath = decompileDir + "/smali"
    classesPath = targetPath + "/classes.dex"

    if not os.path.exists(classesPath):
        # there is no jars need handle in local path
        log_utils.debug("there is no jars need handle in local path")
        return True

    return dex2smali(targetPath + '/classes.dex', smaliPath)


def check_multidex_jar(workDir, decompileDir):
    smaliPath = decompileDir + "/smali"

    multidexFilePath = smaliPath + "/android/support/multidex/MultiDex.smali"

    if os.path.exists(multidexFilePath):
        return True

    # android-support-multidex.jar not exists. copy
    dexJar = file_utils.getFullPath('config/local/android-support-multidex.jar')
    if not os.path.exists(dexJar):
        log_utils.error(
            "the method num expired of dex, but no android-support-multidex.jar in u8.apk or in local folder")
        return False

    targetPath = workDir + "/local"
    if not os.path.exists(targetPath):
        os.makedirs(targetPath)

    file_utils.copy_file(dexJar, targetPath + "/android-support-multidex.jar")

    ret = jar2dex(targetPath, targetPath)

    if not ret:
        return False

    smaliPath = decompileDir + "/smali"
    return dex2smali(targetPath + '/classes.dex', smaliPath)


def copy_permission_res(decompileDir):
    permissionDir = file_utils.getFullPath("config/local/permission")
    if not os.path.exists(permissionDir):
        log_utils.warning("config/local/permission folder not exists. copy permission resource failed.")
        return

    file_utils.copy_files(permissionDir, decompileDir)
    log_utils.debug("copy permission resource success")


def check_base_multidex(decompileDir):
    smaliPath = decompileDir + "/smali"

    smaliIndex = 2
    otherSmaliPath = decompileDir + "/smali_classes" + str(smaliIndex)
    while os.path.exists(otherSmaliPath):
        file_utils.copy_files(otherSmaliPath, smaliPath, None, None, False)  # 如果dex2, dex3等有重复的类， 不再覆盖dex中的类
        file_utils.del_file_folder(otherSmaliPath)
        log_utils.debug(
            "base apk had multiple dex folders. copied smali_classes" + str(smaliIndex) + " into smali folder.")
        smaliIndex = smaliIndex + 1
        otherSmaliPath = decompileDir + "/smali_classes" + str(smaliIndex)


def split_dex(workDir, decompileDir):
    """
        split dex file with 65535
    """

    log_utils.debug("now to check split dex ... ")

    smaliPath = decompileDir + "/smali"

    smaliIndex = 2
    otherSmaliPath = decompileDir + "/smali_classes" + str(smaliIndex)
    while os.path.exists(otherSmaliPath):
        file_utils.copy_files(otherSmaliPath, smaliPath)
        file_utils.del_file_folder(otherSmaliPath)
        smaliIndex = smaliIndex + 1
        otherSmaliPath = decompileDir + "/smali_classes" + str(smaliIndex)

    allFiles = []
    allFiles = file_utils.list_files(decompileDir, allFiles, [])

    maxFuncNum = 65000  # 65535 #未知特殊情况下计算结果比实际值偏小，暂时不要设置为65535，设置小一些
    currFucNum = 0
    totalFucNum = 0

    currDexIndex = 1

    allRefs = dict()

    # 保证U8Application等类在第一个classex.dex文件中
    for f in allFiles:
        f = f.replace("\\", "/")
        if "/com/u8/sdk" in f or "/android/support/multidex" in f:
            currFucNum = currFucNum + smali_utils.get_smali_method_count(f, allRefs)

    totalFucNum = currFucNum
    for f in allFiles:

        f = f.replace("\\", "/")
        if not f.endswith(".smali"):
            continue

        if "/com/u8/sdk" in f or "/android/support/multidex" in f:
            continue

        thisFucNum = smali_utils.get_smali_method_count(f, allRefs)
        totalFucNum = totalFucNum + thisFucNum
        if currFucNum + thisFucNum >= maxFuncNum:
            currFucNum = thisFucNum
            currDexIndex = currDexIndex + 1
            newDexPath = os.path.join(decompileDir, "smali_classes" + str(currDexIndex))
            if os.path.exists(newDexPath):
                file_utils.del_file_folder(newDexPath)

            os.makedirs(newDexPath)

        else:
            currFucNum = currFucNum + thisFucNum

        if currDexIndex > 1:
            targetPath = f[0:len(decompileDir)] + "/smali_classes" + str(currDexIndex) + f[len(smaliPath):]
            file_utils.copy_file(f, targetPath)
            file_utils.del_file_folder(f)

    log_utils.info("the total func num:" + str(totalFucNum))
    log_utils.info("split dex success. the classes.dex num:" + str(currDexIndex))


def write_version_info(game, channel, packageName, decompileDir):
    versionCode = None
    versionName = None
    if 'versionCode' in game and game['versionCode'] != None and game['versionCode'] > 0:
        versionCode = str(game['versionCode'])

    if 'versionName' in game and game['versionName'] != None and len(str(game['versionName'])) > 0:
        versionName = game['versionName']

    minSdkVersion = None
    targetSdkVersion = None
    maxSdkVersion = None

    if 'minSdkVersion' in channel and channel['minSdkVersion'] != None and channel['minSdkVersion'] > 0:
        minSdkVersion = str(channel['minSdkVersion'])

    if 'targetSdkVersion' in channel and channel['targetSdkVersion'] != None and channel['targetSdkVersion'] > 0:
        targetSdkVersion = str(channel['targetSdkVersion'])

    if 'maxSdkVersion' in channel and channel['maxSdkVersion'] != None and channel['maxSdkVersion'] > 0:
        maxSdkVersion = str(channel['maxSdkVersion'])

    if minSdkVersion == None and 'minSdkVersion' in game and game['minSdkVersion'] != None and game[
        'minSdkVersion'] > 0:
        minSdkVersion = str(game['minSdkVersion'])

    if targetSdkVersion == None and 'targetSdkVersion' in game and game['targetSdkVersion'] != None and game[
        'targetSdkVersion'] > 0:
        targetSdkVersion = str(game['targetSdkVersion'])

    if maxSdkVersion == None and 'maxSdkVersion' in game and game['maxSdkVersion'] != None and game[
        'maxSdkVersion'] > 0:
        maxSdkVersion = str(game['maxSdkVersion'])

    yml_utils.modify_version_info(os.path.join(decompileDir, 'apktool.yml'), packageName, versionCode, versionName,
                                  minSdkVersion, targetSdkVersion, maxSdkVersion)


def modify_compress_items(game, decompileDir):
    if 'doNotCompress' not in game or game['doNotCompress'] == None or len(game['doNotCompress']) == 0:
        log_utils.debug("doNotCompress config is not exists. just ignore")
        return

    uncompressRegx = game['doNotCompress'].split(',')

    validLst = list()
    for regx in uncompressRegx:

        regx = regx.strip()
        if len(regx) == 0:
            continue

        if regx.startswith('-'):
            regx = (regx[1:]).strip()

        validLst.append(regx)

    yml_utils.modify_doNotCompress(os.path.join(decompileDir, 'apktool.yml'), validLst)


def build(game, channel, destApkName, keystore):
    apkSourcePath = None
    if "apkPath" in game:
        apkSourcePath = game['apkPath']

    if apkSourcePath is None:
        log_utils.error("base apk is not specified in game " + str(game['appID']))
        return 1

    baseApkPath = file_utils.getFileFullPath(apkSourcePath)
    log_utils.info("the base apk file is : %s", baseApkPath)

    if not os.path.exists(baseApkPath):
        log_utils.error(u'the base apk file is not exists, must named with u8.apk')
        return 1

    isPublic = True
    sdkName = channel['sdk']
    workDir = 'workspace/' + 'game' + str(game["appID"]) + '/' + channel["name"] + str(channel["id"]);
    workDir = file_utils.getFileFullPath(workDir)

    tempApkSource = workDir + "/temp.apk"
    decompileDir = workDir + "/decompile"

    file_utils.del_file_folder(workDir)

    progress_utils.setProgressTempPath(decompileDir)
    progress_utils.setProgress(0)

    file_utils.copy_file(baseApkPath, tempApkSource)

    progress_utils.setProgress(5)

    # 反编译apk
    ret = apk_helper.decompile(tempApkSource, decompileDir)

    progress_utils.setProgress(20)
    if not ret:
        return 1

        # 合并母包multiple dex
    check_base_multidex(decompileDir)

    # 执行全局的预定义逻辑
    global_pre_script.execute(decompileDir, game, channel)

    # 删除测试资源
    delete_debug_res(decompileDir)

    # 添加local目录下的jar包
    ret = add_local_jars(workDir, decompileDir)

    if not ret:
        return False

    # 检查multidex那个jar包
    ret = check_multidex_jar(workDir, decompileDir)

    if not ret:
        return False

    # 拷贝权限资源
    copy_permission_res(decompileDir)

    # 重命名包名
    newPackageName = manifest_utils.rename_package_name(channel, os.path.join(decompileDir, 'AndroidManifest.xml'),
                                                        channel['bundleID'])

    temp_utils.setCurrPackageName(newPackageName)

    progress_utils.setProgress(25)

    # 执行游戏级别预执行脚本.
    do_game_pre_script(game, channel, decompileDir, newPackageName)

    progress_utils.setProgress(30)

    # 处理渠道SDK
    ret = handle_channel_sdk(game, channel, workDir, decompileDir)

    if not ret:
        return False

    # 处理集成的第三方插件
    ret = handle_third_plugins(workDir, decompileDir, game, channel, newPackageName)

    if not ret:
        return False

    progress_utils.setProgress(36)

    # 处理${applicationId}等占位符
    handle_manifest_placeholders(game, channel, decompileDir, newPackageName)

    # 处理角标
    append_channel_icon_mark(game, channel, decompileDir)

    progress_utils.setProgress(40)

    # 拷贝资源
    copy_channel_resources(game, channel, decompileDir)
    copy_game_resources(game, decompileDir)
    copy_game_root_resources(game, decompileDir)
    progress_utils.setProgress(45)

    # 添加闪屏
    add_splash_screen(workDir, game, channel, decompileDir)

    # so文件检查
    check_cpu_support(game, decompileDir)

    progress_utils.setProgress(46)

    # 修改游戏名称
    modify_game_name(game, channel, decompileDir)

    progress_utils.setProgress(47)

    # merge resources
    merge_resources(workDir, decompileDir, channel)

    progress_utils.setProgress(55)

    # 执行插件自定义脚本
    do_plugin_scripts(workDir, decompileDir, channel, newPackageName)

    # 执行渠道SDK自定义脚本

    do_channel_script(channel, workDir, decompileDir, newPackageName, game)

    progress_utils.setProgress(60)

    # 执行游戏自定义脚本
    do_game_post_script(game, channel, decompileDir, newPackageName)

    progress_utils.setProgress(62)

    # 写入各种配置信息
    write_version_info(game, channel, newPackageName, decompileDir)
    write_develop_info(game, channel, decompileDir)
    write_plugin_info(channel, decompileDir)
    write_meta_info(channel, decompileDir)
    write_log_info(game, decompileDir)

    # 自动权限处理
    permission_utils.handle_auto_permission(game, channel, decompileDir)

    progress_utils.setProgress(65)

    # 修改doNotCompress 压缩配置项
    modify_compress_items(game, decompileDir)

    # 根据最新的资源生成 R.java
    ret = generate_new_R(game, newPackageName, decompileDir, channel)
    if not ret:
        return False

    progress_utils.setProgress(75)

    # 拆分dex
    split_dex(workDir, decompileDir)

    progress_utils.setProgress(85)

    targetApk = workDir + "/output.apk"

    # 回编译
    ret = apk_helper.recompile(decompileDir, targetApk)
    if not ret:
        return False

    progress_utils.setProgress(90)

    # 拷贝root下面的资源
    copy_root_res_to_apk(targetApk, decompileDir)
    progress_utils.setProgress(91)
    channelNameStr = channel["name"].replace(' ', '')

    destApkPath = file_utils.getFullOutputPath('game' + str(game["appID"]), channel["name"] + str(channel["id"]))
    destApkPath = os.path.join(destApkPath, destApkName)

    # 签名apk
    keystoreFilePath = file_utils.getFileFullPath(keystore['filePath'])

    if "signVersion" in channel and channel["signVersion"] == "V2":
        # v2签名之前进行zipalign.
        alignApkPath = workDir + "/align.apk"
        ret = apk_helper.align_apk(targetApk, alignApkPath)
        if not ret:
            return False

        progress_utils.setProgress(95)

        if 'signApk' not in channel or channel['signApk'] != 0:
            ret = apk_helper.sign_apk_v2(alignApkPath, destApkPath, keystoreFilePath, keystore['password'],
                                         keystore['aliasName'], keystore['aliasPwd'])
            if not ret:
                return False
        else:
            log_utils.debug("the apk is set to unsigned.")

    else:

        if 'signApk' not in channel or channel['signApk'] != 0:
            ret = apk_helper.sign_apk_v1(targetApk, keystoreFilePath, keystore['password'], keystore['aliasName'],
                                         keystore['aliasPwd'], "SHA1withRSA")
            if not ret:
                return False
            progress_utils.setProgress(95)
        else:
            log_utils.debug("the apk is set to unsigned.")

        ret = apk_helper.align_apk(targetApk, destApkPath)
        if not ret:
            return False

    progress_utils.setProgress(100)

    log_utils.info("channel %s package success.", channel['name'])

    return True
