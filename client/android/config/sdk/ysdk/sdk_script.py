import file_utils
import sdk_helper
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
import sdk_helper

androidNS = 'http://schemas.android.com/apk/res/android'


def execute(channel, decompileDir, packageName):
    # checkURL(channel, decompileDir, packageName)

    sdk_helper.setPropOnApplicationNode(decompileDir, '{' + androidNS + '}usesCleartextTraffic',
                                        'true')

    # compileWXEntryActivity(channel, decompileDir, packageName)
    sdk_helper.compileJava2Smali(channel, decompileDir, packageName + ".wxapi", 'WXEntryActivity',
                                 ["YSDK.jar"])

    modifyManifest(channel, decompileDir, packageName)

    modifyActivityForSingleTop(channel, decompileDir, packageName)

    generateYSDKConfig(channel, decompileDir, packageName)

    return 0


def checkURL(channel, decompileDir, packageName):
    game = config_utils.currGame

    u8serverUrl = None

    if "u8server_url" in game:
        u8serverUrl = game["u8server_url"]

    local_config = config_utils.getLocalConfig()
    if u8serverUrl is None and "u8server_url" in local_config:
        u8serverUrl = local_config['u8server_url']

    if u8serverUrl is None:
        return

    while u8serverUrl.endswith('/'):
        u8serverUrl = u8serverUrl[0:-1]

    if 'params' in channel:
        params = channel['params']
        for p in params:
            if p['name'] == 'WG_QUERY_URL':
                url = p['value']
                if not url.startswith('http'):
                    # p['WG_QUERY_URL'] = u8serverUrl + url
                    p['value'] = u8serverUrl + url

            elif p['name'] == 'WG_PAY_URL':
                url = p['value']
                if not url.startswith('http'):
                    # p['WG_PAY_URL'] = u8serverUrl + url
                    p['value'] = u8serverUrl + url


def generateYSDKConfig(channel, decompileDir, packageName):
    qqAppID = ""
    wxAppID = ""
    offerID = ""
    ysdkUrl = ""
    realName = ""
    cfStr = ""

    if 'params' in channel:
        params = channel['params']
        for p in params:
            if p['name'] == 'QQ_APP_ID':
                qqAppID = p['value']
            elif p['name'] == 'WX_APP_ID':
                wxAppID = p['value']
            elif p['name'] == 'OFFER_ID':
                offerID = p['value']
            elif p['name'] == 'YSDK_URL':
                ysdkUrl = p['value']
            elif p['name'] == 'YSDK_REAL_NAME':
                realName = p['value']

    if realName is None or len(realName) == 0:
        realName = "true"

    cfStr = cfStr + "QQ_APP_ID=" + qqAppID + "\n"
    cfStr = cfStr + "WX_APP_ID=" + wxAppID + "\n"
    cfStr = cfStr + "OFFER_ID=" + offerID + "\n"
    cfStr = cfStr + "YSDK_URL=" + ysdkUrl + "\n"
    cfStr = cfStr + "YSDK_ANTIADDICTION_SWITCH=" + realName + "\n"
    cfStr = cfStr + "YSDK_ICON_SWITCH=true\n"

    cfStr = cfStr + "YSDK_IMMERSIVE_ICON_SWITCH=true\n"
    cfStr = cfStr + "YSDK_MSG_BOX_SWITCH=true\n"
    cfStr = cfStr + "YSDK_ICON_CAPTURE_SWITCH=false\n"
    cfStr = cfStr + "YSDK_ICON_LOCATION=100\n"
    cfStr = cfStr + "YSDK_H5GAME_SWITCH=false\n"

    filepath = os.path.join(decompileDir, "assets/ysdkconf.ini")
    if os.path.exists(filepath):
        os.remove(filepath)

    f = open(filepath, 'w')
    f.write(cfStr)
    f.close()


def modifyManifest(channel, decompileDir, packageName):
    qqAppID = ""
    wxAppID = ""

    if 'params' in channel:
        params = channel['params']
        for param in params:
            if param['name'] == 'QQ_APP_ID':
                qqAppID = param['value']
            elif param['name'] == 'WX_APP_ID':
                wxAppID = param['value']

    manifest = decompileDir + '/AndroidManifest.xml'

    file_utils.modifyFileContent(manifest, '${applicationId}', packageName)
    file_utils.modifyFileContent(manifest, '${YSDK_QQ_APPID}', qqAppID)
    file_utils.modifyFileContent(manifest, '${YSDK_WX_APPID}', wxAppID)

    # ET.register_namespace('android', androidNS)
    # name = '{' + androidNS + '}name'    
    # scheme = '{' + androidNS + '}scheme'
    # taskAffinity = '{' + androidNS + '}taskAffinity'
    # tree = ET.parse(manifest)
    # root = tree.getroot()

    # appNode = root.find('application')
    # if appNode is None:
    #     return 1

    # activityNodes = appNode.findall('activity')
    # if activityNodes != None and len(activityNodes) > 0:
    #     for activityNode in activityNodes:
    #         activityName = activityNode.get(name)
    #         if activityName == 'com.tencent.tauth.AuthActivity':
    #             intentFilters = activityNode.findall('intent-filter')
    #             if intentFilters != None and len(intentFilters) > 0:
    #                 for intentNode in intentFilters:
    #                     dataNode = SubElement(intentNode, 'data')
    #                     dataNode.set(scheme, 'tencent'+qqAppID)
    #                     break
    #         if activityName == 'com.tencent.ysdk.module.user.impl.freelogin.FreeLoginInfoActivity':
    #             intentFilters = activityNode.findall('intent-filter')
    #             if intentFilters != None and len(intentFilters) > 0:
    #                 for intentNode in intentFilters:
    #                     dataNode = SubElement(intentNode, 'data')
    #                     dataNode.set(scheme, 'tencentysdk'+qqAppID)
    #                     break                       
    #         elif activityName == '.wxapi.WXEntryActivity':
    #             activityNode.set(name, packageName+activityName)
    #             activityNode.set(taskAffinity, packageName + '.diff')
    #             intentFilters = activityNode.findall('intent-filter')
    #             if intentFilters != None and len(intentFilters) > 0:
    #                 for intentNode in intentFilters:
    #                     dataNode = SubElement(intentNode, 'data')
    #                     dataNode.set(scheme, wxAppID)
    #                     break

    # tree.write(manifest, 'UTF-8')


def compileWXEntryActivity(channel, decompileDir, packageName):
    sdkDir = decompileDir + '/../sdk/' + channel['sdk']
    if not os.path.exists(sdkDir):
        file_utils.printF("The sdk temp folder is not exists. path:" + sdkDir)
        return 1

    extraFilesPath = sdkDir + '/extraFiles'
    relatedJar = os.path.join(extraFilesPath, 'YSDK.jar')
    WXPayEntryActivity = os.path.join(extraFilesPath, 'WXEntryActivity.java')
    file_utils.modifyFileContent(WXPayEntryActivity, 'com.example.wegame.wxapi',
                                 packageName + ".wxapi")

    splitdot = ';'
    if platform.system() == 'Darwin' or platform.system() == 'Linux':
        splitdot = ':'

    cmd = '"%sjavac" -source 1.7 -target 1.7 "%s" -classpath "%s"%s"%s"' % (
        file_utils.getJavaBinDir(), WXPayEntryActivity, relatedJar, splitdot,
        file_utils.getFullToolPath('android.jar'))

    ret = file_utils.execFormatCmd(cmd)
    if ret:
        return 1

    packageDir = packageName.replace('.', '/')
    srcDir = sdkDir + '/tempDex'
    classDir = srcDir + '/' + packageDir + '/wxapi'

    if not os.path.exists(classDir):
        os.makedirs(classDir)

    sourceClassFilePath = os.path.join(extraFilesPath, 'WXEntryActivity.class')
    targetClassFilePath = classDir + '/WXEntryActivity.class'

    file_utils.copy_file(sourceClassFilePath, targetClassFilePath)

    targetDexPath = os.path.join(sdkDir, 'WXEntryActivity.dex')

    dxTool = file_utils.getFullToolPath("/lib/dx.jar")

    cmd = file_utils.getJavaCMD() + ' -jar -Xmx512m -Xms512m "%s" --dex --output="%s" "%s"' % (
        dxTool, targetDexPath, srcDir)

    ret = file_utils.execFormatCmd(cmd)

    if ret:
        return 1

    ret = sdk_helper.dex2smali(targetDexPath, decompileDir + '/smali', "baksmali.jar")

    if ret:
        return 1


def modifyActivityForSingleTop(channel, decompileDir, packageName):
    manifestFile = decompileDir + "/AndroidManifest.xml"
    manifestFile = file_utils.getFullPath(manifestFile)
    ET.register_namespace('android', androidNS)
    key = '{' + androidNS + '}launchMode'
    keyName = '{' + androidNS + '}name'
    screenKey = '{' + androidNS + '}screenOrientation'

    tree = ET.parse(manifestFile)
    root = tree.getroot()

    applicationNode = root.find('application')
    if applicationNode is None:
        return 1

    activityNodeLst = applicationNode.findall('activity')
    if activityNodeLst is None:
        return

    activityName = ''

    screenOrientation = 'sensorLandscape'

    for activityNode in activityNodeLst:
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
                if actionNode.attrib[keyName] == 'android.intent.action.MAIN':
                    bFindAction = True
                    break

            categoryNodeLst = intentNode.findall('category')
            if categoryNodeLst is None:
                break
            for categoryNode in categoryNodeLst:
                if categoryNode.attrib[keyName] == 'android.intent.category.LAUNCHER':
                    bFindCategory = True
                    break

            if bFindAction and bFindCategory:
                bMain = True

                break

        if bMain:
            activityNode.set(key, "singleTop")
            screenOrientation = activityNode.get(screenKey)
            break

    activityNodes = applicationNode.findall('activity')
    if activityNodes != None and len(activityNodes) > 0:
        for activityNode in activityNodes:
            activityName = activityNode.get(keyName)
            if activityName == 'com.tencent.midas.proxyactivity.APMidasPayProxyActivity':

                if screenOrientation and len(screenOrientation) > 0:
                    activityNode.set(screenKey, screenOrientation)
                else:
                    activityNode.set(screenKey, 'portrait')

                break

    tree.write(manifestFile, 'UTF-8')

    return 0
