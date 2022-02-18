-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               10.6.3-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             11.3.0.6371
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for games_portal
CREATE DATABASE IF NOT EXISTS `games_portal` /*!40100 DEFAULT CHARACTER SET utf8mb3 */;
USE `games_portal`;

-- Dumping structure for table games_portal.active_logins
CREATE TABLE IF NOT EXISTS `active_logins` (
  `Login_id` varchar(38) NOT NULL,
  `Player_id` varchar(38) NOT NULL,
  `Login_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `User_name` varchar(30) NOT NULL,
  UNIQUE KEY `Uniikki` (`Player_id`) USING BTREE,
  UNIQUE KEY `Uniikki2` (`Login_id`),
  UNIQUE KEY `Player_name` (`User_name`) USING BTREE,
  CONSTRAINT `Player_id_FK` FOREIGN KEY (`Player_id`) REFERENCES `users` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table games_portal.active_logins: ~3 rows (approximately)

-- Dumping structure for table games_portal.available_games
CREATE TABLE IF NOT EXISTS `available_games` (
  `Id` int(11) NOT NULL DEFAULT 0,
  `Name_EN` varchar(50) NOT NULL DEFAULT 'TicTacToe',
  `Name_CH` varchar(50) NOT NULL DEFAULT 'TicTacToe',
  `Name_ES` varchar(50) NOT NULL DEFAULT 'TicTacToe',
  PRIMARY KEY (`Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table games_portal.available_games: ~0 rows (approximately)

-- Dumping structure for table games_portal.pending_registrations
CREATE TABLE IF NOT EXISTS `pending_registrations` (
  `Id` varchar(50) NOT NULL,
  `TimesSent` int(11) DEFAULT NULL,
  `Password` varchar(50) NOT NULL,
  `Email` varchar(140) NOT NULL,
  `UserName` varchar(50) DEFAULT NULL,
  `Sent` datetime DEFAULT NULL,
  `Expires` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `users_tult_idx` (`Email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table games_portal.pending_registrations: ~0 rows (approximately)

-- Dumping structure for table games_portal.statistics_games
CREATE TABLE IF NOT EXISTS `statistics_games` (
  `game_type` int(11) NOT NULL COMMENT 'TicTacToe=1, C4=2',
  `playerA_username` varchar(15) NOT NULL,
  `playerB_username` varchar(15) NOT NULL,
  `result` int(11) NOT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `game_id` varchar(38) NOT NULL,
  `playerA_id` varchar(38) DEFAULT NULL,
  `playerB_id` varchar(38) DEFAULT NULL,
  `winner_id` varchar(38) DEFAULT NULL,
  KEY `FK_statistics_users` (`playerA_id`),
  KEY `FK_statistics_users_2` (`playerB_id`),
  KEY `FK_statistics_users_` (`winner_id`),
  CONSTRAINT `FK_statistics_users_` FOREIGN KEY (`winner_id`) REFERENCES `users` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_statistics_users_2` FOREIGN KEY (`playerB_id`) REFERENCES `users` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table games_portal.statistics_games: ~15 rows (approximately)

-- Dumping structure for table games_portal.statistics_game_counts
CREATE TABLE IF NOT EXISTS `statistics_game_counts` (
  `connectfours` bigint(20) unsigned DEFAULT NULL COMMENT 'total finished games',
  `tictactoes` bigint(20) unsigned DEFAULT NULL COMMENT 'total finished games'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table games_portal.statistics_game_counts: ~1 rows (approximately)
INSERT INTO `statistics_game_counts` (`connectfours`, `tictactoes`) VALUES
	(0, 0);

-- Dumping structure for table games_portal.users
CREATE TABLE IF NOT EXISTS `users` (
  `UserName` varchar(15) NOT NULL,
  `Id` varchar(38) NOT NULL,
  `Email` varchar(130) DEFAULT NULL,
  `Secret` varchar(50) CHARACTER SET utf8mb4 NOT NULL,
  `Created` datetime NOT NULL DEFAULT current_timestamp(),
  `Modified` datetime DEFAULT NULL,
  `LastLogin` datetime DEFAULT NULL,
  `Status` enum('ACTIVE','PASSIVE','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
  `Force_password_change` enum('true','false') NOT NULL DEFAULT 'false',
  `tult` varchar(50) NOT NULL,
  `ranking_tictactoe` double NOT NULL DEFAULT 1000,
  `ranking_connect_four` double NOT NULL DEFAULT 1000,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `Uniikki_username` (`UserName`) USING BTREE,
  UNIQUE KEY `Uniikki_email` (`Email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Dumping data for table games_portal.users: ~3 rows (approximately)

-- Dumping structure for trigger games_portal.count_trigger
SET @OLDTMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
DELIMITER //
CREATE TRIGGER `count_trigger` AFTER INSERT ON `statistics_games` FOR EACH ROW BEGIN

	IF (NEW.game_type <20) THEN
 		UPDATE statistics_game_counts SET tictactoes= tictactoes+1;
	ELSE 
		UPDATE statistics_game_counts SET connectfours= connectfours+1;
	END IF;
END//
DELIMITER ;
SET SQL_MODE=@OLDTMP_SQL_MODE;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
