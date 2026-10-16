package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Ad;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.FileStorageService;
import com.gk.study.service.AdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author Administrator
 * @email 123456@qq.com
 */
@RestController
@RequestMapping("/ad")
public class AdController {

    private final static Logger logger = LoggerFactory.getLogger(AdController.class);

    @Autowired
    AdService service;

    @Autowired
    FileStorageService fileStorageService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public APIResponse list(){
        List<Ad> list =  service.getAdList();
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", list);
    }

    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @Transactional
    public APIResponse create(Ad ad) throws IOException {
        String image = saveAd(ad);
        if(!StringUtils.isEmpty(image)) {
            ad.image = image;
        }

        service.createAd(ad);
        return new APIResponse(ResponeCode.SUCCESS, "创建成功");
    }

    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public APIResponse delete(String ids){
        System.out.println("ids===" + ids);
        // 批量删除
        String[] arr = ids.split(",");
        for (String id : arr) {
            service.deleteAd(id);
        }
        return new APIResponse(ResponeCode.SUCCESS, "删除成功");
    }

    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Transactional
    public APIResponse update(Ad ad) throws IOException {
        String image = saveAd(ad);
        if(!StringUtils.isEmpty(image)) {
            ad.image = image;
        }

        service.updateAd(ad);
        return new APIResponse(ResponeCode.SUCCESS, "更新成功");
    }

    public String saveAd(Ad ad) throws IOException {
        MultipartFile file = ad.getImageFile();
        String newFileName = fileStorageService.save("image", file);
        if(!StringUtils.isEmpty(newFileName)) {
            ad.image = newFileName;
        }
        return newFileName;
    }
}
