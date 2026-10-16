package com.gk.study.ai.controller;

import com.gk.study.ai.dto.AiGuideRequest;
import com.gk.study.ai.dto.AiGuideResponse;
import com.gk.study.ai.service.AiGuideService;
import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AiGuideController {
    @Autowired
    private AiGuideService aiGuideService;

    @RequestMapping(value = "/guide", method = RequestMethod.POST)
    public APIResponse guide(@RequestBody AiGuideRequest request) {
        if (request == null || StringUtils.isBlank(request.getQuery())) {
            return new APIResponse(ResponeCode.FAIL, "请输入导购需求");
        }
        AiGuideResponse response = aiGuideService.guide(request);
        return new APIResponse(ResponeCode.SUCCESS, "AI 导购推荐成功", response);
    }
}
