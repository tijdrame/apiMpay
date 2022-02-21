package com.boa.api.web;

import com.boa.api.request.LoginRequest;
import com.boa.api.request.SouscriptionRequest;
import com.boa.api.response.LoginResponse;
import com.boa.api.response.SouscriptionResponse;
import com.boa.api.service.ApiService;
import com.boa.api.service.util.ICodeDescResponse;
import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
@AllArgsConstructor
public class ApiResource {

    private final ApiService apiService;

    @PostMapping("/loginMpay")
    public ResponseEntity<LoginResponse> loginMpay(@RequestBody LoginRequest sRequest, HttpServletRequest request) {
        log.debug("REST request to loginMpay : [{}]", sRequest);
        LoginResponse response = new LoginResponse();
        if (controleParam(sRequest.getUsername()) || controleParam(sRequest.getPassword())) {
            response.setCode(ICodeDescResponse.PARAM_ABSENT_CODE);
            response.setDateResponse(Instant.now());
            response.setDescription(ICodeDescResponse.PARAM_DESCRIPTION);
            return ResponseEntity.badRequest().header("Authorization", request.getHeader("Authorization")).body(response);
        }
        response = apiService.loginMpay(sRequest, request);

        return ResponseEntity.ok().header("Authorization", request.getHeader("Authorization")).body(response);
    }

    @PostMapping("/souscription")
    public ResponseEntity<SouscriptionResponse> souscription(@RequestBody SouscriptionRequest sRequest, HttpServletRequest request) {
        log.debug("REST request to souscription : [{}]", sRequest);
        SouscriptionResponse response = new SouscriptionResponse();
        if (
            controleParam(sRequest.getMsisdn()) ||
            controleParam(sRequest.getTypeIdentite()) ||
            controleParam(sRequest.getNumeroIdentite()) ||
            controleParam(sRequest.getCardType()) ||
            controleParam(sRequest.getNumCard()) ||
            controleParam(sRequest.getDateValidite()) ||
            controleParam(sRequest.getNom()) ||
            controleParam(sRequest.getDateValidite()) ||
            controleParam(sRequest.getAdresse()) ||
            controleParam(sRequest.getPays()) ||
            controleParam(sRequest.getCodeBanque()) ||
            controleParam(sRequest.getCodeAgence()) ||
            controleParam(sRequest.getUserName()) ||
            controleParam(sRequest.getPassword()) ||
            controleParam(sRequest.getIdSession())
        ) {
            response.setCode(ICodeDescResponse.PARAM_ABSENT_CODE);
            response.setDateResponse(Instant.now());
            response.setDescription(ICodeDescResponse.PARAM_DESCRIPTION);
            return ResponseEntity.badRequest().header("Authorization", request.getHeader("Authorization")).body(response);
        }
        response = apiService.souscription(sRequest, request);

        return ResponseEntity.ok().header("Authorization", request.getHeader("Authorization")).body(response);
    }

    private Boolean controleParam(Object param) {
        Boolean flag = false;
        if (StringUtils.isEmpty(param.toString().trim())) flag = true;
        return flag;
    }
}
