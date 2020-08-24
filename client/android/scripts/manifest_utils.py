# -*- coding: utf-8 -*-
# Author:xiaohei
# CreateTime:2018-11-16
#
# All operations for AndroidManifest.xml or SDKManifest.xml
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

androidNS = 'http://schemas.android.com/apk/res/android'


def get_sdk_manfiest_name(game, sdkDir):
    """
        get SDKManifest.xml name with orientation
    """

    name = 'SDKManifest.xml'

    if 'orientation' in game:
        if game['orientation'] == 'portrait':
            name = name[:-4] + "_portrait.xml"
        else:
            name = name[:-4] + "_landscape.xml"

        if not os.path.exists(os.path.join(sdkDir, name)):
            name = 'SDKManifest.xml'
    return name


def parse_proxy_application(channel, sdkManifest):
    """
        parse proxy application in SDKManifest.xml
    """

    if not os.path.exists(sdkManifest):
        log_utils.error("the manifest file is not exists.sdkManifest:%s", sdkManifest)
        return False

    ET.register_namespace('android', androidNS)
    sdkTree = ET.parse(sdkManifest)
    sdkRoot = sdkTree.getroot()

    appConfigNode = sdkRoot.find('applicationConfig')

    if appConfigNode != None:

        proxyApplicationName = appConfigNode.get('proxyApplication')
        if proxyApplicationName != None and len(proxyApplicationName) > 0:

            if 'U8_APPLICATION_PROXY_NAME' in channel:
                channel['U8_APPLICATION_PROXY_NAME'] = channel['U8_APPLICATION_PROXY_NAME'] + ',' + proxyApplicationName
            else:
                channel['U8_APPLICATION_PROXY_NAME'] = proxyApplicationName
    return True


def get_package_name(manifestFile):
    """
        Get The package attrib of application node in AndroidManifest.xml
    """

    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()
    package = root.attrib.get('package')

    return package


def rename_package_name(channel, manifestFile, newPackageName):
    """
        Rename package name to the new name configed in the channel
    """

    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()
    package = root.attrib.get('package')

    oldPackageName = package
    tempPackageName = newPackageName

    addExtraR = True

    if tempPackageName != None and len(tempPackageName) > 0:

        if tempPackageName[0:1] == '.':
            newPackageName = oldPackageName + tempPackageName
        else:
            newPackageName = tempPackageName

    if newPackageName == None or len(newPackageName) <= 0:
        addExtraR = False
        newPackageName = oldPackageName

    log_utils.info("the new package name is %s", newPackageName)

    # now to check activity or service
    appNode = root.find('application')
    if appNode != None:

        activityLst = appNode.findall('activity')
        key = '{' + androidNS + '}name'
        if activityLst != None and len(activityLst) > 0:
            for aNode in activityLst:
                activityName = aNode.attrib[key]

                if activityName.startswith(".wxapi.WXEntryActivity") or activityName.startswith(
                        ".wxapi.WXEntryPayActivity"):
                    continue

                if activityName[0:1] == '.':
                    activityName = oldPackageName + activityName
                elif activityName.find('.') == -1:
                    activityName = oldPackageName + '.' + activityName
                aNode.attrib[key] = activityName

        serviceLst = appNode.findall('service')
        if serviceLst != None and len(serviceLst) > 0:
            for sNode in serviceLst:
                serviceName = sNode.attrib[key]
                if serviceName[0:1] == '.':
                    serviceName = oldPackageName + serviceName
                elif serviceName.find('.') == -1:
                    serviceName = oldPackageName + '.' + serviceName
                sNode.attrib[key] = serviceName

        receiverLst = appNode.findall('receiver')
        if receiverLst != None and len(receiverLst) > 0:
            for sNode in receiverLst:
                receiverName = sNode.attrib[key]
                if receiverName[0:1] == '.':
                    receiverName = oldPackageName + receiverName
                elif receiverName.find('.') == -1:
                    receiverName = oldPackageName + '.' + receiverName
                sNode.attrib[key] = receiverName

        providerLst = appNode.findall('provider')
        if providerLst != None and len(providerLst) > 0:
            for sNode in providerLst:
                providerName = sNode.attrib[key]
                if providerName[0:1] == '.':
                    providerName = oldPackageName + providerName
                elif providerName.find('.') == -1:
                    providerName = oldPackageName + '.' + providerName
                sNode.attrib[key] = providerName

    root.attrib['package'] = newPackageName
    tree.write(manifestFile, 'UTF-8')

    # generate R...
    if addExtraR:

        if "extraRList" not in channel:
            channel["extraRList"] = []

        channel['extraRList'].append(oldPackageName)

    return newPackageName


def remove_start_activity(manifestFile, ignoredActivity=None):
    """
        remove android.intent.action.MAIN and android.intent.category.LAUNCHER flag from start activity
    """
    activityName = remove_start_component_internal(manifestFile, "activity", ignoredActivity)

    if activityName == None or len(activityName) == 0:
        activityName = remove_start_component_internal(manifestFile, "activity-alias", ignoredActivity)

    log_utils.debug("remove_start_activity success. activityName:" + activityName)

    return activityName


def remove_start_component_internal(manifestFile, componentName, ignoredActivity=None):
    """
        remove android.intent.action.MAIN and android.intent.category.LAUNCHER flag from start activity
    """

    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'

    returnKey = key
    if componentName == 'activity-alias':
        returnKey = '{' + androidNS + '}targetActivity'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return None

    activityNodeLst = applicationNode.findall(componentName)
    if activityNodeLst is None:
        return None

    activityName = ''

    for activityNode in activityNodeLst:

        name = activityNode.attrib[key]
        if ignoredActivity != None and name == ignoredActivity:
            continue

        bMain = False
        intentNodeLst = activityNode.findall('intent-filter')
        if intentNodeLst is None:
            continue

        for intentNode in intentNodeLst:
            bFindAction = False
            bFindCategory = False

            actionNodeLst = intentNode.findall('action')
            if actionNodeLst is None:
                continue
            for actionNode in actionNodeLst:
                if actionNode.attrib[key] == 'android.intent.action.MAIN':
                    bFindAction = True
                    break

            categoryNodeLst = intentNode.findall('category')
            if categoryNodeLst is None:
                continue
            for categoryNode in categoryNodeLst:
                if categoryNode.attrib[key] == 'android.intent.category.LAUNCHER':
                    bFindCategory = True
                    break

            if bFindAction and bFindCategory:
                bMain = True
                intentNode.remove(actionNode)
                intentNode.remove(categoryNode)

                if len(list(intentNode)) == 0:
                    activityNode.remove(intentNode)

                break

        if bMain:
            activityName = activityNode.attrib[returnKey]
            break

    tree.write(manifestFile, 'UTF-8')

    return activityName


def change_prop_on_component(manifestFile, componentType, componentName, propName, propValue):
    """
        change the prop of the component. forexample. android:launchMode
    """

    log_utils.debug(
        "begin to change prop on component:" + componentName + ";propName:" + propName + ";propValue:" + propValue)
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return None

    activityNodeLst = applicationNode.findall(componentType)
    if activityNodeLst is None:
        return None

    activityName = ''

    for activityNode in activityNodeLst:

        name = activityNode.attrib[key]
        if name == componentName:
            activityNode.set(propName, propValue)
            break

    tree.write(manifestFile, 'UTF-8')


def append_splash_activity(manifestFile, isLandscape):
    """
        add uni slash activity into AndroidManifest.xml
    """

    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'
    screenkey = '{' + androidNS + '}screenOrientation'
    theme = '{' + androidNS + '}theme'
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return

    splashNode = SubElement(applicationNode, 'activity')
    splashNode.set(key, 'com.u8.sdk.SplashActivity')
    splashNode.set(theme, '@android:style/Theme.Black.NoTitleBar.Fullscreen')

    if isLandscape:
        splashNode.set(screenkey, 'landscape')
    else:
        splashNode.set(screenkey, 'portrait')

    intentNode = SubElement(splashNode, 'intent-filter')
    actionNode = SubElement(intentNode, 'action')
    actionNode.set(key, 'android.intent.action.MAIN')
    categoryNode = SubElement(intentNode, 'category')
    categoryNode.set(key, 'android.intent.category.LAUNCHER')
    tree.write(manifestFile, 'UTF-8')


def delete_icon_in_activity(manifestFile):
    """
        delete android:icon in all activities
    """

    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}icon'
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')

    activityNodes = applicationNode.findall("activity")

    for anode in activityNodes:
        anode.attrib.pop(key, None)

    tree.write(manifestFile, 'UTF-8')


def set_game_icon(manifestFile, iconName):
    delete_icon_in_activity(manifestFile)

    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    iconKey = '{' + androidNS + '}icon'
    roundIconKey = '{' + androidNS + '}roundIcon'
    applicationNode = root.find('application')
    applicationNode.set(iconKey, iconName)
    applicationNode.attrib.pop(roundIconKey, None)
    tree.write(manifestFile, 'UTF-8')


def delete_label_in_activity(manifestFile):
    """
        delete android:icon in all activities
    """

    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}label'
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')

    activityNodes = applicationNode.findall("activity")

    for anode in activityNodes:
        anode.attrib.pop(key, None)

    tree.write(manifestFile, 'UTF-8')


def get_icon_name(manifestFile):
    """
        get android:icon from AndroidManifest.xml
    """

    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return "ic_launcher"

    key = '{' + androidNS + '}icon'
    iconName = applicationNode.get(key)

    if iconName is None:
        return "ic_launcher"

    name = iconName[10:]

    return name


def modify_app_name(manifestFile, gameName):
    """
        modify app name 
    """

    delete_label_in_activity(manifestFile)

    file_utils.modifyFileContent(manifestFile, '@string/app_name', gameName)

    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    labelKey = '{' + androidNS + '}label'
    applicationNode = root.find('application')
    applicationNode.set(labelKey, gameName)

    tree.write(manifestFile, 'UTF-8')


def get_meta_data_in_component(manifestFile, componentType, componentName, metaKey):
    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')

    if applicationNode == None:
        return None

    nodes = applicationNode.findall(componentType)

    if nodes == None or len(nodes) == 0:
        return None

    for node in nodes:

        name = node.get('{' + androidNS + '}name')

        if name != componentName:
            continue

        metaNodes = node.findall('meta-data')

        if metaNodes == None or len(metaNodes) == 0:
            return None

        for mnode in metaNodes:
            mname = mnode.get('{' + androidNS + '}name')
            if mname != metaKey:
                continue

            return mnode.get('{' + androidNS + '}value')

    return None


def get_paths_for_provider(filePath):
    if not os.path.exists(filePath):
        return list()

    ET.register_namespace('android', androidNS)
    tree = ET.parse(filePath)
    root = tree.getroot()

    pathNode = root
    if pathNode.tag != 'paths':
        nodes = list(pathNode)
        pathNode = nodes[0]

    if pathNode.tag != 'paths':
        log_utils.error("file provider file not valid ? :" + filePath)
        return list()

    nodes = list(pathNode)

    result = list()

    for node in nodes:
        item = dict()
        item['tag'] = node.tag
        item['name'] = node.get('name')
        item['path'] = node.get('path')

        result.append(item)

    return result


def merge_paths_for_provider(sourceFilePath, targetFilePath):
    """
        merge two file-provider res files.
    """

    sourcePaths = get_paths_for_provider(sourceFilePath)
    targetPaths = get_paths_for_provider(targetFilePath)

    sameLst = list()

    for source in sourcePaths:
        for target in targetPaths:
            if source['tag'] == target['tag'] and source['name'] == target['name']:

                if source['path'] != target['path']:
                    # name same but path not same. merge failed.
                    return False
                else:
                    # same
                    sameLst.append(source['tag'] + "_" + source['name'])

    ET.register_namespace('android', androidNS)
    tree = ET.parse(targetFilePath)
    root = tree.getroot()

    pathNode = root
    if pathNode.tag != 'paths':
        nodes = list(pathNode)
        pathNode = nodes[0]

    for info in sourcePaths:

        if (info['tag'] + "_" + info['name']) in sameLst:
            continue

        pathNode = SubElement(pathNode, info['tag'])
        pathNode.set('name', info['name'])
        pathNode.set('path', info['path'])

    tree.write(targetFilePath, 'UTF-8')

    return True
