package com.xiaoniu.fund.web;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.xiaoniu.fund.JWTUtil;
import com.xiaoniu.fund.MD5Util;
import com.xiaoniu.fund.entity.Fund;
import com.xiaoniu.fund.entity.User;
import com.xiaoniu.fund.service.FundService;
import com.xiaoniu.fund.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.xiaoniu.fund.MD5Util.MD5_32;
import static com.xiaoniu.fund.MD5Util.MD5_32_bytes;

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

    @PostMapping("/updateuser")
    public Map<String, Object> updateUser(@RequestParam("token") String token, @RequestParam("email") String email, @RequestParam("name") String name, @RequestParam("phone") String phone, @RequestParam("password") String password) {
        logger.info("try updateUser");
        try {
            User user = jwtUtil.getUserByToken(token);
            if (Objects.equals(password, "")) {
                userService.updateUser(user, email, name, phone);   //不修改密码
            } else {
                userService.updateUserAll(user, email, name, phone, MD5_32(password));
            }
            return Map.of("code", "1", "message", "Success");
        } catch (Exception e) {
            return Map.of("code", "0", "message", e.getMessage());
        }
    }

    // 设置文件保存的路径，可以是绝对路径或相对路径
    private static final String filePath = "/fund/image/";

    // 处理图片上传的方法，参数名 file 要和表单中的 name 属性一致
    @CrossOrigin // 实现跨域请求
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("token") String token, @RequestParam("file") MultipartFile file) {
        logger.info("upload picture");
        try {
            jwtUtil.getUserByToken(token);
            if (file.isEmpty()) {
                return Map.of("code", "0", "message", "请选择文件");
            }
            String fileName = file.getOriginalFilename();
            //String suffixName = fileName.substring(fileName.lastIndexOf("."));  // 获取文件的后缀名
            String newFileName = MD5_32_bytes(file.getBytes());  // 以MD5为文件名，减少重复存储开销
            File dest = new File(filePath);
            dest = new File(dest.getAbsolutePath(), newFileName);
            logger.info(dest.toString());
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            if (!dest.exists()) file.transferTo(dest);
            return Map.of("code", "1", "message", "image/" + newFileName);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", "0", "message", e.getMessage());
        }
    }
}
