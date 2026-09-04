-- Active: 1785938747882@@172.24.223.134@3306@user_db


create database user_db;
use user_db;
create table `user`(
    `id` BIGINT PRIMARY key comment '用户id',
    `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
    `username` varchar(64) NOT NULL COMMENT '用户名',
    `password` varchar(128) NOT NULL COMMENT '加密密码',
    `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1普通 2管理员',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1正常 2冻结 3未激活',
    `create_time` TIMESTAMP not null default current_timestamp
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


