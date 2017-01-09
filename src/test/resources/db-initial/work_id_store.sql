-- CREATE TABLE IF NOT EXISTS `work_id_store` (
  --`id` int NOT NULL AUTO_INCREMENT (99,1) PRIMARY KEY,
  --`ip` varchar(32) NOT NULL,
  --`service_work_path` varchar(512) NOT NULL
--);

-- CREATE UNIQUE INDEX  IF NOT EXISTS `IDX_WORK_ID_STORE_1` ON work_id_store (`ip`,`service_work_path`);

CREATE TABLE `work_id_store` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `ip` varchar(32) NOT NULL,
  `service_work_path` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_WORK_ID_STORE_1` (`ip`,`service_work_path`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;