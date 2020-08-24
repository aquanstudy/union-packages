# -*- coding: utf-8 -*-
# Author:xiaohei
# CreateTime:2019-06-18
#
# auto handle permission.
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
import manifest_utils

androidNS = 'http://schemas.android.com/apk/res/android'

dangerous_groups = {

    "CALENDAR": {
        "name": u"日历",
        "permissions": [
            "READ_CALENDAR",
            "WRITE_CALENDAR"
        ]
    },
    "CAMERA": {
        "name": u"相机",
        "permissions": [
            "CAMERA"
        ]
    },
    "CONTACTS": {
        "name": u"联系人",
        "permissions": [
            "GET_ACCOUNTS",
            "READ_CONTACTS",
            "WRITE_CONTACTS"

        ]
    },
    "LOCATION": {
        "name": u"位置",
        "permissions": [
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION"
        ]
    },
    "MICROPHONE": {
        "name": u"麦克风",
        "permissions": [
            "RECORD_AUDIO"
        ]
    },
    "PHONE": {
        "name": u"电话",
        "permissions": [
            "READ_PHONE_STATE",
            "CALL_PHONE",
            "READ_CALL_LOG",
            "WRITE_CALL_LOG",
            "ADD_VOICEMAIL",
            "USE_SIP",
            "PROCESS_OUTGOING_CALLS"
        ]
    },
    "SMS": {
        "name": u"短信",
        "permissions": [
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_SMS",
            "RECEIVE_WAP_PUSH",
            "RECEIVE_MMS"
        ]
    },
    "STORAGE": {
        "name": u"存储",
        "permissions": [
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE"
        ]
    },
    "SENSORS": {
        "name": u"体感",
        "permissions": [
            "BODY_SENSORS"
        ]
    }
}


def try_get_dangerous_permission_info(permission):
    """
        try get the dangerous permission info of the specified permission
        if return None, the permission is not dangerous.
    """

    pname = permission[19:]  # android.permission.

    for gkey in dangerous_groups:

        group = dangerous_groups[gkey]

        permissions = group['permissions']

        dangerous = False
        for p in permissions:

            if p != pname:
                continue
            dangerous = True

        if not dangerous:
            continue

        name = group['name']

        return {
            "group": gkey,
            "name": group['name'],
            "permission": permission
        }

    return None


def get_all_use_permissions(manifestFile):
    if not os.path.exists(manifestFile):
        log_utils.debug("the manifest file not exists:" + manifestFile)
        return None

    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    key = '{' + androidNS + '}name'
    nodes = root.findall('uses-permission')

    if nodes == None or len(nodes) == 0:
        log_utils.warning("there is no uses-permission in " + manifestFile)
        return None

    permissions = []
    for node in nodes:

        name = node.get(key)

        if name not in permissions:
            permissions.append(name)
        else:
            log_utils.warning("ignore permission. node " + name + " duplicated in " + manifestFile)

    return permissions


def is_need_write_settings(manifestFile):
    # allPermissions = get_all_use_permissions(manifestFile)

    # if not allPermissions:
    #     return False

    # for p in allPermissions:

    #     if p == 'android.permission.WRITE_SETTINGS':
    #         return True

    # return False

    # 根据最新的隐私政策，修改设置这个权限，不再进行自动申请
    return False


def get_dangerous_permissions(manifestFile, excludeGroups=None):
    """
        get all dangerous permissions in manifest file
    """

    allPermissions = get_all_use_permissions(manifestFile)

    if not allPermissions:
        return None

    result = dict()

    for p in allPermissions:

        dangerousInfo = try_get_dangerous_permission_info(p)
        if not dangerousInfo:
            log_utils.debug(p + " is not dangerous permission")
            continue

        if excludeGroups != None and dangerousInfo['group'] in excludeGroups:
            log_utils.debug(
                dangerousInfo['permission'] + " is not put to dangerous list. because group " + dangerousInfo[
                    'group'] + " is excluded.")
            continue

        if dangerousInfo['group'] not in result:
            result[dangerousInfo['group']] = dangerousInfo

        else:
            log_utils.debug(
                dangerousInfo['permission'] + "is not put to dangerous list. because group " + dangerousInfo[
                    'group'] + " already has a permission in it.")

    return result


def write_dangerous_permissions(manifestFile, decompileDir, permissions, protocolUrl, isLandscape):
    permissionFile = os.path.join(decompileDir, "assets")
    if not os.path.exists(permissionFile):
        os.makedirs(permissionFile)
    permissionFile = os.path.join(permissionFile, "u8_permissions.xml")

    needWriteSettings = is_need_write_settings(manifestFile)

    targetTree = None
    targetRoot = None
    pluginNodes = None

    targetTree = ElementTree()
    targetRoot = Element('permissions')
    targetTree._setroot(targetRoot)

    if needWriteSettings:
        targetRoot.set('writeSettings', 'true')
    else:
        targetRoot.set('writeSettings', 'false')

    if protocolUrl != None and len(protocolUrl) > 0:
        targetRoot.set('protocolUrl', protocolUrl)

    if isLandscape:
        targetRoot.set('protocolOrientation', 'landscape')
    else:
        targetRoot.set('protocolOrientation', 'portrait')

    for pkey in permissions:
        p = permissions[pkey]
        typeTag = 'permission'
        typeName = p['permission']
        typeCName = p['name']
        typeGroup = p['group']
        pluginNode = SubElement(targetRoot, typeTag)
        pluginNode.set('name', typeName)
        pluginNode.set('cname', typeCName)
        pluginNode.set('group', typeGroup)

    targetTree.write(permissionFile, 'UTF-8')


def append_permission_activity(manifestFile, isLandscape, oldStartActivity):
    """
        add uni permission activity into AndroidManifest.xml
    """
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}name'
    screenkey = '{' + androidNS + '}screenOrientation'
    theme = '{' + androidNS + '}theme'
    launchMode = '{' + androidNS + '}launchMode'
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return

    permissionNode = SubElement(applicationNode, 'activity')
    permissionNode.set(key, 'com.u8.sdk.permission.U8PermissionActivity')
    permissionNode.set(theme, '@android:style/Theme.Black.NoTitleBar.Fullscreen')
    permissionNode.set(launchMode, 'singleTask')

    if isLandscape:
        permissionNode.set(screenkey, 'landscape')
    else:
        permissionNode.set(screenkey, 'portrait')

    intentNode = SubElement(permissionNode, 'intent-filter')
    actionNode = SubElement(intentNode, 'action')
    actionNode.set(key, 'android.intent.action.MAIN')
    categoryNode = SubElement(intentNode, 'category')
    categoryNode.set(key, 'android.intent.category.LAUNCHER')
    tree.write(manifestFile, 'UTF-8')

    # 将permission activity 作为启动Activity之后，设置了android:launchMode为singleTask， 防止游戏之前的启动Activity也是singleTask，导致返回Home，再次点击游戏的时候，回不到游戏界面，这里将游戏Activity的启动模式设置为standard
    manifest_utils.change_prop_on_component(manifestFile, 'activity', oldStartActivity, launchMode, 'standard')


def handle_auto_permission(game, channel, decompileDir):
    autoPermission = False
    directPermission = False
    protocolUrl = "file:///android_asset/m_userprotocol.html"

    if 'autoPermission' in channel and len(str(channel['autoPermission'])) > 0 and str(
            channel['autoPermission']) == '1':
        autoPermission = True
    elif 'autoPermission' in game and len(game['autoPermission']) > 0 and game['autoPermission'] == '1':
        autoPermission = True

    if 'noPermission' in channel and len(channel['noPermission']) > 0 and channel['noPermission'] == '1':
        autoPermission = False
    elif 'noPermission' in game and len(game['noPermission']) > 0 and game['noPermission'] == '1':
        autoPermission = False

    if not autoPermission:
        log_utils.debug("autoPermission not enabled.")
        return

    excludeGroups = None

    if "excludePermissionGroups" in channel and channel["excludePermissionGroups"] != None and len(
            channel['excludePermissionGroups']) > 0:
        excludeGroups = channel['excludePermissionGroups'].split(',')
    elif "excludePermissionGroups" in game and game["excludePermissionGroups"] != None and len(
            game['excludePermissionGroups']) > 0:
        excludeGroups = game['excludePermissionGroups'].split(',')

    if 'directPermission' in channel and len(str(channel['directPermission'])) > 0 and str(
            channel['directPermission']) == '1':
        directPermission = True
    elif 'directPermission' in game and len(game['directPermission']) > 0 and game['directPermission'] == '1':
        directPermission = True
    else:
        directPermission = False

    autoProtocol = True

    log_utils.debug("auto protocol:" + str(channel['autoProtocol']))

    if 'autoProtocol' in channel and len(str(channel['autoProtocol'])) > 0 and str(channel['autoProtocol']) == '0':
        autoProtocol = False

    if autoProtocol:
        if 'protocolUrl' in channel and len(channel['protocolUrl']) > 0:
            protocolUrl = channel['protocolUrl']
        elif 'protocolUrl' in game and len(game['protocolUrl']) > 0:
            protocolUrl = game['protocolUrl']
    else:
        protocolUrl = None

    manifestFile = os.path.join(decompileDir, 'AndroidManifest.xml')
    permissions = get_dangerous_permissions(manifestFile, excludeGroups)

    if permissions == None or len(permissions) == 0:
        log_utils.debug("there is no dangerous permission. just ignore auto permission")
        return

    isLandscape = False
    if game["orientation"] == 'landscape':
        isLandscape = True

    write_dangerous_permissions(manifestFile, decompileDir, permissions, protocolUrl, isLandscape)

    if directPermission:
        log_utils.debug("curr permission request type is direct permission. no permission activity need")
        return

    # remove original launcher activity of the game
    activityName = manifest_utils.remove_start_activity(manifestFile)
    append_permission_activity(manifestFile, isLandscape, activityName)

    smaliTargetPath = decompileDir + "/smali"

    # if U8PermissionActivity.smali may exists in smali, smali_classes2, smali_classes3...
    retryNum = 1
    while retryNum > 0:

        curPath = smaliTargetPath

        if retryNum > 1:
            curPath = smaliTargetPath + "_classes" + str(retryNum)

        if not os.path.exists(curPath):
            retryNum = -1
        else:
            retryNum = retryNum + 1
            permissionActivityPath = curPath + "/com/u8/sdk/permission/U8PermissionActivity.smali"
            file_utils.modifyFileContent(permissionActivityPath, '{U8SDK_Permission_Next_Activity}', activityName)

    log_utils.info("modify permission smali file success.")
