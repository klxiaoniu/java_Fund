package com.xiaoniu.fund.web;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.xiaoniu.fund.JWTUtil;
import com.xiaoniu.fund.entity.Fund;
import com.xiaoniu.fund.entity.User;
import com.xiaoniu.fund.service.FundService;
import com.xiaoniu.fund.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;
    @Autowired
    FundService fundService;
    @Autowired
    JWTUtil jwtUtil;

    @GetMapping("/users")
    public List<User> users() {
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public User user(@PathVariable("id") long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/funds")
    public List<Fund> funds(@RequestParam("page") int page) {
        return fundService.getFunds(page);
    }

    @GetMapping("/onefunds")
    public List<Fund> getOnefunds(@RequestParam("id") Long id) {
        return fundService.getOneFunds(id);
    }

    @GetMapping("/searchfunds")
    public List<Fund> searchFunds(@RequestParam("key") String key) {
        return fundService.searchFunds(key);
    }

    @GetMapping("/fundstocheck")
    public List<Fund> fundsToCheck(@RequestParam("token") String token) {

        logger.info("fundsToCheck");
        try {
            User user = jwtUtil.getUserByToken(token);
            if (user.getisAdmin() == 0) throw new Exception("您不是管理员，无法查看审核列表");
            return fundService.getFundsToCheck();
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/funds/{id}")
    public Fund fund(@PathVariable("id") long id) {
        return fundService.getFundById(id);
    }

    @PostMapping("/delfund")
    public Map<String, Object> delFundById(@RequestParam("token") String token, @RequestParam("id") long id) {
        try {
            User user = jwtUtil.getUserByToken(token);
            if (user.getisAdmin() == 1 || Objects.equals(fundService.getFundById(id).getRaiser(), user.getId()))  //用户校验
                fundService.delFundById(id);
            else throw new Exception("您没有权限执行此操作");
            return Map.of("code", "1", "message", "Success");
        } catch (Exception e) {
            logger.info(e.getMessage());
            return Map.of("code", "0", "message", e.getMessage());
        }
    }

    @PostMapping("/fundaddpay")
    public Map<String, Object> fundAddPay(@RequestParam("token") String token, @RequestParam("id") Long id, @RequestParam("pay") Integer pay) {
        logger.info("try addpay");
        try {
            User user = jwtUtil.getUserByToken(token);
            Fund fund = fundService.getFundById(id);
            fundService.updateFundPay(fund, pay);
            //为用户添加爱心值
            userService.updatePoint(user, user.getPoint() + pay);
            return Map.of("code", "1", "message", "Success");
        } catch (Exception e) {
            logger.info(e.getMessage());
            return Map.of("code", "0", "message", e.getMessage());
        }
    }

    @PostMapping("/newfund")
    public Map<String, Object> newFund(@RequestParam("token") String token, @RequestParam("title") String title, @RequestParam("desc") String desc, @RequestParam("pic") String pic, @RequestParam("total") String total) {
        logger.info("try newfund");
        try {
            User user = jwtUtil.getUserByToken(token);
            fundService.newFund(user.getId(), title, desc, pic, Integer.parseInt(total));
            return Map.of("code", "1", "message", "Success");
        } catch (Exception e) {
            return Map.of("code", "0", "message", e.getMessage());
        }
    }

    @PostMapping("/signin")
    public Map<String, Object> signin(@RequestParam("email") String email, @RequestParam("password") String password) {
        logger.info("try signin");
        try {
            User user = userService.signin(email, password);
            return Map.of("code", "1", "message", jwtUtil.sign(user));
        } catch (Exception e) {
            return Map.of("code", "0", "message", e.getMessage());
        }
    }

    @GetMapping("/loginwithtoken")
    public Map<String, Object> loginWithToken(@RequestParam("token") String token) {
        logger.info("try loginwithtoken");
        try {
            User user = jwtUtil.getUserByToken(token);
            return Map.of("code", "1", "message", user);
        } catch (Exception e) {
            return Map.of("code", "0", "message", e.getMessage());
        }
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("name") String name, @RequestParam("phone") String phone) {
        logger.info("try register");
        try {
            User user = userService.register(email, password, name, phone);
            return Map.of("code", "1", "message", user.toString());
        } catch (Exception e) {
            return Map.of("code", "0", "message", e.getMessage());
        }
    }

  /*  @PostMapping("/hassignin")
    public Map<String, Object> hassignin(HttpSession session) {
        logger.info("try hassignin");
        User user = (User) session.getAttribute(KEY_USER);
        return Map.of("code", user != null ? "1" : "0");
    }

    @PostMapping("/del")
    public Map<String, Object> delFund(HttpSession session, @RequestParam("id") String id) {
        logger.info("try del");
        fundService.getFundById(Long.parseLong(id)).setIsPass(Integer.parseInt(isPass));
        return Map.of("code", 1, "message", "SETPASS_" + id + "_" + isPass);
    }*/

    @PostMapping("/admin/setpass")
    public Map<String, Object> setpass(@RequestParam("token") String token, @RequestParam("id") String id, @RequestParam("isPass") String isPass) {
        logger.info("try setpass");

        try {
            User user = jwtUtil.getUserByToken(token);
            if (user.getisAdmin() == 0) throw new Exception("权限不足，无法执行此操作");      //IMPORTANT：服务端校验
            Fund fund = fundService.getFundById(Long.parseLong(id));
            fund.setIsPass(Integer.parseInt(isPass));
            fundService.updatePass(fund);
            return Map.of("code", "1", "message", "Success");
        } catch (Exception e) {
            return Map.of("code", "0", "message", e.getMessage());
        }

    }


}
