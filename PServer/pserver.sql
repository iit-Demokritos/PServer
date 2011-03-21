-- MySQL dump 10.13  Distrib 5.1.41, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: pserver
-- ------------------------------------------------------
-- Server version	5.1.41-3ubuntu12.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attributes`
--

DROP TABLE IF EXISTS `attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attributes` (
  `attr_name` varchar(50) NOT NULL,
  `attr_defvalue` varchar(255) DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`attr_name`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collaborative_feature_associations`
--

DROP TABLE IF EXISTS `collaborative_feature_associations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collaborative_feature_associations` (
  `ftr_src` varchar(50) NOT NULL,
  `ftr_dst` varchar(50) NOT NULL,
  `weight` float NOT NULL,
  `type` int(11) NOT NULL,
  `profile` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`ftr_src`,`ftr_dst`,`type`,`profile`,`FK_psclient`),
  KEY `src` (`ftr_src`,`FK_psclient`,`profile`,`type`),
  KEY `dst` (`ftr_dst`,`type`,`profile`,`FK_psclient`),
  KEY `client` (`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1
/*!50100 PARTITION BY KEY (FK_psclient,`profile`,`type`) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collaborative_feature_statistics`
--

DROP TABLE IF EXISTS `collaborative_feature_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collaborative_feature_statistics` (
  `profile` varchar(50) NOT NULL,
  `ftr` varchar(50) NOT NULL,
  `type` int(11) NOT NULL,
  `value` float NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`profile`,`ftr`,`type`,`FK_psclient`) USING BTREE,
  KEY `user` (`profile`,`FK_psclient`,`type`),
  KEY `client` (`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1
/*!50100 PARTITION BY KEY (FK_psclient,`profile`,`type`) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collaborative_profiles`
--

DROP TABLE IF EXISTS `collaborative_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collaborative_profiles` (
  `cp_user` varchar(50) NOT NULL,
  `cp_feature` varchar(50) NOT NULL,
  `cp_value` text,
  `cp_numvalue` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`cp_user`,`cp_feature`,`FK_psclient`),
  KEY `user` (`cp_user`,`FK_psclient`),
  KEY `client` (`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `communities`
--

DROP TABLE IF EXISTS `communities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `communities` (
  `community` varchar(50) NOT NULL,
  `FK_psclient` varchar(255) NOT NULL,
  PRIMARY KEY (`community`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `community_associations`
--

DROP TABLE IF EXISTS `community_associations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `community_associations` (
  `community_src` varchar(50) NOT NULL,
  `community_dst` varchar(50) NOT NULL,
  `weight` float NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `FK_psclient` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,`type`) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `community_feature_associations`
--

DROP TABLE IF EXISTS `community_feature_associations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `community_feature_associations` (
  `ftr_src` varchar(50) NOT NULL,
  `ftr_dst` varchar(50) NOT NULL,
  `weight` float NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `community` varchar(50) NOT NULL,
  `FK_psclient` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,`type`,community) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `community_profiles`
--

DROP TABLE IF EXISTS `community_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `community_profiles` (
  `community` varchar(50) NOT NULL,
  `feature` varchar(50) NOT NULL,
  `feature_value` double DEFAULT '0',
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`community`,`feature`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,community) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `decay_data`
--

DROP TABLE IF EXISTS `decay_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `decay_data` (
  `dd_user` varchar(50) NOT NULL,
  `dd_feature` varchar(50) NOT NULL,
  `dd_timestamp` double unsigned NOT NULL DEFAULT '0',
  `FK_psclient` varchar(50) NOT NULL,
  `FK_session` varchar(45) NOT NULL DEFAULT '',
  PRIMARY KEY (`dd_user`,`dd_feature`,`FK_session`,`dd_timestamp`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,dd_user) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `decay_groups`
--

DROP TABLE IF EXISTS `decay_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `decay_groups` (
  `dg_group` varchar(50) NOT NULL,
  `dg_rate` float DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`dg_group`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ftrgroup_features`
--

DROP TABLE IF EXISTS `ftrgroup_features`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ftrgroup_features` (
  `feature_group` varchar(50) NOT NULL,
  `feature_name` varchar(50) NOT NULL,
  `value` tinyint(4) DEFAULT '0',
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`feature_group`,`feature_name`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ftrgroups`
--

DROP TABLE IF EXISTS `ftrgroups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ftrgroups` (
  `ftrgroup` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`ftrgroup`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `num_data`
--

DROP TABLE IF EXISTS `num_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `num_data` (
  `nd_user` varchar(50) NOT NULL,
  `nd_feature` varchar(50) NOT NULL,
  `nd_value` double DEFAULT '0',
  `nd_timestamp` int(10) unsigned NOT NULL DEFAULT '0',
  `sessionId` int(10) unsigned NOT NULL DEFAULT '0',
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`nd_user`,`nd_feature`,`nd_timestamp`,`FK_psclient`,`sessionId`),
  KEY `nd_pc_idx` (`FK_psclient`),
  KEY `nd_ftr_idx` (`nd_feature`,`FK_psclient`) USING BTREE,
  KEY `nd_ts_idx` (`nd_timestamp`,`FK_psclient`) USING BTREE,
  KEY `nd_sid_idx` (`sessionId`,`FK_psclient`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,nd_user) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pserver_clients`
--

DROP TABLE IF EXISTS `pserver_clients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pserver_clients` (
  `name` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stereotype_attributes`
--

DROP TABLE IF EXISTS `stereotype_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stereotype_attributes` (
  `sp_stereotype` varchar(50) NOT NULL,
  `sp_attribute` varchar(50) NOT NULL,
  `sp_value` varchar(50) DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`sp_stereotype`,`sp_attribute`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,sp_stereotype) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stereotype_feature_associations`
--

DROP TABLE IF EXISTS `stereotype_feature_associations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stereotype_feature_associations` (
  `ftr_src` varchar(50) NOT NULL,
  `ftr_dst` varchar(50) NOT NULL,
  `weight` float NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `stereotype` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`ftr_src`,`ftr_dst`,`type`,`stereotype`,`FK_psclient`) USING BTREE,
  KEY `src` (`ftr_src`,`type`,`stereotype`,`FK_psclient`),
  KEY `dst` (`ftr_dst`,`type`,`stereotype`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,`type`,stereotype) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stereotype_feature_statistics`
--

DROP TABLE IF EXISTS `stereotype_feature_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stereotype_feature_statistics` (
  `stereotype` varchar(50) NOT NULL,
  `ftr` varchar(50) NOT NULL,
  `type` int(11) NOT NULL,
  `value` float NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`stereotype`,`ftr`,`type`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1
/*!50100 PARTITION BY KEY (FK_psclient,`type`,stereotype) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stereotype_profiles`
--

DROP TABLE IF EXISTS `stereotype_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stereotype_profiles` (
  `sp_stereotype` varchar(50) NOT NULL,
  `sp_feature` varchar(50) NOT NULL DEFAULT '',
  `sp_value` varchar(50) DEFAULT NULL,
  `sp_numvalue` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`sp_stereotype`,`sp_feature`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,sp_stereotype) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stereotype_users`
--

DROP TABLE IF EXISTS `stereotype_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stereotype_users` (
  `su_user` varchar(50) NOT NULL,
  `su_stereotype` varchar(50) NOT NULL,
  `su_degree` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`su_user`,`su_stereotype`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stereotypes`
--

DROP TABLE IF EXISTS `stereotypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stereotypes` (
  `st_stereotype` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`st_stereotype`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `up_features`
--

DROP TABLE IF EXISTS `up_features`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `up_features` (
  `uf_feature` varchar(50) NOT NULL DEFAULT '',
  `uf_defvalue` varchar(50) DEFAULT NULL,
  `uf_numdefvalue` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`uf_feature`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_associations`
--

DROP TABLE IF EXISTS `user_associations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_associations` (
  `user_src` varchar(50) NOT NULL,
  `user_dst` varchar(50) NOT NULL,
  `weight` float NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`user_src`,`user_dst`,`type`,`FK_psclient`),
  KEY `client` (`FK_psclient`),
  KEY `src_type` (`user_src`,`FK_psclient`,`type`),
  KEY `type` (`type`,`FK_psclient`),
  KEY `dst_type` (`user_dst`,`type`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,`type`) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_attributes`
--

DROP TABLE IF EXISTS `user_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_attributes` (
  `user` varchar(50) NOT NULL,
  `attribute` varchar(50) NOT NULL,
  `attribute_value` varchar(255) DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`user`,`attribute`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_community`
--

DROP TABLE IF EXISTS `user_community`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_community` (
  `user` varchar(50) NOT NULL,
  `community` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`user`,`community`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_feature_associations`
--

DROP TABLE IF EXISTS `user_feature_associations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_feature_associations` (
  `ftr_src` varchar(50) NOT NULL,
  `ftr_dst` varchar(50) NOT NULL,
  `weight` float NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `user` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`ftr_src`,`ftr_dst`,`type`,`user`,`FK_psclient`) USING BTREE,
  KEY `type_idx` (`type`,`FK_psclient`) USING BTREE,
  KEY `usr_idx` (`user`,`FK_psclient`) USING BTREE,
  KEY `src_idx` (`ftr_src`,`FK_psclient`,`type`,`user`) USING BTREE,
  KEY `dst_idx` (`ftr_dst`,`FK_psclient`,`type`,`user`) USING BTREE
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,`type`,`user`) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_feature_statistics`
--

DROP TABLE IF EXISTS `user_feature_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_feature_statistics` (
  `user` varchar(50) NOT NULL,
  `ftr` varchar(50) NOT NULL,
  `type` int(11) NOT NULL,
  `value` float NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`user`,`ftr`,`type`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1
/*!50100 PARTITION BY KEY (FK_psclient,`type`,`user`) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_interests`
--

DROP TABLE IF EXISTS `user_interests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_interests` (
  `up_user` varchar(50) NOT NULL,
  `up_feature` varchar(50) NOT NULL,
  `up_value` text,
  `up_numvalue` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`up_user`,`up_feature`,`FK_psclient`),
  KEY `usr_idx` (`up_user`,`FK_psclient`),
  KEY `ftr_idx` (`up_feature`,`FK_psclient`),
  KEY `usr_ftr_idx` (`up_user`,`up_feature`,`FK_psclient`),
  KEY `clnt_idx` (`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_profiles`
--

DROP TABLE IF EXISTS `user_profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_profiles` (
  `up_user` varchar(50) NOT NULL,
  `up_feature` varchar(50) NOT NULL,
  `up_value` text,
  `up_numvalue` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`up_user`,`up_feature`,`FK_psclient`),
  KEY `user` (`up_user`,`FK_psclient`),
  KEY `user_ftr` (`up_user`,`up_feature`,`FK_psclient`),
  KEY `ftr` (`up_feature`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient,up_user) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_sessions`
--

DROP TABLE IF EXISTS `user_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_sessions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `FK_user` varchar(50) NOT NULL DEFAULT '',
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`id`,`FK_user`,`FK_psclient`)
) ENGINE=MyISAM AUTO_INCREMENT=33 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  `decay_factor` float NOT NULL,
  PRIMARY KEY (`user`,`FK_psclient`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC
/*!50100 PARTITION BY KEY (FK_psclient) */;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-10-19  3:55:39
