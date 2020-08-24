# -*- coding: utf-8 -*-
# Author:xiaohei
# CreateTime:2016-5-8
#
# All smali file operations are defined here
import os
import os.path
import re
import platform
import subprocess
import inspect
import sys
import codecs
import threading
import time
import log_utils
import file_utils
import shutil


def get_smali_method_count(smaliFile, allMethods):

	if not os.path.exists(smaliFile):
		return 0

	f = open(smaliFile)
	lines = f.readlines()
	f.close()

	classLine = lines[0]
	classLine = classLine.strip()
	if not classLine.startswith(".class"):
		log_utils.error(f + " not startswith .class")
		return 0

	className = parse_class(classLine)
	#log_utils.debug("the class Name is "+className)

	# if className not in allMethods:
	# 	allMethods[className] = list()


	count = 0
	for line in lines:
		line = line.strip()

		method = None
		tempClassName = className
		if line.startswith(".method"):
			method = parse_method_default(className, line)
		elif line.startswith("invoke-"):
			tempClassName, method = parse_method_invoke(line)

		if method is None:
			continue

		#log_utils.debug("the method is "+method)

		if tempClassName not in allMethods:
			allMethods[tempClassName] = list()


		if method not in allMethods[tempClassName]:
			count = count + 1
			allMethods[tempClassName].append(method)
		else:
			pass
			#log_utils.debug(method + " is already exists in allMethods.")

	return count



def parse_class(line):

	if not line.startswith(".class"):
		log_utils.error("line parse error. not startswith .class : "+line)
		return None

	blocks = line.split()
	return blocks[len(blocks)-1]



def parse_method_default(className, line):
	if not line.startswith(".method"):
		log_utils.error("the line parse error in parse_method_default:"+line)
		return None

	blocks = line.split()
	return blocks[len(blocks)-1]


def parse_method_invoke(line):
	if not line.startswith("invoke-"):
		log_utils.error("the line parse error in parse_method_invoke:"+line)

	blocks = line.split("->")
	method = blocks[len(blocks)-1]

	preblocks = blocks[0].split(",")
	className = preblocks[len(preblocks)-1]
	className = className.strip()

	return className,method



























