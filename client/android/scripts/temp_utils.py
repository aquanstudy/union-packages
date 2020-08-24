
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

currPackageName = None

def setCurrPackageName(packageName):

	global currPackageName
	currPackageName = packageName

def getCurrPackageName():

	return currPackageName


