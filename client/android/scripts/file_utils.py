# -*- coding: utf-8 -*-
# Author:seeme
# CreateTime:2014-10-25
#
# All file operations are defined here
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
import shutil
import zipfile
import log_utils
import config_utils

curDir = os.getcwd()


# extend path to avoid PATH_MAX_LEN(260) limit
def win_expand_path(dos_path, encoding=None):
    if platform.system() != 'Windows':
        return dos_path

    if (not isinstance(dos_path, unicode) and
            encoding is not None):
        dos_path = dos_path.decode(encoding)
    path = os.path.abspath(dos_path)
    if path.startswith(u"\\\\"):
        return u"\\\\?\\UNC\\" + path[2:]
    return u"\\\\?\\" + path


def unzip_file(zipfilename, unziptodir):
    if not os.path.exists(unziptodir):
        os.makedirs(unziptodir)
    zfobj = zipfile.ZipFile(zipfilename)

    for name in zfobj.namelist():

        try:
            name = name.replace('\\', '/')
            ext_filename = formatPath(os.path.join(unziptodir, name))

            if name.endswith('/'):
                os.makedirs(ext_filename)
            else:
                ext_dir = os.path.dirname(ext_filename)
                if not os.path.exists(ext_dir):
                    os.makedirs(ext_dir)

                log_utils.debug("unzip_file:" + ext_filename)
                ext_filename = win_expand_path(ext_filename)
                with open(ext_filename, 'wb') as outfile:
                    outfile.write(zfobj.read(name))

        except Exception as e:
            log_utils.error("unzip_file cause an exception:%s", repr(e))



def list_files(src, resFiles, igoreFiles):
    if os.path.exists(src):

        if os.path.isfile(src) and src not in igoreFiles:
            resFiles.append(src)
        elif os.path.isdir(src):
            for f in os.listdir(src):
                if src not in igoreFiles:
                    list_files(os.path.join(src, f), resFiles, igoreFiles)

    return resFiles


def list_files_with_ext(src, resFiles, targetExt):
    if os.path.exists(src):

        if os.path.isfile(src):

            ext = os.path.splitext(src)[1]
            if ext == targetExt:
                resFiles.append(src)

        elif os.path.isdir(src):
            for f in os.listdir(src):
                list_files_with_ext(os.path.join(src, f), resFiles, targetExt)

    return resFiles


def del_file_folder(src):
    if os.path.exists(src):
        if os.path.isfile(src):
            try:
                src = src.replace('\\', '/')
                os.remove(src)
            except:
                pass

        elif os.path.isdir(src):
            for item in os.listdir(src):
                itemsrc = os.path.join(src, item)
                del_file_folder(itemsrc)

            try:
                os.rmdir(src)
            except:
                pass


def copy_files(src, dest, ignoredExt=None, ignoredFiles=None, overrideable=True):
    if not os.path.exists(src):
        log_utils.warning("the path is not exists.path:%s", src)
        return

    filename = os.path.basename(src)
    if ignoredFiles != None and filename in ignoredFiles:
        return

    if os.path.isfile(src):
        copy_file(src, dest, ignoredExt, ignoredFiles, overrideable)
        return

    for f in os.listdir(src):
        sourcefile = os.path.join(src, f)
        targetfile = os.path.join(dest, f)
        if os.path.isfile(sourcefile):
            copy_file(sourcefile, targetfile, ignoredExt, ignoredFiles, overrideable)
        else:
            copy_files(sourcefile, targetfile, ignoredExt, ignoredFiles, overrideable)


def copy_file(src, dest, ignoredExt=None, ignoredFiles=None, overrideable=True):
    sourcefile = getFullPath(src)
    destfile = getFullPath(dest)
    if not os.path.exists(sourcefile):
        return

    if (not overrideable) and os.path.exists(destfile):
        log_utils.warning("file copy failed. target file already exists. " + destfile)
        return

    fileName = os.path.basename(src)

    if ignoredFiles != None and fileName in ignoredFiles:
        return

    (baseName, ext) = os.path.splitext(fileName)

    if ext != None and ignoredExt != None and ext in ignoredExt:
        return

    destdir = os.path.dirname(destfile)
    if not os.path.exists(destdir):
        os.makedirs(destdir)

    shutil.copy(src, dest)


def modifyFileContent(sourcefile, oldContent, newContent, allReplace=True):
    if os.path.isdir(sourcefile):
        log_utils.warning("the source %s must be a file not a dir", sourcefile)
        return

    if not os.path.exists(sourcefile):
        log_utils.warning("the source is not exists.path:%s", sourcefile)
        return

    f = open(sourcefile, 'r+')
    data = str(f.read())
    f.close()
    bRet = False
    idx = data.find(oldContent)

    if allReplace:

        while idx != -1:
            data = data[:idx] + newContent + data[idx + len(oldContent):]
            idx = data.find(oldContent, idx + len(oldContent))
            bRet = True

    else:
        # just replace first position
        if idx != -1:
            data = data[:idx] + newContent + data[idx + len(oldContent):]
            bRet = True

    if bRet:
        fw = open(sourcefile, 'w')
        fw.write(data)
        fw.close()
        log_utils.info("modify file success.path:%s", sourcefile)
    else:
        log_utils.warning("there is no content matched in file:%s with content:%s", sourcefile, oldContent)


def getCurrDir():
    # global curDir
    curDir = os.getcwd()
    retPath = curDir
    if platform.system() == 'Darwin' or platform.system() == 'Linux':
        retPath = sys.path[0]
        lstPath = os.path.split(retPath)
        if lstPath[1]:
            retPath = lstPath[0]

    return retPath


def getFullPath(filename):
    if os.path.isabs(filename):
        return filename
    currdir = getCurrDir()
    filename = os.path.join(currdir, filename)
    filename = filename.replace('\\', '/')
    filename = re.sub('/+', '/', filename)
    return filename


def formatPath(path):
    filename = path.replace('\\', '/')
    filename = re.sub('/+', '/', filename)
    return filename


def getFileFullPath(retPath):
    # return getFullPath(retPath)

    path = config_utils.get_file_temp_path()
    if path != None and len(path) > 0:
        return os.path.join(path, retPath)

    return getFullPath(retPath)


def getSplashPath():
    return getFullPath("config/splash")


def getJavaDir():
    if platform.system() == 'Windows':
        return getFullPath("tool/win/jre/")
    else:
        return ""


def getJavaBinDir():
    if platform.system() == 'Windows':
        return getFullPath("tool/win/jre/bin/")
    else:
        return ""


def getJavaCMD():
    return getJavaBinDir() + "java"


def getToolPath(filename):
    if platform.system() == 'Windows':
        return "tool/win/" + filename
    elif platform.system() == 'Darwin':
        return "tool/mac/" + filename
    else:
        return "tool/linux/" + filename


def getFullToolPath(filename):
    return getFullPath(getToolPath(filename))


def getDBPath():
    return getFullPath("config/local/db/config.db")


def getFullOutputPath(appName, channel):
    path = getFileFullPath('output/' + appName + '/' + channel)
    # del_file_folder(path)
    if not os.path.exists(path):
        os.makedirs(path)
    return path


def exec_cmd(cmd):
    cmd = cmd.replace('\\', '/')
    cmd = re.sub('/+', '/', cmd)
    ret = True

    log_utils.debug("begin exec_cmd:" + cmd)

    try:
        reload(sys)
        sys.setdefaultencoding('utf-8')

        s = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
        stdoutput, erroutput = s.communicate()

        if platform.system() == 'Windows':
            stdoutput = stdoutput.decode('gbk')
            erroutput = erroutput.decode('gbk')

        retCode = s.returncode

        if retCode:

            log_utils.error("*******ERROR*******")
            log_utils.error(stdoutput)
            log_utils.error(erroutput)
            log_utils.error("*******************")

            cmd = 'error::' + cmd + '  !!!exec failed!!!  '
            ret = False

        else:

            log_utils.info(stdoutput)
            log_utils.info(erroutput)

            cmd = cmd + ' !!!exec success!!! '

        log_utils.info(cmd)

    except Exception as e:
        log_utils.error("execFormatCmd cause an exception:%s", repr(e))
        ret = False
        return

    return ret


def execFormatCmd(cmd):
    cmd = cmd.replace('\\', '/')
    cmd = re.sub('/+', '/', cmd)
    ret = 0

    try:
        reload(sys)
        sys.setdefaultencoding('utf-8')

        # s = subprocess.Popen(cmd, shell=True)
        s = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
        stdoutput, erroutput = s.communicate()

        if platform.system() == 'Windows':
            stdoutput = stdoutput.decode('gbk')
            erroutput = erroutput.decode('gbk')

        ret = s.returncode

        if ret:
            # s = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
            # stdoutput, erroutput = s.communicate()

            log_utils.error("*******ERROR*******")
            log_utils.error(stdoutput)
            log_utils.error(erroutput)
            log_utils.error("*******************")

            cmd = 'error::' + cmd + '  !!!exec Fail!!!  '
        else:

            # s = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
            # stdoutput, erroutput = s.communicate()

            log_utils.info(stdoutput)
            log_utils.info(erroutput)

            cmd = cmd + ' !!!exec success!!! '

        log_utils.info(cmd)

    except Exception as e:
        log_utils.error("execFormatCmd cause an exception:%s", repr(e))
        return

    return ret


def execWinCommand(cmd):
    os.system(cmd)


def execWinCommandInput(tip):
    r = os.popen("set /p s=" + tip)
    txt = r.read()
    r.close()
    return txt


def printLogo():
    u = [
        "$$    $$",
        "$$    $$",
        "$$    $$",
        "$$    $$",
        " $$$$$$ "
    ]

    n8 = [
        " $$$$$$ ",
        "$$    $$",
        " $$$$$$ ",
        "$$    $$",
        " $$$$$$ "
    ]

    s = [
        " $$$$$$ ",
        " $$     ",
        " $$$$$$ ",
        "     $$ ",
        " $$$$$$ "
    ]

    d = [
        "$$$$$$  ",
        "$     $$",
        "$     $$",
        "$     $$",
        "$$$$$$  "
    ]

    k = [
        "$$    $$",
        "$$  $$  ",
        "$$$$    ",
        "$$  $$  ",
        "$$    $$"
    ]

    print("################################################################")
    print(" ")
    for i in range(0, len(u)):
        line = "    " + u[i] + "    " + n8[i] + "    " + s[i] + "    " + d[i] + "    " + k[i]
        print(line)

    print(" ")
    print("################################################################")
