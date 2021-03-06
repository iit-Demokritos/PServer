DROP DATABASE IF EXISTS PServerDBname;
CREATE DATABASE PServerDBname
CHARACTER SET utf8
DEFAULT CHARACTER SET utf8
COLLATE utf8_general_ci
DEFAULT COLLATE utf8_general_ci;

USE PServerDBname;


--
-- Table structure for table `attributes`
--

DROP TABLE IF EXISTS `attributes`;


CREATE TABLE `attributes` (
  `attr_name` varchar(50) NOT NULL,
  `attr_defvalue` varchar(255) DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`attr_name`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;

--
-- Table structure for table `collaborative_feature_associations`
--

DROP TABLE IF EXISTS `collaborative_feature_associations`;

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
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;

--
-- Table structure for table `collaborative_feature_statistics`
--

DROP TABLE IF EXISTS `collaborative_feature_statistics`;

CREATE TABLE `collaborative_feature_statistics` (
  `profile` varchar(50) NOT NULL,
  `ftr` varchar(50) NOT NULL,
  `type` int(11) NOT NULL,
  `value` float NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`profile`,`ftr`,`type`,`FK_psclient`) USING BTREE,
  KEY `user` (`profile`,`FK_psclient`,`type`),
  KEY `client` (`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;


--
-- Table structure for table `collaborative_profiles`
--

DROP TABLE IF EXISTS `collaborative_profiles`;

CREATE TABLE `collaborative_profiles` (
  `cp_user` varchar(50) NOT NULL,
  `cp_feature` varchar(50) NOT NULL,
  `cp_value` text,
  `cp_numvalue` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`cp_user`,`cp_feature`,`FK_psclient`),
  KEY `user` (`cp_user`,`FK_psclient`),
  KEY `client` (`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `communities`
--

DROP TABLE IF EXISTS `communities`;

CREATE TABLE `communities` (
  `community` varchar(50) NOT NULL,
  `FK_psclient` varchar(255) NOT NULL,
  PRIMARY KEY (`community`,`FK_psclient`)
)  ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `community_associations`
--

DROP TABLE IF EXISTS `community_associations`;

CREATE TABLE `community_associations` (
  `community_src` varchar(50) NOT NULL,
  `community_dst` varchar(50) NOT NULL,
  `weight` float NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `FK_psclient` varchar(255) NOT NULL
)  ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `community_feature_associations`
--

DROP TABLE IF EXISTS `community_feature_associations`;

CREATE TABLE `community_feature_associations` (
  `ftr_src` varchar(50) NOT NULL,
  `ftr_dst` varchar(50) NOT NULL,
  `weight` float NOT NULL,
  `type` int(10) unsigned NOT NULL,
  `community` varchar(50) NOT NULL,
  `FK_psclient` varchar(255) NOT NULL
)  ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `community_profiles`
--

DROP TABLE IF EXISTS `community_profiles`;

CREATE TABLE `community_profiles` (
  `community` varchar(50) NOT NULL,
  `feature` varchar(50) NOT NULL,
  `feature_value` double DEFAULT '0',
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`community`,`feature`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `decay_data`
--

DROP TABLE IF EXISTS `decay_data`;

CREATE TABLE `decay_data` (
  `dd_user` varchar(50) NOT NULL,
  `dd_feature` varchar(50) NOT NULL,
  `dd_timestamp` double unsigned NOT NULL DEFAULT '0',
  `FK_psclient` varchar(50) NOT NULL,
  `FK_session` varchar(45) NOT NULL DEFAULT '',
  PRIMARY KEY (`dd_user`,`dd_feature`,`FK_session`,`dd_timestamp`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `decay_groups`
--

DROP TABLE IF EXISTS `decay_groups`;

CREATE TABLE `decay_groups` (
  `dg_group` varchar(50) NOT NULL,
  `dg_rate` float DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`dg_group`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `ftrgroup_features`
--

DROP TABLE IF EXISTS `ftrgroup_features`;

CREATE TABLE `ftrgroup_features` (
  `feature_group` varchar(50) NOT NULL,
  `feature_name` varchar(50) NOT NULL,
  `value` tinyint(4) DEFAULT '0',
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`feature_group`,`feature_name`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `ftrgroup_users`
--

DROP TABLE IF EXISTS `ftrgroup_users`;

CREATE TABLE `ftrgroup_users` (
  `feature_group` varchar(50) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`feature_group`,`user_name`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;

--
-- Table structure for table `ftrgroups`
--

DROP TABLE IF EXISTS `ftrgroups`;

CREATE TABLE `ftrgroups` (
  `ftrgroup` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`ftrgroup`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `num_data`
--

DROP TABLE IF EXISTS `num_data`;

CREATE TABLE `num_data` (
  `nd_user` varchar(50) NOT NULL,
  `nd_feature` varchar(50) NOT NULL,
  `nd_value` double DEFAULT '0',
  `nd_timestamp` BIGINT unsigned NOT NULL DEFAULT '0',
  `sessionId` int(10) unsigned NOT NULL DEFAULT '0',
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`nd_user`,`nd_feature`,`nd_timestamp`,`FK_psclient`,`sessionId`),
  KEY `nd_pc_idx` (`FK_psclient`),
  KEY `nd_ftr_idx` (`nd_feature`,`FK_psclient`) USING BTREE,
  KEY `nd_ts_idx` (`nd_timestamp`,`FK_psclient`) USING BTREE,
  KEY `nd_sid_idx` (`sessionId`,`FK_psclient`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `pserver_clients`
--

DROP TABLE IF EXISTS `pserver_clients`;

CREATE TABLE `pserver_clients` (
  `name` varchar(50) NOT NULL,
  `password` varchar(64) NOT NULL,
  `salt` varchar(64) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `stereotype_attributes`
--

DROP TABLE IF EXISTS `stereotype_attributes`;

CREATE TABLE `stereotype_attributes` (
  `sp_stereotype` varchar(50) NOT NULL,
  `sp_attribute` varchar(50) NOT NULL,
  `sp_value` varchar(50) DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`sp_stereotype`,`sp_attribute`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `stereotype_feature_associations`
--

DROP TABLE IF EXISTS `stereotype_feature_associations`;

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
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `stereotype_feature_statistics`
--

DROP TABLE IF EXISTS `stereotype_feature_statistics`;

CREATE TABLE `stereotype_feature_statistics` (
  `stereotype` varchar(50) NOT NULL,
  `ftr` varchar(50) NOT NULL,
  `type` int(11) NOT NULL,
  `value` float NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`stereotype`,`ftr`,`type`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;


--
-- Table structure for table `stereotype_profiles`
--

DROP TABLE IF EXISTS `stereotype_profiles`;

CREATE TABLE `stereotype_profiles` (
  `sp_stereotype` varchar(50) NOT NULL,
  `sp_feature` varchar(50) NOT NULL DEFAULT '',
  `sp_value` varchar(50) DEFAULT NULL,
  `sp_numvalue` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`sp_stereotype`,`sp_feature`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `stereotype_users`
--

DROP TABLE IF EXISTS `stereotype_users`;

CREATE TABLE `stereotype_users` (
  `su_user` varchar(50) NOT NULL,
  `su_stereotype` varchar(50) NOT NULL,
  `su_degree` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`su_user`,`su_stereotype`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `stereotypes`
--

DROP TABLE IF EXISTS `stereotypes`;

CREATE TABLE `stereotypes` (
  `st_stereotype` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  `st_rule` text DEFAULT NULL,
  PRIMARY KEY (`st_stereotype`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `up_features`
--

DROP TABLE IF EXISTS `up_features`;

CREATE TABLE `up_features` (
  `uf_feature` varchar(50) NOT NULL DEFAULT '',
  `uf_defvalue` varchar(50) DEFAULT NULL,
  `uf_numdefvalue` double DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`uf_feature`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `user_associations`
--

DROP TABLE IF EXISTS `user_associations`;

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
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `user_attributes`
--

DROP TABLE IF EXISTS `user_attributes`;

CREATE TABLE `user_attributes` (
  `user` varchar(50) NOT NULL,
  `attribute` varchar(50) NOT NULL,
  `attribute_value` varchar(255) DEFAULT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`user`,`attribute`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `user_community`
--

DROP TABLE IF EXISTS `user_community`;

CREATE TABLE `user_community` (
  `user` varchar(50) NOT NULL,
  `community` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`user`,`community`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `user_feature_associations`
--

DROP TABLE IF EXISTS `user_feature_associations`;

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
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `user_feature_statistics`
--

DROP TABLE IF EXISTS `user_feature_statistics`;

CREATE TABLE `user_feature_statistics` (
  `user` varchar(50) NOT NULL,
  `ftr` varchar(50) NOT NULL,
  `type` int(11) NOT NULL,
  `value` float NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`user`,`ftr`,`type`,`FK_psclient`),
  KEY `user` (`user`,`FK_psclient`),
  KEY `userftr` (`user`,`ftr`,`type`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci;


--
-- Table structure for table `user_interests`
--

DROP TABLE IF EXISTS `user_interests`;

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
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;

--
-- Table structure for table `user_profiles`
--

DROP TABLE IF EXISTS `user_profiles`;

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
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `user_sessions`
--

DROP TABLE IF EXISTS `user_sessions`;

CREATE TABLE `user_sessions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `FK_user` varchar(50) NOT NULL DEFAULT '',
  `FK_psclient` varchar(50) NOT NULL,
  PRIMARY KEY (`id`,`FK_user`,`FK_psclient`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARACTER SET = utf8 COLLATE=utf8_general_ci ROW_FORMAT=COMPACT;


--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `user` varchar(50) NOT NULL,
  `FK_psclient` varchar(50) NOT NULL,
  `decay_factor` float NOT NULL,
  PRIMARY KEY (`user`,`FK_psclient`)
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT=COMPACT;




grant all privileges on PServerDBname.* to 'PServerDBUsername'@'localhost' identified by 'PServerDBPassword' with grant option;