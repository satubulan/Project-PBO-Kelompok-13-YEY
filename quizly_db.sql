-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Dec 03, 2025 at 03:07 AM
-- Server version: 8.0.30
-- PHP Version: 8.4.7

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `quizly_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `leaderboard`
--

CREATE TABLE `leaderboard` (
  `id_leaderboard` int NOT NULL,
  `id_quiz` int NOT NULL,
  `id_student` int NOT NULL,
  `score` int NOT NULL,
  `rank` int NOT NULL,
  `completed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `leaderboard`
--

INSERT INTO `leaderboard` (`id_leaderboard`, `id_quiz`, `id_student`, `score`, `rank`, `completed_at`) VALUES
(4, 3, 4, 0, 0, '2025-12-03 01:08:39'),
(6, 3, 1, 0, 0, '2025-12-03 02:55:43'),
(8, 2, 1, 20, 0, '2025-12-03 02:59:56'),
(9, 2, 4, 10, 0, '2025-12-03 03:05:41');

-- --------------------------------------------------------

--
-- Table structure for table `question`
--

CREATE TABLE `question` (
  `id_question` int NOT NULL,
  `id_quiz` int NOT NULL,
  `question_text` text NOT NULL,
  `option_a` varchar(255) NOT NULL,
  `option_b` varchar(255) NOT NULL,
  `option_c` varchar(255) NOT NULL,
  `option_d` varchar(255) NOT NULL,
  `correct_answer` char(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `question`
--

INSERT INTO `question` (`id_question`, `id_quiz`, `question_text`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`) VALUES
(7, 2, '2+990', '992', '4', '5', '6', 'A'),
(8, 3, 'blabla', 'r', 't', 'y', 'u', 'B'),
(9, 2, '7 * 7', '88', '11', '49', '21', 'C');

-- --------------------------------------------------------

--
-- Table structure for table `quiz`
--

CREATE TABLE `quiz` (
  `id_quiz` int NOT NULL,
  `title` varchar(100) NOT NULL,
  `quiz_key` varchar(10) NOT NULL,
  `time_limit` int NOT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `id_teacher` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `quiz`
--

INSERT INTO `quiz` (`id_quiz`, `title`, `quiz_key`, `time_limit`, `status`, `id_teacher`) VALUES
(2, 'Matematika', 'RP9P1J', 60, 'ACTIVE', 2),
(3, 'IPA', 'BSMGCJ', 40, 'ACTIVE', 2);

-- --------------------------------------------------------

--
-- Table structure for table `quiz_session`
--

CREATE TABLE `quiz_session` (
  `id_session` int NOT NULL,
  `id_student` int NOT NULL,
  `id_quiz` int NOT NULL,
  `start_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` timestamp NULL DEFAULT NULL,
  `status` enum('ONGOING','COMPLETED') DEFAULT 'ONGOING'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `quiz_session`
--

INSERT INTO `quiz_session` (`id_session`, `id_student`, `id_quiz`, `start_time`, `end_time`, `status`) VALUES
(5, 1, 2, '2025-12-02 14:30:14', '2025-12-02 14:30:21', 'COMPLETED'),
(6, 1, 2, '2025-12-02 14:34:21', '2025-12-02 14:34:25', 'COMPLETED'),
(7, 1, 3, '2025-12-02 14:39:13', '2025-12-02 14:39:19', 'COMPLETED'),
(8, 1, 2, '2025-12-02 14:56:07', '2025-12-02 14:56:12', 'COMPLETED'),
(9, 1, 2, '2025-12-02 15:00:16', '2025-12-02 15:00:19', 'COMPLETED'),
(10, 1, 2, '2025-12-02 22:05:33', '2025-12-02 22:05:37', 'COMPLETED'),
(11, 4, 3, '2025-12-03 00:20:51', '2025-12-03 00:20:55', 'COMPLETED'),
(12, 1, 2, '2025-12-03 00:47:10', '2025-12-03 00:47:15', 'COMPLETED'),
(13, 4, 3, '2025-12-03 01:08:35', '2025-12-03 01:08:39', 'COMPLETED'),
(14, 1, 3, '2025-12-03 01:09:15', '2025-12-03 01:09:17', 'COMPLETED'),
(15, 1, 3, '2025-12-03 02:55:32', '2025-12-03 02:55:43', 'COMPLETED'),
(16, 1, 2, '2025-12-03 02:55:56', '2025-12-03 02:56:03', 'COMPLETED'),
(17, 1, 2, '2025-12-03 02:59:51', '2025-12-03 02:59:56', 'COMPLETED'),
(18, 4, 2, '2025-12-03 03:05:34', '2025-12-03 03:05:41', 'COMPLETED');

-- --------------------------------------------------------

--
-- Table structure for table `result`
--

CREATE TABLE `result` (
  `id_result` int NOT NULL,
  `id_session` int NOT NULL,
  `total_score` int NOT NULL,
  `completed_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `result`
--

INSERT INTO `result` (`id_result`, `id_session`, `total_score`, `completed_at`) VALUES
(4, 5, 20, '2025-12-02 14:30:21'),
(5, 6, 20, '2025-12-02 14:34:25'),
(6, 7, 0, '2025-12-02 14:39:19'),
(7, 8, 20, '2025-12-02 14:56:12'),
(8, 9, 10, '2025-12-02 15:00:19'),
(9, 10, 10, '2025-12-02 22:05:37'),
(10, 11, 0, '2025-12-03 00:20:55'),
(11, 12, 20, '2025-12-03 00:47:15'),
(12, 13, 0, '2025-12-03 01:08:39'),
(13, 14, 10, '2025-12-03 01:09:17'),
(14, 15, 0, '2025-12-03 02:55:43'),
(15, 16, 10, '2025-12-03 02:56:03'),
(16, 17, 20, '2025-12-03 02:59:56'),
(17, 18, 10, '2025-12-03 03:05:41');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id_user` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('TEACHER','STUDENT') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id_user`, `username`, `password`, `role`) VALUES
(1, 'siswa', 'siswa', 'STUDENT'),
(2, 'guru', 'guru', 'TEACHER'),
(3, 'guru1', 'guru1', 'TEACHER'),
(4, 'siswa2', 'siswa2', 'STUDENT');

-- --------------------------------------------------------

--
-- Table structure for table `user_answer`
--

CREATE TABLE `user_answer` (
  `id_answer` int NOT NULL,
  `id_session` int NOT NULL,
  `id_question` int NOT NULL,
  `answer` char(1) DEFAULT NULL,
  `is_correct` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user_answer`
--

INSERT INTO `user_answer` (`id_answer`, `id_session`, `id_question`, `answer`, `is_correct`) VALUES
(19, 5, 7, 'C', 1),
(21, 6, 7, 'C', 1),
(22, 7, 8, 'C', 0),
(24, 8, 7, 'C', 1),
(26, 9, 7, 'C', 1),
(28, 10, 7, 'A', 0),
(29, 11, 8, 'A', 0),
(30, 12, 7, 'A', 1),
(31, 12, 9, 'C', 1),
(32, 13, 8, 'C', 0),
(33, 14, 8, 'B', 1),
(34, 15, 8, 'D', 0),
(35, 16, 7, 'B', 0),
(36, 16, 9, 'C', 1),
(37, 17, 7, 'A', 1),
(38, 17, 9, 'C', 1),
(39, 18, 7, 'B', 0),
(40, 18, 9, 'C', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `leaderboard`
--
ALTER TABLE `leaderboard`
  ADD PRIMARY KEY (`id_leaderboard`),
  ADD KEY `id_quiz` (`id_quiz`),
  ADD KEY `id_student` (`id_student`);

--
-- Indexes for table `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`id_question`),
  ADD KEY `id_quiz` (`id_quiz`);

--
-- Indexes for table `quiz`
--
ALTER TABLE `quiz`
  ADD PRIMARY KEY (`id_quiz`),
  ADD UNIQUE KEY `quiz_key` (`quiz_key`),
  ADD KEY `id_teacher` (`id_teacher`);

--
-- Indexes for table `quiz_session`
--
ALTER TABLE `quiz_session`
  ADD PRIMARY KEY (`id_session`),
  ADD KEY `id_student` (`id_student`),
  ADD KEY `id_quiz` (`id_quiz`);

--
-- Indexes for table `result`
--
ALTER TABLE `result`
  ADD PRIMARY KEY (`id_result`),
  ADD KEY `id_session` (`id_session`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `user_answer`
--
ALTER TABLE `user_answer`
  ADD PRIMARY KEY (`id_answer`),
  ADD KEY `id_session` (`id_session`),
  ADD KEY `id_question` (`id_question`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `leaderboard`
--
ALTER TABLE `leaderboard`
  MODIFY `id_leaderboard` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `question`
--
ALTER TABLE `question`
  MODIFY `id_question` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `quiz`
--
ALTER TABLE `quiz`
  MODIFY `id_quiz` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `quiz_session`
--
ALTER TABLE `quiz_session`
  MODIFY `id_session` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `result`
--
ALTER TABLE `result`
  MODIFY `id_result` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id_user` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `user_answer`
--
ALTER TABLE `user_answer`
  MODIFY `id_answer` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=41;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `leaderboard`
--
ALTER TABLE `leaderboard`
  ADD CONSTRAINT `leaderboard_ibfk_1` FOREIGN KEY (`id_quiz`) REFERENCES `quiz` (`id_quiz`) ON DELETE CASCADE,
  ADD CONSTRAINT `leaderboard_ibfk_2` FOREIGN KEY (`id_student`) REFERENCES `users` (`id_user`) ON DELETE CASCADE;

--
-- Constraints for table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `question_ibfk_1` FOREIGN KEY (`id_quiz`) REFERENCES `quiz` (`id_quiz`) ON DELETE CASCADE;

--
-- Constraints for table `quiz`
--
ALTER TABLE `quiz`
  ADD CONSTRAINT `quiz_ibfk_1` FOREIGN KEY (`id_teacher`) REFERENCES `users` (`id_user`) ON DELETE CASCADE;

--
-- Constraints for table `quiz_session`
--
ALTER TABLE `quiz_session`
  ADD CONSTRAINT `quiz_session_ibfk_1` FOREIGN KEY (`id_student`) REFERENCES `users` (`id_user`) ON DELETE CASCADE,
  ADD CONSTRAINT `quiz_session_ibfk_2` FOREIGN KEY (`id_quiz`) REFERENCES `quiz` (`id_quiz`) ON DELETE CASCADE;

--
-- Constraints for table `result`
--
ALTER TABLE `result`
  ADD CONSTRAINT `result_ibfk_1` FOREIGN KEY (`id_session`) REFERENCES `quiz_session` (`id_session`) ON DELETE CASCADE;

--
-- Constraints for table `user_answer`
--
ALTER TABLE `user_answer`
  ADD CONSTRAINT `user_answer_ibfk_1` FOREIGN KEY (`id_session`) REFERENCES `quiz_session` (`id_session`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_answer_ibfk_2` FOREIGN KEY (`id_question`) REFERENCES `question` (`id_question`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
