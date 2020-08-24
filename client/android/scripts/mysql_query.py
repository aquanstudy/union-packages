# -*- coding: utf-8 -*-
#Author:tik
#CreateTime:2018-07-19
#
# db query use mysql. you should install pymysql first. you can run the command [pip install pymysql] to install pymysql module.
#
import file_utils
import os
import os.path
import config_utils
import os
import os.path
import zipfile
import re
import subprocess
import platform
import codecs
import sys
import shutil
import time
import log_utils
import pymysql



class MySqlDB:

    def __init__(self, host='127.0.0.1', port=3306, user='root',pwd='000000', db='pdb'):
        self.host = host
        self.port = port
        self.user = user
        self.pwd = pwd
        self.db = db
        self.connection = None
        self.cursor = None

    def connect(self):
        try:
            self.connection = pymysql.connect(host=self.host, port=self.port, user=self.user, passwd=self.pwd, database=self.db, charset='utf8')
            self.cursor = self.connection.cursor(pymysql.cursors.DictCursor)
        except Exception as e:
            print(e)
            log_utils.error("connect db failed.")
            return False

        
        return True


    def close(self):
        if self.connection and self.cursor:
            self.cursor.close()
            self.connection.close()


    def fetch_all(self, sql, params=None):

        suc = self.connect()

        if not suc:
            return None

        result = None

        try:
            if self.connection and self.cursor:
                self.cursor.execute(sql, params)
                result = self.cursor.fetchall()
                self.connection.commit()

        except Exception as e:
            print(e)
            log_utils.error("execute sql ["+sql+"] failed")
        finally:
            self.close()

        return result


    def fetch_one(self, sql, params=None):

        suc = self.connect()

        if not suc:
            return None

        result = None

        try:
            if self.connection and self.cursor:
                self.cursor.execute(sql, params)
                result = self.cursor.fetchone()
                self.connection.commit()

        except Exception as e:
            print(e)
            log_utils.error("execute sql ["+sql+"] failed")
        finally:
            self.close()

        return result        
