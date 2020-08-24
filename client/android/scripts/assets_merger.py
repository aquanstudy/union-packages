# -*- coding: utf-8 -*-
#Author:xiaohei
#CreateTime:2018-11-09
#
# merge files in assets
# igore the files which already exist. not cover.
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
import image_utils
import log_utils
import file_utils


def merge_files(src, dest):

    if not os.path.exists(src):
        log_utils.warning("the path is not exists.path:%s", src)
        return

    if os.path.exists(dest) and os.path.isfile(dest):

        log_utils.warning("assets merge igored file:"+src+" already exists in " + dest)
        return        

    if os.path.isfile(src):
        file_utils.copy_file(src, dest)
        return

    for f in os.listdir(src):
        sourcefile = os.path.join(src, f)
        targetfile = os.path.join(dest, f)
        if os.path.isfile(sourcefile):
            file_utils.copy_file(sourcefile, targetfile)
        else:
            merge_files(sourcefile, targetfile)    



def merge(srcPath, targetPath):

    if not os.path.exists(srcPath):

        return

    if not os.path.exists(targetPath):
        os.makedirs(targetPath)


    if os.path.isfile(srcPath):
        log_utils.error("assets path invalid."+srcPath)
        return

    merge_files(srcPath, targetPath)



if __name__ == "__main__":

    src = "assets"

    target = "target_assets"

    merge(src, target)
