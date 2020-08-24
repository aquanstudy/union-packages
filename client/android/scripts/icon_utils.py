# -*- coding: utf-8 -*-
#CreateTime:2018-06-11

import file_utils
import os
import os.path
from PIL import Image
import image_utils
import log_utils
import db_utils
import sys
import argparse


#generate icon with mask at all position
def generate_icon_with_mark(targetDir, iconPath, maskPath):

    if not os.path.exists(iconPath):
        log_utils.error("icon path is not exists:"+iconPath)
        return 1

    if os.path.exists(targetDir):
        file_utils.del_file_folder(targetDir)


    if not os.path.exists(targetDir):
        os.makedirs(targetDir)

    log_utils.debug("begin generate_icon_with_mark:"+iconPath)

    baseName = os.path.basename(iconPath)
    targetIconPath = os.path.join(targetDir, baseName)

    file_utils.copy_file(iconPath, targetIconPath)

    if os.path.exists(maskPath):
        
        rlImg = Image.open(targetIconPath)

        maskTypes = [
            "left-top.png",
            "left-bottom.png",
            "right-top.png",
            "right-bottom.png"
        ]

        for maskType in maskTypes:
            rPath = os.path.join(maskPath, maskType)
            log_utils.debug("handle mask:"+rPath)
            if os.path.exists(rPath):
                markIcon = Image.open(rPath)
                maskedIcon = image_utils.append_icon_mark(rlImg, markIcon, (0, 0)) 
                maskedIcon.save(os.path.join(targetDir, maskType), "PNG")   


    return 0


def generate(gameID, channelID):

    log_utils.debug("begin generate temp icons for game:"+str(gameID))

    game = db_utils.get_game_by_id(gameID)
    if game == None:
        log_utils.error("game not exists in db. gameID:"+gameID)
        return 1

    log_utils.debug("begin generate temp icons for channel:"+str(channelID))
    channel = db_utils.get_channel_by_id(channelID)
    if game == None:
        log_utils.error("channel not exists in db. channel id:"+channelID)
        return 1


    gameIconPath = 'games/game' + str(game['appID']) + '/icon/icon.png'
    gameIconPath = file_utils.getFileFullPath(gameIconPath)
    if not os.path.exists(gameIconPath):
        log_utils.error("the game %s icon is not exists:%s",game['name'], gameIconPath)
        gameIconPath = file_utils.getFullPath("config/local/default_icon.png")
        #return 1 

    targetDir = 'games/game' + str(game['appID']) + '/channels/' + str(channel['id']) + '/temp_icons'
    targetDir = file_utils.getFileFullPath(targetDir)

    maskPath = 'config/sdk/' + channel['sdk'] + '/icon_marks'
    maskPath = file_utils.getFullPath(maskPath)

    return generate_icon_with_mark(targetDir, gameIconPath, maskPath)



if __name__ == "__main__":

    currPath = os.path.dirname(sys.path[0])

    os.chdir(currPath)

    reload(sys)
    sys.setdefaultencoding('utf-8')     

    parser = argparse.ArgumentParser(u"生成带mask的ICON")
    parser.add_argument('-g', '--gameID', help=u"游戏AppID")
    parser.add_argument('-c', '--channelID', help=u"渠道ID")

    args = parser.parse_args()

    log_utils.debug("begin generate temp icons...")

    generate(args.gameID, args.channelID)   


