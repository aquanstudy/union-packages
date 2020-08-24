# encoding:utf-8
# Author:xiaohei
# CreateTime:2014-10-25
#
# The config operations
#
#
import sys
import os
import os.path
import file_utils
import log_utils
import traceback
from xml.etree import ElementTree as ET
from xml.etree.ElementTree import SubElement
from xml.etree.ElementTree import Element
from xml.etree.ElementTree import ElementTree
import json
import db_utils

currGame = None
currDBConfig = None
currFileStorePath = None


def init_config(tempFilePath, gameID):
    if tempFilePath != None and len(tempFilePath) > 0:
        global currFileStorePath
        currFileStorePath = tempFilePath

    dbPath = os.path.join(tempFilePath, 'games', 'game' + str(gameID), 'u8_temp_db_config.txt')
    if not os.path.exists(dbPath):
        return

    cf = open(dbPath, "r")
    lines = cf.readlines()
    cf.close()

    config = {}

    for line in lines:
        line = line.strip()
        if len(line) == 0:
            continue
        dup = line.split('=')
        config[dup[0]] = dup[1]

    global currDBConfig
    currDBConfig = config


def get_local_config():
    configFile = file_utils.getFullPath("config/local/local.properties")

    if not os.path.exists(configFile):
        log_utils.error("local.properties is not exists. %s " % configFile)
        return None

    cf = open(configFile, "r")
    lines = cf.readlines()
    cf.close()

    config = {}

    for line in lines:
        line = line.strip()
        if len(line) == 0:
            continue
        dup = line.split('=')
        config[dup[0]] = dup[1]

    return config


def get_file_temp_path():
    global currFileStorePath
    if currFileStorePath != None and len(currFileStorePath) > 0:
        return currFileStorePath

    localConfig = get_local_config()

    if localConfig and "file_temp_path" in localConfig:
        currFileStorePath = localConfig['file_temp_path']

    return currFileStorePath


def get_db_config():
    global currDBConfig
    if currDBConfig == None:
        currDBConfig = get_local_config()

    return currDBConfig


def get_tool_version():
    config = get_local_config()
    if config and "tool_versionName" in config:
        return config['tool_versionName']

    return "unkown"


def get_jvm_heap_size():
    config = get_local_config()
    if config and "jdk_heap_size" in config:
        return config['jdk_heap_size']

    return 512


def get_py_version():
    version = sys.version_info
    major = version.major
    minor = version.minor
    micro = version.micro

    currVersion = str(major) + "." + str(minor) + "." + str(micro)

    return currVersion


def is_py_env_2():
    version = sys.version_info
    major = version.major
    return major == 2


def load_channel_config(game, channel, packlog):
    tblSDKParams = {}

    if "channelParams" in packlog:
        channelParams = packlog["channelParams"]
        log_utils.debug("load channel params from packlog. " + channelParams)
        tblSDKParams = json.loads(channelParams)

    # load channel config
    ret = load_channel_user_config(game, channel, tblSDKParams)

    if ret:
        return 1

    # load channel plugins
    plugins = db_utils.get_channel_plugin_params(game["id"], channel["id"])

    if plugins == None:
        return 0

    localPlugins = []
    for pk in plugins:
        plugin = {}
        plugin["name"] = pk

        if plugins[pk] == None:
            log_utils.error("plugin params not configed of " + pk + "; just igore this plugin.")
            continue

        pluginParams = dict()
        if plugins[pk] and len(plugins[pk]) > 0:
            pluginParams = json.loads(plugins[pk])

        # log_utils.debug("curr plugin name:"+pk+";params:"+pluginParams)
        ret = load_plugin_config(channel, plugin, pluginParams)
        if ret:
            return 1

        localPlugins.append(plugin)

    channel['third-plugins'] = localPlugins

    return 0


def load_plugin_config(channel, plugin, tblSDKParams):
    configFile = file_utils.getFullPath("config/plugin/" + plugin["name"] + "/config.xml")

    if not os.path.exists(configFile):
        log_utils.error("the plugin %s config.xml file is not exists.path:%s", plugin["name"], configFile)
        return 1

    try:
        tree = ET.parse(configFile)
        root = tree.getroot()
    except:
        log_utils.error("can not parse config.xml.path:%s", configFile)
        return 1

    configNode = root

    # subpluginNodes = configNode.find("subplugins")

    # if subpluginNodes != None and len(subpluginNodes) > 0:
    #     plugin['subplugins'] = []
    #     for subNode in subpluginNodes:
    #         subplugin = {}
    #         subplugin['name'] = subNode.get('name')
    #         subplugin['desc'] = subNode.get('desc')
    #         subParamNodes = subNode.findall('param')
    #         subplugin['params'] = []
    #         if subParamNodes != None and len(subParamNodes) > 0:
    #             for subParamNode in subParamNodes:
    #                 param = {}
    #                 param['name'] = subParamNode.get('name')
    #                 param['value'] = subParamNode.get('value')
    #                 #log_utils.debug("name:"+param['name']+";val:"+param['value'])
    #                 param['required'] = subParamNode.get('required')
    #                 param['showName'] = subParamNode.get('showName')
    #                 param['bWriteInManifest'] = subParamNode.get('bWriteInManifest')
    #                 param['bWriteInClient'] = subParamNode.get('bWriteInClient')
    #                 subplugin['params'].append(param)

    #         plugin['subplugins'].append(subplugin)

    paramNodes = configNode.find("params")
    plugin['params'] = []
    if paramNodes != None and len(paramNodes) > 0:

        for paramNode in paramNodes:
            param = {}
            param['name'] = paramNode.get('name')
            param['value'] = paramNode.get('value')
            param['required'] = paramNode.get('required')
            param['showName'] = paramNode.get('showName')
            param['bWriteInManifest'] = paramNode.get('bWriteInManifest')
            param['bWriteInClient'] = paramNode.get('bWriteInClient')

            if param['name'] in tblSDKParams:
                param['value'] = tblSDKParams[param["name"]]

            plugin['params'].append(param)

    operationNodes = configNode.find("operations")
    plugin['operations'] = []
    if operationNodes != None and len(operationNodes) > 0:

        for opNode in operationNodes:
            op = {}
            op['type'] = opNode.get('type')
            op['from'] = opNode.get('from')
            op['to'] = opNode.get('to')
            plugin['operations'].append(op)

    pluginNodes = configNode.find("plugins")
    if pluginNodes != None and len(pluginNodes) > 0:
        plugin['plugins'] = []
        for pNode in pluginNodes:
            p = {}
            p['name'] = pNode.get('name')
            p['type'] = pNode.get('type')
            plugin['plugins'].append(p)

    extraRNodes = configNode.find("extraR")
    if extraRNodes != None and len(extraRNodes) > 0:
        if "extraRList" not in plugin:
            plugin["extraRList"] = []

        for rNode in extraRNodes:
            name = rNode.get('name')
            if name != None and len(name) > 0 and name not in plugin["extraRList"]:
                plugin["extraRList"].append(name)
                # log_utils.debug("add a new extra R package:"+name)

    if "dependencyList" not in plugin:
        plugin["dependencyList"] = []

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

                plugin["dependencyList"].append(dependencyItem)

    return 0


def load_channel_user_config(game, channel, tblSDKParams):
    configFile = file_utils.getFullPath("config/sdk/" + channel['sdk'] + "/config.xml")

    if not os.path.exists(configFile):
        log_utils.error("the config.xml is not exists of sdk %s.path:%s", channel['name'], configFile)
        return 1

    try:
        tree = ET.parse(configFile)
        root = tree.getroot()
    except:
        log_utils.error("can not parse config.xml.path:%s", configFile)
        return 1

    configNode = root
    channel['params'] = []

    for pkey in tblSDKParams:
        param = {}
        param['name'] = pkey
        param['value'] = tblSDKParams[pkey]["value"]
        param['required'] = '1'
        param['showName'] = ''
        param['bWriteInManifest'] = tblSDKParams[pkey]["toMetaData"]
        param['bWriteInClient'] = tblSDKParams[pkey]["toConfig"]
        channel['params'].append(param)

    paramNodes = configNode.find("params")

    if paramNodes != None and len(paramNodes) > 0:

        for paramNode in paramNodes:
            param = {}
            param['name'] = paramNode.get('name')
            param['required'] = paramNode.get('required')

            if param['required'] == '1':
                log_utils.warning(
                    "all params in config.xml must be not required to config in web client. so required must be 0 for param %s",
                    param['name'])
                continue

            if param['name'] in tblSDKParams:
                log_utils.warning("key %s both configed in web client and config.xml. select the one in web client.")
                continue

            param['value'] = paramNode.get('value')
            param['showName'] = paramNode.get('showName')
            param['bWriteInManifest'] = paramNode.get('bWriteInManifest')
            param['bWriteInClient'] = paramNode.get('bWriteInClient')
            channel['params'].append(param)

    # 支持sdk-params里面配置额外的参数，默认写到assets下面u8_developer_config.properties中
    # begin
    # if channel['sdkParams'] is not None:

    #     for key in channel['sdkParams']:
    #         extraKey = True
    #         if channel['params'] is not None and len(channel['params']) > 0:
    #             for p in channel['params']:
    #                 if p['name'] == key:
    #                     extraKey = False
    #                     break

    #         if extraKey:
    #             param = {}
    #             param['name'] = key
    #             param['value'] = channel['sdkParams'][key]
    #             param['required'] = "1"
    #             param['showName'] = key
    #             param['bWriteInManifest'] = "0"
    #             param['bWriteInClient'] = "1"
    #             channel['params'].append(param)
    # end
    # 支持sdk-params里面配置额外的参数，默认写到assets下面u8_developer_config.properties中

    operationNodes = configNode.find("operations")
    channel['operations'] = []
    if operationNodes != None and len(operationNodes) > 0:

        for opNode in operationNodes:
            op = {}
            op['type'] = opNode.get('type')
            op['from'] = opNode.get('from')
            op['to'] = opNode.get('to')
            channel['operations'].append(op)

    pluginNodes = configNode.find("plugins")
    if pluginNodes != None and len(pluginNodes) > 0:
        channel['plugins'] = []
        for pNode in pluginNodes:
            p = {}
            p['name'] = pNode.get('name')
            p['type'] = pNode.get('type')
            channel['plugins'].append(p)

    versionNode = configNode.find("version")
    if versionNode != None and len(versionNode) > 0:
        versionCodeNode = versionNode.find("versionCode")
        versionNameNode = versionNode.find("versionName")
        # the sdk version code is used to check version update for the sdk.
        if versionCodeNode != None and versionNameNode != None:
            channel['sdkVersionCode'] = versionCodeNode.text
            channel['sdkVersionName'] = versionNameNode.text

    extraRNodes = configNode.find("extraR")
    if extraRNodes != None and len(extraRNodes) > 0:
        if "extraRList" not in channel:
            channel["extraRList"] = []

        for rNode in extraRNodes:
            name = rNode.get('name')
            if name != None and len(name) > 0 and name not in channel["extraRList"]:
                channel["extraRList"].append(name)
                # log_utils.debug("add a new extra R package:"+name)

    if "dependencyList" not in channel:
        channel["dependencyList"] = []

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

                channel["dependencyList"].append(dependencyItem)

    return 0


def write_developer_properties(game, channel, targetFilePath):
    if os.path.exists(targetFilePath):
        file_utils.del_file_folder(targetFilePath)

    proStr = ""
    if channel['params'] != None and len(channel['params']) > 0:
        for param in channel['params']:
            print("write param:")
            print(param)
            if param['bWriteInClient'] == '1' and param['name'] != None:
                proStr = proStr + param['name'] + "=" + param['value'] + "\n"

    if "sdkLogicVersionCode" in channel and channel['sdkLogicVersionCode'] != None and len(
            str(channel['sdkLogicVersionCode'])) > 0:
        proStr = proStr + "U8_SDK_VERSION_CODE=" + channel["sdkLogicVersionCode"] + "\n"

    proStr = proStr + "U8_Channel=" + str(channel['channelID']) + "\n"
    proStr = proStr + "U8_APPID=" + str(game["appID"]) + "\n"
    proStr = proStr + "U8_APPKEY=" + game["appKey"] + "\n"

    if "payPrivateKey" in game:
        proStr = proStr + "U8_PAY_PRIVATEKEY=" + game["payPrivateKey"] + "\n"

    showSplash = "false"
    if "splash" in channel and channel['splash'] != None and len(channel['splash']) > 0 and int(channel["splash"]) > 0:
        showSplash = "true"

    proStr = proStr + "U8_SDK_SHOW_SPLASH=" + showSplash + "\n"

    authUrl = None
    orderUrl = None
    analyticsUrl = None
    u8serverUrl = None
    u8analytics = "true"
    singleGame = "false"

    if "singleGame" in game and game["singleGame"] == 1:
        singleGame = "true"

    if "serverBaseUrl" in channel and channel["serverBaseUrl"] != None and len(channel["serverBaseUrl"].strip()) > 0:
        u8serverUrl = channel["serverBaseUrl"]
    elif "serverBaseUrl" in game and game["serverBaseUrl"] != None and len(game["serverBaseUrl"].strip()) > 0:
        u8serverUrl = game["serverBaseUrl"]

    # append u8 local config
    local_config = get_local_config()

    if u8serverUrl is None and "u8server_url" in local_config:
        u8serverUrl = local_config['u8server_url']

    if u8serverUrl is not None:
        proStr = proStr + "U8SERVER_URL=" + u8serverUrl + "\n"

    if u8analytics is not None:
        proStr = proStr + "U8_ANALYTICS=" + u8analytics + "\n"

    if singleGame is not None:
        proStr = proStr + "U8_SINGLE_GAME=" + singleGame + "\n"

    # write third plugin info:
    plugins = channel.get('third-plugins')
    if plugins != None and len(plugins) > 0:

        for plugin in plugins:
            if 'params' in plugin and plugin['params'] != None and len(plugin['params']) > 0:
                for param in plugin['params']:
                    if param['bWriteInClient'] == '1':
                        proStr = proStr + param['name'] + "=" + param['value'] + "\n"

    log_utils.debug("the develop info is %s", proStr)
    targetFile = open(targetFilePath, 'wb')
    proStr = proStr.encode('UTF-8')
    targetFile.write(proStr)
    targetFile.close()


def write_plugin_configs(channel, targetFilePath):
    targetTree = None
    targetRoot = None
    pluginNodes = None

    targetTree = ElementTree()
    targetRoot = Element('plugins')
    targetTree._setroot(targetRoot)

    if 'plugins' in channel:
        for plugin in channel['plugins']:
            typeTag = 'plugin'
            typeName = plugin['name']
            typeVal = plugin['type']
            pluginNode = SubElement(targetRoot, typeTag)
            pluginNode.set('name', typeName)
            pluginNode.set('type', typeVal)

    # write third plugin info

    thirdPlugins = channel.get('third-plugins')
    if thirdPlugins != None and len(thirdPlugins) > 0:
        for cPlugin in thirdPlugins:

            if 'plugins' in cPlugin and cPlugin['plugins'] != None and len(cPlugin['plugins']) > 0:
                for plugin in cPlugin['plugins']:
                    typeTag = 'plugin'
                    typeName = plugin['name']
                    typeVal = plugin['type']
                    pluginNode = SubElement(targetRoot, typeTag)
                    pluginNode.set('name', typeName)
                    pluginNode.set('type', typeVal)

    targetTree.write(targetFilePath, 'UTF-8')


if __name__ == "__main__":
    currPath = os.path.dirname(sys.path[0])

    print(currPath)
