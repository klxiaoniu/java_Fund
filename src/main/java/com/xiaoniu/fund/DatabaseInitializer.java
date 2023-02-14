package com.xiaoniu.fund;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS users (" //
                + "id BIGINT IDENTITY NOT NULL PRIMARY KEY, " //
                + "email VARCHAR(255) NOT NULL, " //
                + "password VARCHAR(255) NOT NULL, " //
                + "name VARCHAR(255) NOT NULL, " //
                + "phone VARCHAR(11) DEFAULT NULL, " //
                + "createdAt BIGINT NOT NULL, " //
                + "isAdmin INT NOT NULL, " //
                + "point INT NOT NULL, " //
                + "UNIQUE (email))");
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS funds (" //
                + "id BIGINT IDENTITY NOT NULL PRIMARY KEY, " //
                + "raiser BIGINT NOT NULL, " //
                + "title VARCHAR(255) NOT NULL, " //
                + "desc VARCHAR(255) NOT NULL, " //
                + "pic VARCHAR(255) DEFAULT NULL, " //图片地址
                + "createdAt BIGINT NOT NULL, " //
                + "isPass INT NOT NULL, " //审核是否通过
                + "total INT NOT NULL, " //需要的总数，暂定为整型
                + "current INT NOT NULL)"); //当前总数
    }
}
