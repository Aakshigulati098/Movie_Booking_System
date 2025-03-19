-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: moviebooking
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `movies`
--

DROP TABLE IF EXISTS `movies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movies` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `year` int DEFAULT NULL,
  `genre` varchar(255) DEFAULT NULL,
  `summary` varchar(1000) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `language` varchar(255) DEFAULT NULL,
  `image_urls` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movies`
--

LOCK TABLES `movies` WRITE;
/*!40000 ALTER TABLE `movies` DISABLE KEYS */;
INSERT INTO `movies` VALUES (1,'Inception',2010,'Sci-Fi','A thief who steals secrets through dreams.','A mind-bending thriller directed by Christopher Nolan.','English','https://img1.jpg,https://img2.jpg,https://img3.jpg'),(2,'Interstellar',2014,'Sci-Fi','A team of explorers travel through a wormhole.','Explores space travel and time dilation.','English','https://inter1.jpg,https://inter2.jpg'),(3,'Parasite',2019,'Thriller','A poor family infiltrates a wealthy household.','A social satire about class struggle.','Korean','https://parasite1.jpg,https://parasite2.jpg,https://parasite3.jpg'),(4,'Inception',2010,'Sci-Fi','A thief who steals secrets through dreams.','A mind-bending thriller directed by Christopher Nolan.','English','https://img1.jpg,https://img2.jpg,https://img3.jpg'),(5,'Interstellar',2014,'Sci-Fi','A team of explorers travel through a wormhole.','Explores space travel and time dilation.','English','https://inter1.jpg,https://inter2.jpg'),(6,'Parasite',2019,'Thriller','A poor family infiltrates a wealthy household.','A social satire about class struggle.','Korean','https://parasite1.jpg,https://parasite2.jpg,https://parasite3.jpg');
/*!40000 ALTER TABLE `movies` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-19 20:15:47
