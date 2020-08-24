# -*- coding: utf-8 -*-
#Author:xiaohei
#CreateTime:2018-11-15
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
from res_merger import ResourceMerger
import manifest_merger
import apk_helper

androidNS = 'http://schemas.android.com/apk/res/android'


def decompile(source, targetdir):
    """
        decompile apk to targetdir
    """
    apkfile = source
    apktool = file_utils.getFullToolPath('apktool2.jar')
    if os.path.exists(targetdir):
        file_utils.del_file_folder(targetdir)
    if not os.path.exists(targetdir):
        os.makedirs(targetdir)

    heapSize = config_utils.get_jvm_heap_size()
    cmd = '"%s" -jar -Xms%sm -Xmx%sm "%s" -v d -b --only-main-classes -f "%s" -o "%s"' % (file_utils.getJavaCMD(), heapSize, heapSize, apktool, apkfile, targetdir)
    ret = file_utils.exec_cmd(cmd)
    return ret


def recompile(sourcefolder, apkfile):
    """
        recompile apk
    """

    apktool = file_utils.getFullToolPath('apktool2.jar')

    ret = False
    if os.path.exists(sourcefolder):
        heapSize = config_utils.get_jvm_heap_size()
        cmd = '"%s" -jar -Xms%sm -Xmx%sm "%s" -v b -f "%s" -o "%s"' % (file_utils.getJavaCMD(), heapSize, heapSize, apktool, sourcefolder, apkfile)
        ret = file_utils.exec_cmd(cmd)
    else:
        log_utils.error("recompile failed. apk source folder not exists.")

    return ret


def generate_keystore(workDir, game, channel):
    """
        auto generate keystore file
        if user want to use auto generated keystore file, you can use this method
    """

    keytoolPath = file_utils.getJavaBinDir()+"keytool"

    keystorePath = os.path.join(workDir, 'temp_keystore')
    if not os.path.exists(keystorePath):
        os.makedirs(keystorePath)

    keystore = dict()
    keystore['keystore'] =  os.path.join(keystorePath, "temp.keystore")
    keystore['password'] = channel['name']+game['appName']
    keystore['aliaskey'] = game["appName"]+channel["name"]+time.strftime('%Y%m%d%H%M%S')
    keystore['aliaspwd'] = channel['name']+game['appName']
    keystore['sigalg'] = "SHA1withRSA"

    dname = "CN=mqttserver.ibm.com, OU=ID, O=IBM, L=Hursley, S=Hants, C=GB"

    cmd = '"%s" -genkeypair -dname "%s" -alias "%s" -keyalg "RSA" -sigalg "%s" -validity 20000 -keystore "%s" -storepass "%s" -keypass "%s" ' % (keytoolPath, dname, keystore['aliaskey'],keystore['sigalg'], keystore['keystore'], keystore['password'], keystore['aliaspwd'])

    ret = file_utils.exec_cmd(cmd)

    if ret:
        return None

    return keystore


def sign_apk_v1(apkfile, keystore, password, alias, aliaspwd, sigalg):

    aapt = file_utils.getFullToolPath("aapt")

    if not os.path.exists(keystore):
        log_utils.error("the keystore file is not exists. %s", keystore)
        return False

    listcmd = '%s list %s' % (aapt, apkfile)

    output = os.popen(listcmd).read()
    for filename in output.split('\n'):
        if filename.find('META_INF') == 0:
            rmcmd = '"%s" remove "%s" "%s"' % (aapt, apkfile, filename)
            file_utils.exec_cmd(rmcmd)


    if sigalg is None:
        sigalg = "SHA1withRSA"

    signcmd = '"%sjarsigner" -digestalg SHA1 -sigalg %s -keystore "%s" -storepass "%s" -keypass "%s" "%s" "%s" ' % (file_utils.getJavaBinDir(),sigalg,
            keystore, password, aliaspwd, apkfile, alias)

    ret = file_utils.exec_cmd(signcmd)

    return ret


def sign_apk_v2(apkfile, targetapkfile, keystore, password, alias, aliaspwd):

    aapt = file_utils.getFullToolPath("aapt")
    apksigner = file_utils.getFullToolPath("apksigner.jar")

    if not os.path.exists(keystore):
        log_utils.error("the keystore file is not exists. %s", keystore)
        return False

    listcmd = '%s list %s' % (aapt, apkfile)

    output = os.popen(listcmd).read()
    for filename in output.split('\n'):
        if filename.find('META_INF') == 0:
            rmcmd = '"%s" remove "%s" "%s"' % (aapt, apkfile, filename)
            file_utils.exec_cmd(rmcmd)

    signcmd = '"%s" -jar "%s" sign -v --ks "%s" --ks-key-alias "%s" --ks-pass pass:"%s" --key-pass pass:"%s" --out "%s" "%s" ' % (file_utils.getJavaCMD(),apksigner,
            keystore,alias, password, aliaspwd, targetapkfile, apkfile)

    ret = file_utils.exec_cmd(signcmd)

    return ret


def align_apk(apkfile, targetapkfile):

    """
        zip align the apk file
    """

    align = file_utils.getFullToolPath('zipalign')
    aligncmd = '"%s" -f 4 "%s" "%s"' % (align, apkfile, targetapkfile)

    ret = file_utils.exec_cmd(aligncmd)

    return ret


def generate_rindex_v1(genPath, resPath, manifestPath):
    """
        generate R.java with aapt
    """
    
    androidPath = file_utils.getFullToolPath("android.jar")
    aaptPath = file_utils.getFullToolPath("aapt")

    cmd = '"%s" p -f -m -J "%s" -S "%s" -I "%s" -M "%s"' % (aaptPath, genPath, resPath, androidPath, manifestPath)
    ret = file_utils.exec_cmd(cmd)  

    return ret


def generate_rindex_v2(workDir, genPath, resPath, manifestPath):
    """
        generate R.java with appt2
    """

    androidPath = file_utils.getFullToolPath("android.jar")

    if platform.system() == 'Windows':

        aapt2Path = file_utils.getFullToolPath("aapt2.exe")
    else:
        aapt2Path = file_utils.getFullToolPath("aapt2")

    if not os.path.exists(aapt2Path):
        log_utils.error("aapt2 is not exists in tool")
        return False

    #compile res first...
    resFlatPath = workDir + "/res_flat.zip"
    cmd = '"%s" compile -o "%s" --dir "%s" -v' % (aapt2Path, resFlatPath, resPath)
    ret = file_utils.exec_cmd(cmd)
    if not ret:
        return False

    #link res to generate R.java
    resTempPath = workDir + "/res.apk"
    cmd = '"%s" link -o "%s" --manifest "%s" -I "%s" --java "%s" "%s" -v' % (aapt2Path, resTempPath, manifestPath, androidPath, genPath, resFlatPath)
    ret = file_utils.exec_cmd(cmd)

    return ret   