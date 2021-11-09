CREATE TABLE `issue_follow` (
                                `issue_id` varchar(50) DEFAULT NULL,
                                `follow_id` varchar(50) DEFAULT NULL,
                                UNIQUE KEY `issue_follow_pk` (`issue_id`,`follow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4


