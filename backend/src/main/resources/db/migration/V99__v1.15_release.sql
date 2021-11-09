CREATE TABLE `issue_follow` (
                                `issue_id` varchar(50) DEFAULT NULL,
                                `follow_id` varchar(50) DEFAULT NULL,
                                UNIQUE KEY `issue_follow_pk` (`issue_id`,`follow_id`),
                                KEY `issue_follow_follow_id_index` (`follow_id`),
                                KEY `issue_follow_issue_id_index` (`issue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4




