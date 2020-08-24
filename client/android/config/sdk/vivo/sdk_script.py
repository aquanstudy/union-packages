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
import json

androidNS = 'http://schemas.android.com/apk/res/android'


def handleWXPayActivity(channel, decompileDir, packageName, className, oldPackageName):

	sdkDir = decompileDir + '/../sdk/' + channel['sdk']
	if not os.path.exists(sdkDir):
		file_utils.printF("The sdk temp folder is not exists. path:"+sdkDir)
		return 1

	extraFilesPath = sdkDir + '/extraFiles'
	relatedJar = os.path.join(extraFilesPath, 'vivoUnionSDK.jar')
	relatedJar2 = os.path.join(extraFilesPath, 'libammsdk.jar')
	WXPayEntryActivity = os.path.join(extraFilesPath, className+'.java')
	file_utils.modifyFileContent(WXPayEntryActivity, oldPackageName, packageName+".wxapi")

	splitdot = ';'
	if platform.system() == 'Darwin':
		splitdot = ':'

	cmd = '"%sjavac" -source 1.7 -target 1.7 "%s" -classpath "%s"%s"%s"%s"%s"' % (file_utils.getJavaBinDir(), WXPayEntryActivity, relatedJar,splitdot,relatedJar2, splitdot, file_utils.getFullToolPath('android.jar'))

	ret = file_utils.execFormatCmd(cmd)
	if ret:
		return 1

	packageDir = packageName.replace('.', '/')
	srcDir = sdkDir + '/tempDex'
	classDir = srcDir + '/' + packageDir + '/wxapi'

	if not os.path.exists(classDir):
		os.makedirs(classDir)

	sourceClassFilePath = os.path.join(extraFilesPath, className + '.class')
	targetClassFilePath = classDir + '/' + className + '.class'

	file_utils.copy_file(sourceClassFilePath, targetClassFilePath)

	targetDexPath = os.path.join(sdkDir, className+'.dex')

	dxTool = file_utils.getFullToolPath("/lib/dx.jar")

	cmd = file_utils.getJavaCMD() + ' -jar -Xmx512m -Xms512m "%s" --dex --output="%s" "%s"' % (dxTool, targetDexPath, srcDir)



	ret = file_utils.execFormatCmd(cmd)

	if ret:
		return 1

	ret = sdk_helper.dex2smali(targetDexPath, decompileDir+'/smali', "baksmali.jar")

	if ret:
		return 1

	return 0


def execute(channel, decompileDir, packageName):

    appID = sdk_helper.getSdkParamByKey(channel, 'Vivo_AppID')

    supplierconfig = os.path.join(decompileDir, 'assets/supplierconfig.json')

    with open(supplierconfig, 'r') as f:

        content = f.read()
        config = json.loads(content)

    config['supplier']['vivo']['appid'] = appID

    with open(supplierconfig, 'w') as f:
        f.write(json.dumps(config))

    return 0

