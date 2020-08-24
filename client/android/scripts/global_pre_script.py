
# -*- coding: utf-8 -*-
#Author:xiaohei
#CreateTime:2018-05-16
#
# All apk operations are defined here
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
import sdk_helper

androidNS = 'http://schemas.android.com/apk/res/android'


def delete_aibei_res(decompileDir):
    """
        remove aibei resources in base apk
    """

    filelist = ["colors","dimens","ids","public","strings","styles", "values"]

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

                if attribName.lower().startswith("ipay_"):
                    root.remove(node)
                    log_utils.debug("remove debug res index name:"+attribName+" from"+fpath)  
                elif attribName.lower() == 'custom_dialog':
                    root.remove(node)
                    log_utils.debug("remove debug res index name:"+attribName+" from"+fpath)  

            tree.write(fpath, "UTF-8")


    filelist = ["ab_login_values.xml","ab_pay_values.xml"]

    for f in filelist:
        path = os.path.join(decompileDir, 'res/values', f)
        file_utils.del_file_folder(path)
        log_utils.debug("remove debug res file:"+path)


    resPath = decompileDir + "/res"

    filelist = file_utils.list_files(resPath, [], [])
    for f in filelist:
        if os.path.basename(f).lower().startswith("ipay_"):
            file_utils.del_file_folder(f)
            log_utils.debug("remove debug res file:"+f)


    devConfig = decompileDir + "/assets/u8_developer_config.properties"
    pluginConfig = decompileDir + "/assets/u8_plugin_config.xml"
    permissionConfig = decompileDir + "/assets/u8_permissions.xml"

    file_utils.del_file_folder(devConfig)
    file_utils.del_file_folder(pluginConfig) 
    file_utils.del_file_folder(permissionConfig)     

    smaliPath = decompileDir + "smali"

    smaliIndex = 1
    otherSmaliFolder = "smali"
    while os.path.exists(os.path.join(decompileDir, otherSmaliFolder)):
        
        #check class
        dels = [
            "com/u8/sdk/ABSDK",
            "com/u8/sdk/ABUser",
            "com/u8/sdk/ABPay",
            "com/iapppay/",
            "com/alipay/",
            "com/ta/utdid2/",
            "com/ut/device/",
            "org/json/alipay/",
            "org/apache/commons/",
            "org/apache/http/"
        ]

        for toDel in dels:
            file_utils.del_file_folder(os.path.join(decompileDir, otherSmaliFolder, toDel))
            log_utils.debug("remove debug res :"+toDel)

        smaliIndex = smaliIndex + 1
        otherSmaliFolder = "smali_classes" + str(smaliIndex)    


    manifestFile = decompileDir + "/AndroidManifest.xml"
    ET.register_namespace('android', androidNS)
    tree = ET.parse(manifestFile)
    root = tree.getroot()

    permissionDels = [
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.CHANGE_NETWORK_STATE",
        "android.permission.ACCESS_WIFI_STATE",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.READ_SMS",
        "android.permission.WRITE_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.SEND_SMS",
        "android.permission.MOUNT_UNMOUNT_FILESYSTEMS",
        "android.permission.SYSTEM_ALERT_WINDOW"        
    ]

    componentDes = [
        "com.iapppay.sdk.main.WebActivity",
        "com.iapppay.ui.activity.PersonCenterActivity",
        "com.iapppay.ui.activity.PayHubActivity",
        "com.iapppay.ui.activity.SelectAmountActivity",
        "com.iapppay.ui.activity.ChargeActivity",
        "com.iapppay.ui.card.GamepayActivity",
        "com.alipay.sdk.app.H5PayActivity",
        "com.alipay.sdk.auth.AuthActivity",
        "com.iapppay.openid.channel.ui.LoginActivity",
        "com.iapppay.openid.channel.ui.RegisterActivity",
        "com.iapppay.openid.channel.ui.OneRegisterActivity",
        "com.iapppay.openid.channel.ui.OpenIdBaseActivity",
        "com.iapppay.openid.channel.ui.BindPhoneActivity",
        "com.iapppay.openid.channel.ui.NameAuthActivity",
        "com.iapppay.openid.channel.ui.FindPasswordActivity",
        "com.iapppay.openid.channel.ui.ModifyUserNameActivity",
        "com.iapppay.openid.channel.ui.ModifyPasswordActivity",
        "com.iapppay.openid.channel.ui.SettingCenterActivity",
        "com.iapppay.openid.channel.ui.UnbindPhoneActivity",
        "com.iapppay.openid.channel.ui.CommonWebActivity"
    ]

    key = '{'+androidNS+'}name'
    permissionLst = root.findall('uses-permission')
    if permissionLst != None and len(permissionLst) > 0:
        for aNode in permissionLst:
            permissionName = aNode.attrib[key]

            if permissionName in permissionDels:
                root.remove(aNode)
                log_utils.debug("remove debug permission:"+permissionName)

    permission23 = root.find('uses-permission-sdk-23')

    if permission23 != None:
        root.remove(permission23)
        log_utils.debug("remove debug permission:"+permission23.attrib[key])

    appNode = root.find('application')
    if appNode != None:
        activityLst = appNode.findall('activity')
        if activityLst != None and len(activityLst) > 0:
            for aNode in activityLst:
                activityName = aNode.attrib[key]  

                if activityName in componentDes:
                    appNode.remove(aNode)
                    log_utils.debug("remove debug activity:"+activityName)


    tree.write(manifestFile, "UTF-8")


def execute(decompileDir, game, channel):

    #delete_aibei_res(decompileDir)

    manifest = decompileDir + '/AndroidManifest.xml'
    ET.register_namespace('android', androidNS)
    compileSdkVersion = '{' + androidNS + '}compileSdkVersion'
    compileSdkVersionCodename = '{' + androidNS + '}compileSdkVersionCodename'
    platformBuildVersionCode = '{' + androidNS + '}platformBuildVersionCode'
    platformBuildVersionName = '{' + androidNS + '}platformBuildVersionName'

    tree = ET.parse(manifest)
    root = tree.getroot() 

    applicationNode = root.find('application')

    root.attrib.pop(compileSdkVersion, None)
    root.attrib.pop(compileSdkVersionCodename, None)
    root.attrib.pop(platformBuildVersionCode, None)
    root.attrib.pop(platformBuildVersionName, None)

    tree.write(manifest, 'UTF-8')    
