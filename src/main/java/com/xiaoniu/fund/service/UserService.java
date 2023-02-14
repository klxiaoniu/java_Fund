package com.xiaoniu.fund.service;

import java.sql.Statement;
import java.util.List;

import com.xiaoniu.fund.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.xiaoniu.fund.entity.User;

@Component
public class UserService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<User> userRowMapper = new BeanPropertyRowMapper<>(User.class);

    public User getUserById(long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", userRowMapper, id);
    }

    public User getUserByEmail(String email) {
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email = ?", userRowMapper, email);
    }

    public User signin(String email, String password) {
        logger.info("try login by {}...", email);
        User user = getUserByEmail(email);
        if (MD5Util.MD5_32(password).equals(user.getPassword())) {
            return user;
        }
        throw new RuntimeException("用户名或密码错误");
    }

    public User register(String email, String password, String name,String phone) {
        logger.info("try register by {}...", email);
        User user = new User();
        user.setEmail(email);
        user.setPassword(MD5Util.MD5_32(password));   //避免明文密码
        user.setName(name);
        user.setPhone(phone);
        user.setCreatedAt(System.currentTimeMillis());
        user.setisAdmin(0);
        user.setPoint(0);
        KeyHolder holder = new GeneratedKeyHolder();
        if (1 != jdbcTemplate.update((conn) -> {
            var ps = conn.prepareStatement("INSERT INTO users(email, password, name, phone, createdAt, isAdmin, point) VALUES(?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, user.getEmail());
            ps.setObject(2, user.getPassword());
            ps.setObject(3, user.getName());
            ps.setObject(4, user.getPhone());
            ps.setObject(5, user.getCreatedAt());
            ps.setObject(6, user.getisAdmin());
            ps.setObject(7, user.getPoint());
            return ps;
        }, holder)) {
            throw new RuntimeException("Insert failed.");
        }
        user.setId(holder.getKey().longValue());
        return user;
    }

    public void updatePoint(User user, Integer point) {
        if (1 != jdbcTemplate.update("UPDATE users SET point = ? WHERE id = ?", point, user.getId())) {
            throw new RuntimeException("User not found by id");
        }
    }

    public List<User> getUsers() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }
}
