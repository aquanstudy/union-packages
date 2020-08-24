# -*- coding: utf-8 -*-
#Author:tik
#CreateTime:2018-04-25
#
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
# import sqlite_query

import mysql_query

def create_db():

	# if use mysql to store config of package tool . you can cancel the commented block downside
	
	config = config_utils.get_db_config()
	host = config["mysql.host"] #'the host ip of mysql'
	port = int(config["mysql.port"])	#the port of mysql
	user = config["mysql.user"]	# the user of mysql
	pwd = config["mysql.password"]	# the password of mysql
	db = config["mysql.db"]	# the db name of package tool
	cdb = mysql_query.MySqlDB(host, port, user, pwd, db)


	log_utils.debug("==========db info begin==================")
	log_utils.debug("mysql.host:"+host)
	log_utils.debug("mysql.port:"+str(port))
	log_utils.debug("mysql.user:"+user)
	log_utils.debug("mysql.password:"+pwd)
	log_utils.debug("mysql.db:"+db)
	log_utils.debug("==========db info end==================")


	#if use sqlite to store config of package tool . you can cancel the commented block downside
	#cdb = sqlite_query.SqliteDB(file_utils.getDBPath())		

	return cdb


def get_uni_result(sql):

	log_utils.debug("db_utils:get_uni_result sql:"+sql)

	cdb = create_db()

	if cdb == None:
		return

	return cdb.fetch_one(sql)



def get_result_list(sql):

	log_utils.debug("db_utils:get_result_list sql:"+sql)

	cdb = create_db()

	if cdb == None:
		return

	return cdb.fetch_all(sql)



def get_game_by_id(gameID):

	sql = "select * from `game` where appID=" + str(gameID);

	return get_uni_result(sql)

def get_all_games():

	sql = "select * from `game`"

	return get_result_list(sql)


def get_channel_by_id(uniID):

	sql = "select * from `channel` where id="+str(uniID);

	return get_uni_result(sql)

def get_channels_by_game(gameID):

	sql = "select * from `channel` where gameID="+str(gameID)

	return get_result_list(sql)


def get_keystore_by_id(uniID):

	sql = "select * from `keystore` where id="+str(uniID);

	return get_uni_result(sql)


def get_packlog_by_id(packID):

	sql = "select * from `packlog` where id="+str(packID);
	return get_uni_result(sql)


def get_channel_plugin_params(gameID, channelID):

	sql = "select * from `channelplugin` where channelID="+str(channelID)

	channelPlugins = get_result_list(sql)

	sql = "select * from `gameplugin` where gameID="+str(gameID)

	gamePlugins = get_result_list(sql)


	result = {}

	for cp in channelPlugins:
		if "state" in cp and cp["state"] == 1:
			result[cp["sdkName"]] = cp["params"]

	for gp in gamePlugins:

		found = False
		for cp in channelPlugins:
			if cp["sdkName"] == gp["sdkName"]:
				found = True
				break

		if not found:
			result[gp["sdkName"]] = gp["params"]


	return result



if __name__ == "__main__":

	#getGameByID(1)
	print(get_packlog_by_id(81))

	# sql = "select * from `ugame` where `appID`=1"
	# print(getResultList(sql))

