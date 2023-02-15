package com.xiaoniu.fund.service;

import com.xiaoniu.fund.entity.Fund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Component
public class FundService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Fund> fundRowMapper = new BeanPropertyRowMapper<>(Fund.class);

    public Fund getFundById(long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM funds WHERE id = ?", fundRowMapper, id);
    }

    public List<Fund> searchFunds(String key) {
        //return jdbcTemplate.query("SELECT * FROM funds WHERE CHARINDEX(title,?)>0", fundRowMapper, key);
        //return jdbcTemplate.query("SELECT * FROM funds WHERE isPass=1 AND title LIKE '%"+key+"%'", fundRowMapper);  //存在安全风险
        return jdbcTemplate.query("SELECT * FROM funds WHERE isPass=1 AND title LIKE ?", fundRowMapper, "%" + key + "%");
    }


    public Fund newFund(long raiser, String title, String desc, String pic, int total) {
        logger.info("try add fund by {}...", title);
        Fund fund = new Fund();
        fund.setRaiser(raiser);
        fund.setTitle(title);
        fund.setDesc(desc);
        fund.setPic(pic);
        fund.setCreatedAt(System.currentTimeMillis());
        fund.setIsPass(0);  //待审核
        fund.setTotal(total);
        fund.setCurrent(0);
        KeyHolder holder = new GeneratedKeyHolder();
        if (1 != jdbcTemplate.update((conn) -> {
            var ps = conn.prepareStatement("INSERT INTO funds(raiser,title,desc,pic,createdAt,isPass,total,current) VALUES(?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, fund.getRaiser());
            ps.setObject(2, fund.getTitle());
            ps.setObject(3, fund.getDesc());
            ps.setObject(4, fund.getPic());
            ps.setObject(5, fund.getCreatedAt());
            ps.setObject(6, fund.getIsPass());
            ps.setObject(7, fund.getTotal());
            ps.setObject(8, fund.getCurrent());
            return ps;
        }, holder)) {
            throw new RuntimeException("Insert failed.");
        }
        fund.setId(holder.getKey().longValue());
        return fund;
    }

    public void updateFundPay(Fund fund, Integer pay) {
        //二次校验
        int curr = fund.getCurrent() + pay;
        if (curr > fund.getTotal()) throw new RuntimeException("支付后总数超过上限");
        //if (1 != jdbcTemplate.update("UPDATE funds SET current = current + ? WHERE id = ?" , pay, fund.getId())) { //不知为何不能用
        if (1 != jdbcTemplate.update("UPDATE funds SET current = ? WHERE id = ?", curr, fund.getId())) {
            throw new RuntimeException("Fund not found by id");
        }
        logger.info("pay id: " + fund.getId() + ", num: " + pay);
    }

    public void updatePass(Fund fund) {
        if (1 != jdbcTemplate.update("UPDATE funds SET isPass = ? WHERE id = ?", fund.getIsPass(), fund.getId())) {
            throw new RuntimeException("Fund not found by id");
        }
        logger.info("pass id: " + fund.getId());
    }

    public List<Fund> getFunds(int page) {
        int limit = 15; //分页查询，每次请求返回多少条数据
        //return jdbcTemplate.query("SELECT * FROM funds", fundRowMapper);
        logger.info("fetch page: " + page);
        return jdbcTemplate.query("SELECT * FROM funds WHERE isPass = 1 ORDER BY createdAt DESC LIMIT ? OFFSET ?", fundRowMapper,limit,(page-1)*limit);
    }

    public List<Fund> getOneFunds(Long id) {
        return jdbcTemplate.query("SELECT * FROM funds WHERE raiser = ?", fundRowMapper, id);
    }

    public List<Fund> getFundsToCheck() {
        //return jdbcTemplate.query("SELECT * FROM funds", fundRowMapper);
        return jdbcTemplate.query("SELECT * FROM funds WHERE isPass = 0", fundRowMapper);
    }

    public void delFundById(Long id) {
        logger.info("delFund id: " + id);
        jdbcTemplate.update("DELETE FROM funds WHERE id = ?", id);
    }
}
