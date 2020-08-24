#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import argparse
import stat
import json
import os
import os.path

print('hahahahah')
for i in range(1,100000):
	print 'ghahahah' + str(i)
	sys.stdout.flush()
#sys.stdout.flush()

# if __name__ == '__main__':

# 	# app = web.application(urls, globals())
# 	# app.run()

# 	products = [
# 		{"id":"201",	"price":"1000",	"name":"1000元宝"},
# 		{"id":"202",	"price":"5000",	"name":"5000元宝"},
# 		{"id":"203",	"price":"10000",	"name":"10000元宝"},
# 		{"id":"204",	"price":"20000",	"name":"20000元宝"},
# 		{"id":"205",	"price":"30000",	"name":"30000元宝"},
# 		{"id":"206",	"price":"50000",	"name":"50000元宝"},
# 		{"id":"207",	"price":"100000",	"name":"100000元宝"},
# 		{"id":"208",	"price":"200000",	"name":"200000元宝"},
# 		{"id":"209",	"price":"300000",	"name":"300000元宝"},
# 		{"id":"210",	"price":"500000",	"name":"500000元宝"},
# 		{"id":"211",	"price":"1000000",	"name":"1000000元宝"},
# 		{"id":"212",	"price":"1000",	"name":"1000元宝"},
# 		{"id":"213",	"price":"5000",	"name":"5000元宝"},
# 		{"id":"214",	"price":"10000",	"name":"10000元宝"},
# 		{"id":"215",	"price":"15000",	"name":"15000元宝"},
# 		{"id":"216",	"price":"2000000",	"name":"2000000元宝"},
# 		{"id":"217",	"price":"3000000",	"name":"3000000元宝"},
# 		{"id":"218",	"price":"5000000",	"name":"5000000元宝"}
# 	]

# 	lines = []
# 	for product in products:

# 		sql = "INSERT INTO `xsdk`.`game_product` (`appId`, `productId`, `productName`, `price`, `appstoreProductId`, `createTime`) VALUES ('14', '"+product["id"]+"', '"+product["name"]+"', '"+product["price"]+"', '', '2020-01-16 01:29:39');"
# 		lines.append(sql + "\n")

# 	f = open('sql.txt', 'w')
# 	f.writelines(lines)
# 	f.close()
