package com.xiaoniu.fund.web;

import java.util.HashMap;
import java.util.Map;

import com.xiaoniu.fund.entity.Fund;
import com.xiaoniu.fund.entity.User;
import com.xiaoniu.fund.service.FundService;
import com.xiaoniu.fund.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    public static final String KEY_USER = "__user__";

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    UserService userService;
    @Autowired
    FundService fundService;

    @PostMapping(value = "/rest", consumes = "application/json;charset=utf-8", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String rest(@RequestBody User user) {
        return "{\"restSupport\":true}";
    }

    @GetMapping("/")
    public ModelAndView index(HttpSession session) {
        User user = (User) session.getAttribute(KEY_USER);
        Map<String, Object> model = new HashMap<>();
        if (user != null) {
            model.put("user", model);
        }
        return new ModelAndView("index.html", model);
    }

    @GetMapping("/register")
    public ModelAndView register() {
        return new ModelAndView("register.html");
    }

    @PostMapping("/register")
    public ModelAndView doRegister(@RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("name") String name, @RequestParam("phone") String phone) {
        try {
            User user = userService.register(email, password, name, phone);
            logger.info("user registered: {}", user.getEmail());
        } catch (RuntimeException e) {
            return new ModelAndView("register.html", Map.of("email", email, "error", "Register failed"));
        }
        return new ModelAndView("redirect:/signin");
    }

    @GetMapping("/signin")
    public ModelAndView signin(HttpSession session) {
        User user = (User) session.getAttribute(KEY_USER);
        if (user != null) {
            return new ModelAndView("redirect:/profile");
        }
        return new ModelAndView("signin.html");
    }

    @PostMapping("/signin")
    public ModelAndView doSignin(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session) {
        try {
            User user = userService.signin(email, password);
            session.setAttribute(KEY_USER, user);
        } catch (RuntimeException e) {
            return new ModelAndView("signin.html", Map.of("email", email, "error", "Signin failed"));
        }
        return new ModelAndView("redirect:/profile");
    }

    @GetMapping("/profile")
    public ModelAndView profile(HttpSession session) {
        User user = (User) session.getAttribute(KEY_USER);
        if (user == null) {
            return new ModelAndView("redirect:/signin");
        }
        return new ModelAndView("profile.html", Map.of("user", user));
    }

    @GetMapping("/signout")
    public String signout(HttpSession session) {
        session.removeAttribute(KEY_USER);
        return "redirect:/signin";
    }

    @GetMapping("/newfund")
    public ModelAndView newfund(HttpSession session) {
        User user = (User) session.getAttribute(KEY_USER);
        if (user != null) {
            return new ModelAndView("newfund.html");
        }
        return new ModelAndView("signin.html");
    }

    @PostMapping("/newfund")
    public ModelAndView doNewfund(@RequestParam("title") String title, @RequestParam("desc") String desc, @RequestParam("pic") String pic, @RequestParam("total") String total, HttpSession session) {
        User user = (User) session.getAttribute(KEY_USER);
        if (user != null)
            try {
                Fund fund = fundService.newFund(user.getId(), title, desc, pic, Integer.parseInt(total));
                logger.info("fund added: {}", fund.getTitle());
                return new ModelAndView("newfund.html", Map.of("title", title, "error", "NewFund success"));
                //TODO:此处可实现fundlist,直接android中用api做了
            } catch (RuntimeException e) {
                logger.info(e.getMessage());
                return new ModelAndView("newfund.html", Map.of("title", title, "error", "NewFund failed"));
            }
        else return new ModelAndView("redirect:/signin");
    }
}
