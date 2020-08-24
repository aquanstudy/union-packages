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


#generate scaled cached splash img
def generate_scaled_cached_splash(targetDir, splashPath, scaleSize):

    if not os.path.exists(splashPath):
        log_utils.error("splash path is not exists:"+splashPath)
        return 1

    if os.path.exists(targetDir):
        file_utils.del_file_folder(targetDir)


    if not os.path.exists(targetDir):
        os.makedirs(targetDir)


    baseName = os.path.basename(splashPath)
    targetSplashPath = os.path.join(targetDir, baseName)

    file_utils.copy_file(splashPath, targetSplashPath)

    rlImg = Image.open(targetSplashPath)

    scaledImg = rlImg.resize(scaleSize, Image.ANTIALIAS)
    scaledImg.save(targetSplashPath, "PNG") 

    return 0


def generate(sdk):

    paths = ["11","12","21","22"]

    for p in paths:

        splashPath = "config/sdk/"+sdk+"/splash/"+p+"/drawable/u8_splash.png"
        splashPath = file_utils.getFullPath(splashPath)

        targetDir = "config/sdk/"+sdk+"/splash/"+p+"/temp_caches"
        targetDir = file_utils.getFileFullPath(targetDir)

        if p == "11" or p == "12":
            generate_scaled_cached_splash(targetDir, splashPath, (200, 120))
        else:
            generate_scaled_cached_splash(targetDir, splashPath, (120, 200))



if __name__ == "__main__":

    currPath = os.path.dirname(sys.path[0])

    os.chdir(currPath)

    reload(sys)
    sys.setdefaultencoding('utf-8')     

    parser = argparse.ArgumentParser(u"生成闪屏缓存图片")
    parser.add_argument('-s', '--sdk', help=u"SDK目录")

    args = parser.parse_args()

    generate(args.sdk)   


