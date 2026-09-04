-- Active: 1785938747882@@172.24.223.134@3306@mysql
create database `account_db`;



create table wallet (
    id BIGINT primary key auto_increment,
    user_id BIGINT not null,
    balance bigint not null DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 2-冻结',
    frozen_balance bigint not null default 0 COMMENT '冻结金额',
    version int not null default 0,
    create_time timestamp default current_timestamp,
    update_time timestamp default current_timestamp on update current_timestamp
);

drop Table wallet;

create table wallet_log (
    id BIGINT primary key auto_increment,
    user_id BIGINT not null,
    amount bigint not null,
    `type` tinyint not null comment '1-充值 2-消费 3-提现 4-退款 5-系统调整',
    `status` tinyint not null comment '1-成功 2-失败',
    create_time timestamp default current_timestamp,
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_user_id_type` (`user_id`) 
)

drop Table wallet_log;

insert into wallet (user_id, balance) values (23132250228527104, 10000);


create table freeze_log(
    id BIGINT PRIMARY key AUTO_INCREMENT COMMENT 'id主键',
    user_id BIGINT NOT NULL COMMENT '用户id',
    amount BIGINT NOT NULL COMMENT '冻结金额',
    `status` TINYINT NOT NULL COMMENT '状态：1-尝试扣款 2-已经扣款 3-取消扣款',
    biz_id VARCHAR(64) NOT NULL COMMENT '业务id',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version int not null DEFAULT 0 COMMENT '版本号',
    KEY `idx_user_id` (`user_id`)
) COMMENT='冻结日志表';

use `account_db`;
drop table freeze_log;