# -*- coding: utf-8 -*-
#Author:tik
#CreateTime:2018-07-19
#
# db query use sqlite3
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
import sqlite3


class SqliteDB:

    def __init__(self, db='config/local/db/config.db'):
        self.db = db
        self.connection = None
        self.cursor = None


    def connect(self):
        try:
            self.connection = sqlite3.connect(self.db)
            self.connection.row_factory = self.dict_factory
            self.cursor = self.connection.cursor()
        except Exception as e:
            print(e)
            log_utils.error("connect db failed.")
            return False

        return True


    def close(self):
        if self.connection and self.cursor:
            self.cursor.close()
            self.connection.close()


    def fetch_all(self, sql):

        suc = self.connect()

        if not suc:
            return None

        result = None            

        try:
            if self.connection and self.cursor:
                self.cursor.execute(sql)
                result = self.cursor.fetchall()

        except Exception as e:
            print(e)
            log_utils.error("execute sql ["+sql+"] failed")
        finally:
            self.close()

        return result


    def fetch_one(self, sql):

        suc = self.connect()

        if not suc:
            return None

        result = None

        try:
            if self.connection and self.cursor:
                self.cursor.execute(sql)
                result = self.cursor.fetchone()

        except Exception as e:
            print(e)
            log_utils.error("execute sql ["+sql+"] failed")
        finally:
            self.close()

        return result    



    def dict_factory(self,cursor, row):
        d = {}
        for idx, col in enumerate(cursor.description):
            d[col[0]] = row[idx]

        return d

