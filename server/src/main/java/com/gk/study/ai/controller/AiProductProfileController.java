package com.gk.study.ai.controller;

import com.gk.study.ai.dto.AiProductProfileGenerateRequest;
import com.gk.study.ai.dto.AiProductProfileView;
import com.gk.study.ai.service.AiProductProfileService;
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
@RequestMapping("/ai/admin/product-profile")
public class AiProductProfileController {
    @Autowired
    private AiProductProfileService aiProductProfileService;

    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public APIResponse detail(Long thingId) {
        AiProductProfileView view = aiProductProfileService.getView(thingId);
        return new APIResponse(ResponeCode.SUCCESS, "AI product profile detail", view);
    }

    @Access(level = AccessLevel.ADMIN)
    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public APIResponse generate(@RequestBody AiProductProfileGenerateRequest request) {
        AiProductProfileView view = aiProductProfileService.generate(
                request == null ? null : request.getThingId(),
                request != null && Boolean.TRUE.equals(request.getForce())
        );
        return new APIResponse(ResponeCode.SUCCESS, "AI product profile generated", view);
    }
}
