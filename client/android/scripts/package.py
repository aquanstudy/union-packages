# -*- coding: utf-8 -*-
# Author:tik
# CreateTime:2018-11-19
#
# package entry 
#
#

import sys
import os
import argparse
import log_utils
import db_utils
import builder
import config_utils
import file_utils


def pack(gameID, channelID, packID, tempFilePath):
    log_utils.debug(
        "开始打包： gameID:" + gameID + ";channelID:" + channelID + ";packID:" + packID + ";temp file path:" + tempFilePath)

    # 设置临时环境变量
    os.environ['JAVA_HOME'] = file_utils.getJavaDir()
    os.environ['CLASSPATH'] = ''

    config_utils.init_config(tempFilePath, gameID)

    game = db_utils.get_game_by_id(gameID)
    if game == None:
        log_utils.error("game not exists in db. gameID:" + gameID)
        return 1

    channel = db_utils.get_channel_by_id(channelID)
    if game == None:
        log_utils.error("channel not exists in db. channel id:" + channelID)
        return 1

    packlog = db_utils.get_packlog_by_id(packID)
    if packlog == None:
        log_utils.error("channel packlog not exists in db. packID:" + packID)
        return 1

    keystore = db_utils.get_keystore_by_id(channel["keystoreID"])
    if channel["signApk"] == 1 and keystore == None:
        log_utils.error("curr config need sign apk but keystore not exists in db. keystore id:" + channel["keystoreID"])
        return 1

    log_utils.info("now to package %s...", channel["channelName"])

    # load channel config.xml
    ret = config_utils.load_channel_config(game, channel, packlog)
    if ret:
        log_utils.error("load channel config failed. " + channel['channelName'])
        return 1

    ret = builder.build(game, channel, packID + ".apk", keystore)

    if ret:
        return 0

    return 1


if __name__ == "__main__":
    currPath = os.path.dirname(sys.path[0])
    os.chdir(currPath)

    reload(sys)
    sys.setdefaultencoding('utf-8')

    parser = argparse.ArgumentParser(u"U8SDK 打包工具")
    parser.add_argument('-g', '--game', help=u"游戏AppID")
    parser.add_argument('-c', '--channelID', help=u"渠道ID")
    parser.add_argument('-i', '--packId', help=u"本次打包唯一ID")
    parser.add_argument('-f', '--filePath', help=u"文件存储目录")

    args = parser.parse_args()

    print("开始打包......")
    sys.stdout.flush()

    pack(args.game, args.channelID, args.packId, args.filePath)

    print("打包结束......")
    sys.stdout.flush()
