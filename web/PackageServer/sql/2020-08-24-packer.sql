/*
Navicat MySQL Data Transfer

Source Server         : TEST-聚合服务
Source Server Version : 50730
Source Host           : test.nianwan.cn:3306
Source Database       : u8packer

Target Server Type    : MYSQL
Target Server Version : 50730
File Encoding         : 65001

Date: 2020-08-24 22:08:44
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `adminRoleID` int(11) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `adminRoleName` varchar(255) DEFAULT NULL,
  `adminGames` varchar(4096) DEFAULT NULL,
  `creatorID` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES ('1', '1', '8145c6edcabaecdff8bb572f4fdf6616', 'admin', '管理员', '', '1');

-- ----------------------------
-- Table structure for adminrole
-- ----------------------------
DROP TABLE IF EXISTS `adminrole`;
CREATE TABLE `adminrole` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createTime` varchar(255) NOT NULL,
  `creatorID` int(11) DEFAULT NULL,
  `permission` varchar(1024) DEFAULT NULL,
  `roleDesc` varchar(255) DEFAULT NULL,
  `roleName` varchar(255) NOT NULL,
  `topRole` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of adminrole
-- ----------------------------
INSERT INTO `adminrole` VALUES ('1', '2017-05-24 14:54:59', '1', '1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25', '最高权限', '管理员', '1');

-- ----------------------------
-- Table structure for apkmeta
-- ----------------------------
DROP TABLE IF EXISTS `apkmeta`;
CREATE TABLE `apkmeta` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gameID` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `bundleID` varchar(255) DEFAULT NULL,
  `versionCode` varchar(20) DEFAULT NULL,
  `versionName` varchar(20) DEFAULT NULL,
  `uploadTime` varchar(20) DEFAULT NULL,
  `apkPath` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of apkmeta
-- ----------------------------
INSERT INTO `apkmeta` VALUES ('1', '10', 'union', 'com.cn.demo', '1', '1.0', '2020-06-09 13:39:02', 'games/game10/apks/13237561365200.apk');

-- ----------------------------
-- Table structure for channel
-- ----------------------------
DROP TABLE IF EXISTS `channel`;
CREATE TABLE `channel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `channelID` int(11) NOT NULL,
  `gameID` int(11) NOT NULL,
  `name` varchar(64) DEFAULT NULL,
  `channelName` varchar(64) DEFAULT NULL,
  `sdk` varchar(64) DEFAULT NULL,
  `masterSDKName` varchar(64) DEFAULT NULL,
  `bundleID` varchar(255) DEFAULT NULL,
  `splash` varchar(20) DEFAULT NULL,
  `replaceUnitySplash` int(11) DEFAULT NULL,
  `iconType` int(11) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `gameName` varchar(64) DEFAULT NULL,
  `signApk` int(11) DEFAULT '1',
  `signVersion` varchar(10) DEFAULT 'V1',
  `isLocal` int(11) NOT NULL DEFAULT '0',
  `localConfig` varchar(4096) DEFAULT NULL,
  `isConfiged` int(11) NOT NULL DEFAULT '0',
  `keystoreID` int(11) DEFAULT NULL,
  `createTime` varchar(30) DEFAULT NULL,
  `minSdkVersion` int(11) DEFAULT NULL,
  `targetSdkVersion` int(11) DEFAULT NULL,
  `maxSdkVersion` int(11) DEFAULT NULL,
  `autoPermission` int(11) DEFAULT '0',
  `directPermission` int(11) DEFAULT '0',
  `excludePermissionGroups` varchar(255) DEFAULT NULL,
  `autoProtocol` int(11) DEFAULT '0',
  `protocolUrl` varchar(255) DEFAULT NULL,
  `serverBaseUrl` varchar(1024) DEFAULT NULL,
  `sdkLogicVersionCode` varchar(64) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of channel
-- ----------------------------
INSERT INTO `channel` VALUES ('1', '2', '10', 'langlun', '朗轮互娱', 'langlun', null, 'com.cn.demo', '0', null, '1', null, '倾世西游', '1', 'V1', '0', null, '1', '1', null, '19', '26', null, '1', '0', '', '1', 'file:///android_asset/m_userprotocol.html', 'http://192.168.0.11:8080/server', '');
INSERT INTO `channel` VALUES ('2', '3', '10', 'oppo', 'Oppo', 'oppo', null, null, '0', null, '1', null, null, '1', 'V1', '0', null, '0', null, null, null, null, null, '1', '0', null, '1', 'file:///android_asset/m_userprotocol.html', null, null);
INSERT INTO `channel` VALUES ('3', '4', '10', 'mobile', '中国移动', 'mobile', null, 'com.xiaoxiaojh.yd.mm', '0', null, '1', null, '嬉游记', '1', 'V1', '0', null, '1', '1', null, null, null, null, '0', '1', '', '0', 'file:///android_asset/m_userprotocol.html', 'http://192.168.0.11:8080/server', '');

-- ----------------------------
-- Table structure for channelplugin
-- ----------------------------
DROP TABLE IF EXISTS `channelplugin`;
CREATE TABLE `channelplugin` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gameID` int(11) NOT NULL,
  `channelID` int(11) NOT NULL,
  `sdkName` varchar(64) NOT NULL,
  `params` varchar(4096) DEFAULT NULL,
  `state` int(11) NOT NULL DEFAULT '0',
  `extend` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of channelplugin
-- ----------------------------

-- ----------------------------
-- Table structure for game
-- ----------------------------
DROP TABLE IF EXISTS `game`;
CREATE TABLE `game` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `cpID` int(11) DEFAULT '0',
  `appID` int(11) NOT NULL,
  `appKey` varchar(255) NOT NULL,
  `appSecret` varchar(255) NOT NULL,
  `orientation` varchar(20) DEFAULT NULL,
  `cpuSupport` varchar(255) DEFAULT NULL,
  `apkPath` varchar(255) DEFAULT NULL,
  `iconPath` varchar(255) DEFAULT NULL,
  `keystoreID` int(11) DEFAULT NULL,
  `minSdkVersion` int(11) DEFAULT NULL,
  `targetSdkVersion` int(11) DEFAULT NULL,
  `maxSdkVersion` int(11) DEFAULT NULL,
  `versionCode` int(11) DEFAULT NULL,
  `versionName` varchar(20) DEFAULT NULL,
  `outputApkName` varchar(255) DEFAULT NULL,
  `doNotCompress` text,
  `enableLog` int(11) DEFAULT NULL,
  `singleGame` int(11) NOT NULL DEFAULT '0',
  `serverBaseUrl` varchar(255) DEFAULT NULL,
  `createTime` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of game
-- ----------------------------
INSERT INTO `game` VALUES ('1', '缘来是西游', '1', '1', 'b727f8955958450dba3a329c04b6722f', '33714f57b1d24a239d84107706d86d40', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 14:50:06');
INSERT INTO `game` VALUES ('2', '嬉游记', '1', '2', 'ed9a520bc4af4b848c241914bf6e9193', '57d0200c76764650bf59a823f68a89b6', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 14:50:03');
INSERT INTO `game` VALUES ('3', '三国志', '1', '3', '57a302e1e65b4279ac9d92d47bcf2487', '302457bf2ace4de4a81e2497bfb4a28d', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 10:16:27');
INSERT INTO `game` VALUES ('4', '水浒传', '1', '4', '518b8f6a1f8e48f582879f191f53b554', '0e8cf09dfc36433e96d4f277f243a081', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 10:16:27');
INSERT INTO `game` VALUES ('5', '红楼梦', '1', '5', '26c0d27f447f4a46a7578463403a3b29', '8048d6b65f19454ebc4bd3c1a315be9d', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 10:16:27');
INSERT INTO `game` VALUES ('6', '梦幻西游', '1', '6', '275838a0e2e64a158201dc72d4d17544', '1d21d99167894426ab87b00e3254bc57', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 10:16:27');
INSERT INTO `game` VALUES ('7', '王者荣耀', '1', '7', '9f9feabe51b34936af78ef75841f3ee6', '5bf5c3935c1f46479ad3668461ec018c', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 10:16:27');
INSERT INTO `game` VALUES ('8', '使命召唤', '1', '8', '17c15c0d9fdf4cbd86333fe7a5865131', 'e28adce45d7d49b3ad0cfe0d1e5715ee', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 10:16:27');
INSERT INTO `game` VALUES ('9', '速度与激情', '1', '9', '08c38c0fe6a64d1f8008bdd6cd010fba', '4938bd7846754a82a08f339c3daa42ee', 'landscape', null, null, null, '0', null, null, null, null, null, '{channelName}.apk', null, '1', '0', null, '2020-05-25 10:16:27');
INSERT INTO `game` VALUES ('10', '仙灵觉醒', '1', '10', '99b81b3b2de07acd0b6f63cb04b81a8c', '046de9ae9aa93b7bf04787605ac49e95', 'landscape', '', 'games/game10/apks/13237561365200.apk', null, '0', null, null, null, null, '', '{channelName}.apk', '', '1', '0', 'http://localhost:8080/server/', '2020-06-08 21:39:09');

-- ----------------------------
-- Table structure for gameplugin
-- ----------------------------
DROP TABLE IF EXISTS `gameplugin`;
CREATE TABLE `gameplugin` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gameID` int(11) NOT NULL,
  `sdkName` varchar(64) NOT NULL,
  `params` varchar(4096) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of gameplugin
-- ----------------------------

-- ----------------------------
-- Table structure for keystore
-- ----------------------------
DROP TABLE IF EXISTS `keystore`;
CREATE TABLE `keystore` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `filePath` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `aliasName` varchar(255) NOT NULL,
  `aliasPwd` varchar(255) NOT NULL,
  `gameID` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of keystore
-- ----------------------------
INSERT INTO `keystore` VALUES ('1', 'default', 'games/game10/keystore/1591681545654.keystore', '123456', 'demokey.keystore', '123456', '10');

-- ----------------------------
-- Table structure for packlog
-- ----------------------------
DROP TABLE IF EXISTS `packlog`;
CREATE TABLE `packlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `channelID` int(11) NOT NULL,
  `channelLocalID` int(11) NOT NULL DEFAULT '0',
  `gameID` int(11) NOT NULL DEFAULT '0',
  `channelName` varchar(64) DEFAULT NULL,
  `fileName` varchar(255) NOT NULL,
  `state` int(11) DEFAULT NULL,
  `testState` int(11) DEFAULT NULL,
  `channelParams` varchar(4096) DEFAULT NULL,
  `createDate` varchar(30) DEFAULT NULL,
  `testFeed` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of packlog
-- ----------------------------
INSERT INTO `packlog` VALUES ('1', 'mobile', '4', '3', '10', '中国移动', '', '-1', '0', '{\"AppId\":{\"toMetaData\":\"0\",\"value\":\"500000000581\",\"toConfig\":\"0\"},\"AppKey\":{\"toMetaData\":\"0\",\"value\":\"jSWKr1yImSZxbXB5FdpiHQ==\",\"toConfig\":\"0\"},\"AppName\":{\"toMetaData\":\"0\",\"value\":\"嬉游记\",\"toConfig\":\"0\"}}', '2020-06-09 13:40:33', '');
INSERT INTO `packlog` VALUES ('2', 'mobile', '4', '3', '10', '中国移动', '', '-1', '0', '{\"AppId\":{\"toMetaData\":\"0\",\"value\":\"500000000581\",\"toConfig\":\"0\"},\"AppKey\":{\"toMetaData\":\"0\",\"value\":\"jSWKr1yImSZxbXB5FdpiHQ==\",\"toConfig\":\"0\"},\"AppName\":{\"toMetaData\":\"0\",\"value\":\"嬉游记\",\"toConfig\":\"0\"}}', '2020-06-09 13:42:48', '');
INSERT INTO `packlog` VALUES ('3', 'mobile', '4', '3', '10', '中国移动', '', '-1', '0', '{\"AppId\":{\"toMetaData\":\"0\",\"value\":\"500000000581\",\"toConfig\":\"0\"},\"AppKey\":{\"toMetaData\":\"0\",\"value\":\"jSWKr1yImSZxbXB5FdpiHQ==\",\"toConfig\":\"0\"},\"AppName\":{\"toMetaData\":\"0\",\"value\":\"嬉游记\",\"toConfig\":\"0\"}}', '2020-06-09 13:47:44', '');
INSERT INTO `packlog` VALUES ('4', 'mobile', '4', '3', '10', '中国移动', '', '2', '-1', '{\"AppId\":{\"toMetaData\":\"0\",\"value\":\"500000000581\",\"toConfig\":\"0\"},\"AppKey\":{\"toMetaData\":\"0\",\"value\":\"jSWKr1yImSZxbXB5FdpiHQ==\",\"toConfig\":\"0\"},\"AppName\":{\"toMetaData\":\"0\",\"value\":\"嬉游记\",\"toConfig\":\"0\"}}', '2020-06-09 13:52:44', '');
INSERT INTO `packlog` VALUES ('5', 'mobile', '4', '3', '10', '中国移动', '', '-1', '0', '{\"AppId\":{\"toMetaData\":\"0\",\"value\":\"500000000581\",\"toConfig\":\"0\"},\"AppKey\":{\"toMetaData\":\"0\",\"value\":\"jSWKr1yImSZxbXB5FdpiHQ==\",\"toConfig\":\"0\"},\"AppName\":{\"toMetaData\":\"0\",\"value\":\"嬉游记\",\"toConfig\":\"0\"}}', '2020-06-09 14:09:47', '');
INSERT INTO `packlog` VALUES ('6', 'mobile', '4', '3', '10', '中国移动', '', '2', '0', '{\"AppId\":{\"toMetaData\":\"0\",\"value\":\"500000000581\",\"toConfig\":\"0\"},\"AppKey\":{\"toMetaData\":\"0\",\"value\":\"jSWKr1yImSZxbXB5FdpiHQ==\",\"toConfig\":\"0\"},\"AppName\":{\"toMetaData\":\"0\",\"value\":\"嬉游记\",\"toConfig\":\"0\"}}', '2020-06-09 14:19:42', '');
INSERT INTO `packlog` VALUES ('7', 'mobile', '4', '3', '10', '中国移动', '', '2', '0', '{\"AppId\":{\"toMetaData\":\"0\",\"value\":\"500000000581\",\"toConfig\":\"1\"},\"AppKey\":{\"toMetaData\":\"0\",\"value\":\"jSWKr1yImSZxbXB5FdpiHQ==\",\"toConfig\":\"1\"},\"AppName\":{\"toMetaData\":\"0\",\"value\":\"嬉游记\",\"toConfig\":\"1\"}}', '2020-06-09 14:26:46', '');
INSERT INTO `packlog` VALUES ('8', 'langlun', '2', '1', '10', '朗轮互娱', '', '-1', '0', '{\"game_app_id\":{\"toMetaData\":\"1\",\"value\":\"29CD6257930F695CD\",\"toConfig\":\"0\"},\"client_key\":{\"toMetaData\":\"1\",\"value\":\"BBCDQZMWGNNLNLAQ\",\"toConfig\":\"0\"},\"game_name\":{\"toMetaData\":\"1\",\"value\":\"嬉游记(安卓版)\",\"toConfig\":\"0\"},\"game_id\":{\"toMetaData\":\"1\",\"value\":\"3\",\"toConfig\":\"0\"}}', '2020-06-09 14:33:11', '');
INSERT INTO `packlog` VALUES ('9', 'langlun', '2', '1', '10', '朗轮互娱', '', '2', '0', '{\"game_app_id\":{\"toMetaData\":\"1\",\"value\":\"29CD6257930F695CD\",\"toConfig\":\"0\"},\"client_key\":{\"toMetaData\":\"1\",\"value\":\"BBCDQZMWGNNLNLAQ\",\"toConfig\":\"0\"},\"game_name\":{\"toMetaData\":\"1\",\"value\":\"嬉游记(安卓版)\",\"toConfig\":\"0\"},\"game_id\":{\"toMetaData\":\"1\",\"value\":\"3\",\"toConfig\":\"0\"}}', '2020-06-09 14:54:47', '');

-- ----------------------------
-- Table structure for sysmenu
-- ----------------------------
DROP TABLE IF EXISTS `sysmenu`;
CREATE TABLE `sysmenu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createTime` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parentID` int(11) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `iconClass` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sysmenu
-- ----------------------------
INSERT INTO `sysmenu` VALUES ('1', '2018-07-05 11:38:40', '游戏管理', '0', null, 'fa-th-list');
INSERT INTO `sysmenu` VALUES ('2', '2018-03-16 13:50:04', 'SDK管理', '0', null, 'fa-object-group');
INSERT INTO `sysmenu` VALUES ('3', '2018-03-16 13:57:59', '测试结果', '0', null, 'fa-bar-chart');
INSERT INTO `sysmenu` VALUES ('4', '2016-07-29 15:37:02', '权限管理', '0', null, 'fa-object-ungroup');
INSERT INTO `sysmenu` VALUES ('5', '2018-03-16 13:50:39', '游戏列表', '1', 'games.html', 'fa-circle-o');
INSERT INTO `sysmenu` VALUES ('6', '2018-12-10 09:12:35', '渠道SDK管理', '2', 'channels.html', 'fa-circle-o');
INSERT INTO `sysmenu` VALUES ('7', '2018-03-16 13:59:03', '测试列表', '3', 'packtestlist.html', 'fa-circle-o');
INSERT INTO `sysmenu` VALUES ('8', '2016-07-29 15:37:19', '管理员', '4', 'admins.html', 'fa-circle-o');
INSERT INTO `sysmenu` VALUES ('9', '2017-06-10 13:29:16', '管理角色', '4', 'adminRoles.html', 'fa-circle-o');
INSERT INTO `sysmenu` VALUES ('10', '2017-06-10 13:30:53', '权限分配', '4', 'permissions2.html', 'fa-circle-o');
INSERT INTO `sysmenu` VALUES ('11', '2018-05-21 14:27:00', '游戏权限', '4', 'gamesAdmin.html', 'fa-circle-o');
