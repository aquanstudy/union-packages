# -*- coding: utf-8 -*-


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

currProgressPercent = 0

progressFile = ""


def setProgressTempPath(filePath):
    global progressFile
    filePath = os.path.join(os.path.dirname(filePath), "progress_temp")
    if not os.path.exists(filePath):
        os.makedirs(filePath)

    progressFile = os.path.join(filePath, "progress.txt")


def flushProgress():
    global progressFile
    f = open(progressFile, 'w')
    f.write(str(currProgressPercent))
    f.close()


def setProgress(percent):
    global currProgressPercent
    currProgressPercent = percent
    flushProgress()
