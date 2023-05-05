package com.communityforum.controller;

import com.communityforum.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @Author YWH
 * @Description DataController
 * @Date 2023/5/5 10:29
 */
@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    /**
     * 打开统计页面
     *
     * @return
     */
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }


    /**
     * 统计网站UV
     */
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        if (start.after(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);
        model.addAttribute("uvResult", uv);
        return "forward:/data";
    }

    /**
     * 统计活跃用户
     */
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start, @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
        if (start.after(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        model.addAttribute("dauResult", dau);
        return "forward:/data";
    }
}
