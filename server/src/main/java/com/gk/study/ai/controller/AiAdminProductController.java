package com.gk.study.ai.controller;

import com.gk.study.ai.dto.AiProductCopyRequest;
import com.gk.study.ai.dto.AiProductCopyResponse;
import com.gk.study.ai.dto.AiProductImageRequest;
import com.gk.study.ai.dto.AiProductImageResponse;
import com.gk.study.ai.service.AiProductCopyService;
import com.gk.study.ai.service.AiProductImageService;
import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/admin/product")
public class AiAdminProductController {
    @Autowired
    private AiProductCopyService aiProductCopyService;

    @Autowired
    private AiProductImageService aiProductImageService;

    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public APIResponse generate(@RequestBody AiProductCopyRequest request) {
        AiProductCopyResponse response = aiProductCopyService.generate(request == null ? new AiProductCopyRequest() : request);
        return new APIResponse(ResponeCode.SUCCESS, "AI product copy generated", response);
    }

    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/image/generate", method = RequestMethod.POST)
    public APIResponse generateImage(@RequestBody AiProductImageRequest request) {
        AiProductImageResponse response = aiProductImageService.generate(request == null ? new AiProductImageRequest() : request);
        return new APIResponse(ResponeCode.SUCCESS, "AI product image generated", response);
    }
}
