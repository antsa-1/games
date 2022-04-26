/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE IF NOT EXISTS `games_portal` /*!40100 DEFAULT CHARACTER SET utf8mb3 */;
USE `games_portal`;

CREATE TABLE IF NOT EXISTS `game` (
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `game_type` int(11) NOT NULL COMMENT 'TicTacToe=1, C4=2',
  `result` int(11) NOT NULL,
  `playera_username` varchar(15) NOT NULL,
  `playera_start_ranking` double DEFAULT NULL,
  `playera_end_ranking` double NOT NULL,
  `playerb_username` varchar(15) NOT NULL,
  `playerb_start_ranking` double DEFAULT NULL,
  `playerb_end_ranking` double NOT NULL,
  `winner_username` varchar(15) DEFAULT NULL,
  `winner_id` varchar(38) DEFAULT NULL,
  `game_id` varchar(38) NOT NULL,
  `playera_id` varchar(38) DEFAULT NULL,
  `playerb_id` varchar(38) DEFAULT NULL,
  UNIQUE KEY `game_id` (`game_id`),
  KEY `FK_statistics_games_users` (`playera_id`),
  KEY `FK_statistics_games_users_2` (`playerb_id`),
  KEY `FK_statistics_games_users_3` (`winner_id`),
  CONSTRAINT `FK_game_playera_id` FOREIGN KEY (`playera_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_game_playerb_id` FOREIGN KEY (`playerb_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_game_winner_id` FOREIGN KEY (`winner_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;


CREATE TABLE IF NOT EXISTS `game_counter` (
  `connectfours` bigint(20) unsigned DEFAULT 0 COMMENT 'total finished games',
  `tictactoes` bigint(20) unsigned DEFAULT 0 COMMENT 'total finished games',
  `eightballs` bigint(20) unsigned DEFAULT 0 COMMENT 'total finished games'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

INSERT IGNORE INTO `game_counter` (`connectfours`, `tictactoes`, `eightballs`) VALUES
	(0, 0, 0);

CREATE TABLE IF NOT EXISTS `login` (
  `id` varchar(38) NOT NULL,
  `player_id` varchar(38) NOT NULL,
  `login_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `user_name` varchar(30) NOT NULL,
  UNIQUE KEY `Uniikki2` (`id`) USING BTREE,
  UNIQUE KEY `Uniikki` (`player_id`) USING BTREE,
  UNIQUE KEY `Player_name` (`user_name`) USING BTREE,
  CONSTRAINT `Player_id_FK` FOREIGN KEY (`player_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;


CREATE TABLE IF NOT EXISTS `pool_turn` (
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `turn_id` int(11) NOT NULL AUTO_INCREMENT,
  `turn_type` enum('PLAY','HANDBALL','POCKET_SELECTION') DEFAULT NULL,
  `game_id` varchar(38) DEFAULT NULL,
  `player_id` varchar(38) DEFAULT NULL,
  `force` double DEFAULT NULL,
  `angle` double DEFAULT NULL,
  `turn_in` varchar(50) NOT NULL,
  `turn_result` varchar(50) NOT NULL DEFAULT '0',
  `remaining_balls` varchar(37) DEFAULT NULL,
  `cueball_x` double DEFAULT NULL,
  `cueball_y` double DEFAULT NULL,
  `selected_pocket` int(11) DEFAULT NULL,
  `game_mode` int(11) DEFAULT NULL,
  `winner` varchar(38) DEFAULT NULL,
  `winner_id` varchar(38) DEFAULT NULL,
  PRIMARY KEY (`turn_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6868 DEFAULT CHARSET=utf8mb3;


CREATE TABLE IF NOT EXISTS `user` (
  `name` varchar(40) NOT NULL,
  `id` varchar(38) NOT NULL,
  `password` varchar(50) CHARACTER SET utf8mb4 NOT NULL,
  `salt` varchar(50) NOT NULL,
  `created` datetime NOT NULL DEFAULT current_timestamp(),
  `status` enum('ACTIVE','INACTIVE','BLOCKED','BOT') NOT NULL DEFAULT 'ACTIVE',
  `force_password_change` enum('true','false') NOT NULL DEFAULT 'false',
  `ranking_tictactoe` double NOT NULL DEFAULT 1000,
  `ranking_connectfour` double NOT NULL DEFAULT 1000,
  `ranking_eightball` double NOT NULL DEFAULT 1000,
  `profile_text` varchar(250) DEFAULT '',
  `modified` datetime DEFAULT NULL,
  `email` varchar(130) DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `Uniikki_username` (`name`) USING BTREE,
  UNIQUE KEY `Uniikki_email` (`email`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

INSERT IGNORE INTO `user` (`name`, `id`, `password`, `salt`, `created`, `status`, `force_password_change`, `ranking_tictactoe`, `ranking_connectfour`, `ranking_eightball`, `profile_text`, `modified`, `email`, `last_login`) VALUES
	('Olav_computer', '123e4567-e89b-12d3-a456-426652340000', '��XP���q�Y�j��.�', '9d02518fd9d7cfb73bbc9e24e04a4e23', '2022-02-21 12:25:51', 'BOT', 'false', 1200, 1205, 1301.608485499218, 'I am just a bot! I can play any game here!', NULL, NULL, NULL);

SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `count_trigger` AFTER INSERT ON `game` FOR EACH ROW BEGIN

	IF (NEW.game_type <20) THEN
 		UPDATE game_counter SET tictactoes= tictactoes+1;
	ELSEIF  (NEW.game_type <30) THEN
		UPDATE game_counter SET connectfours= connectfours+1;
	ELSEIF  (NEW.game_type <40) THEN
		UPDATE game_counter SET eightballs= eightballs+1;
	END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
