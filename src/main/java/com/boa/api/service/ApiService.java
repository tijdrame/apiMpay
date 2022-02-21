package com.boa.api.service;

import com.boa.api.domain.ParamEndPoint;
import com.boa.api.domain.Tracking;
import com.boa.api.request.LoginRequest;
import com.boa.api.request.SouscriptionRequest;
import com.boa.api.response.Access;
import com.boa.api.response.LoginResponse;
import com.boa.api.response.SouscriptionResponse;
import com.boa.api.service.util.ICodeDescResponse;
import com.boa.api.service.util.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ApiService {

    private final TrackingService trackingService;
    //private final UserService userService;
    private final Utils utils;
    private final ParamEndPointService endPointService;

    //private final ApplicationProperties applicationProperties;
    //private final ParamGeneralService paramGeneralService;

    public LoginResponse loginMpay(LoginRequest sRequest, HttpServletRequest request) {
        log.info("=============In loginMpay [{}]", sRequest);
        LoginResponse genericResp = new LoginResponse();
        Tracking tracking = new Tracking();
        tracking.dateRequest(Instant.now());
        Optional<ParamEndPoint> endPoint = endPointService.findByCodeParam("loginMpay");
        if (!endPoint.isPresent()) {
            genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
            genericResp.setDescription(ICodeDescResponse.SERVICE_ABSENT_DESC);
            genericResp.setDateResponse(Instant.now());
            tracking =
                createTracking(
                    tracking,
                    ICodeDescResponse.ECHEC_CODE,
                    "loginMpay",
                    genericResp.toString(),
                    sRequest.toString(),
                    genericResp.getResponseReference()
                );
            trackingService.save(tracking);
            return genericResp;
        }
        HttpURLConnection conn;
        try {
            String jsonStr = new JSONObject().put("username", sRequest.getUsername()).put("password", sRequest.getUsername()).toString();
            log.info("Requete loginMpay wso2 = [{}]", jsonStr);
            conn = utils.doConnexion(endPoint.get().getEndPoints(), jsonStr, "application/json", null, null, false);
            log.info("resp code envoi loginMpay [{}]", conn.getResponseCode());
            String result = "";
            BufferedReader br = null;
            JSONObject obj = new JSONObject();
            log.info("resp code envoi loginMpay [{}]", (conn != null ? conn.getResponseCode() : ""));
            if (conn != null && conn.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ligne = br.readLine();
                while (ligne != null) {
                    result += ligne;
                    ligne = br.readLine();
                }
                // result = IOUtils.toString(conn.getInputStream(), "UTF-8");
                log.info("loginMpay result ===== [{}]", result);
                // if(result.contains(";")) result = result.replace(";", " ");
                obj = new JSONObject(result);
                if (obj.toString() != null && !obj.isNull("token")) {
                    genericResp.setCode(ICodeDescResponse.SUCCES_CODE);
                    genericResp.setDescription(ICodeDescResponse.SUCCES_DESCRIPTION);
                    genericResp.setDateResponse(Instant.now());
                    genericResp.setToken(obj.getString("token"));
                    genericResp.setExpiration(obj.getString("expiration"));
                    genericResp.setUsername(obj.getString("username"));
                    genericResp.setLastConnection(obj.getString("lastConnection"));
                    genericResp.setUserRole(obj.getString("userRole"));
                    genericResp.setIdMerchant(obj.getString("idMerchant"));
                    genericResp.setMerchantName(obj.getString("merchantName"));
                    Access access = new Access();
                    access.setSuiviPaiements(obj.getJSONObject("access").getInt("SuiviPaiements"));
                    access.setSuiviFacturiers(obj.getJSONObject("access").getInt("SuiviFacturiers"));
                    access.setSuiviFactures(obj.getJSONObject("access").getInt("SuiviFactures"));
                    access.setListeCommercants(obj.getJSONObject("access").getInt("ListeCommercants"));
                    access.setSuiviTransferts(obj.getJSONObject("access").getInt("SuiviTransferts"));
                    access.setListeClients(obj.getJSONObject("access").getInt("ListeClients"));
                    access.setSuiviCashOut(obj.getJSONObject("access").getInt("SuiviCASHOUT"));
                    access.setSuiviCashIn(obj.getJSONObject("access").getInt("SuiviCASHIN"));
                    access.setInscriptionClients(obj.getJSONObject("access").getInt("InscriptionClients"));
                    access.setListeActions(obj.getJSONObject("access").getInt("ListeActions"));

                    tracking =
                        createTracking(
                            tracking,
                            ICodeDescResponse.SUCCES_CODE,
                            request.getRequestURI(),
                            genericResp.toString(),
                            sRequest.toString(),
                            genericResp.getResponseReference()
                        );
                } else {
                    genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
                    genericResp.setDateResponse(Instant.now());
                    genericResp.setDescription(ICodeDescResponse.ECHEC_DESCRIPTION);
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> map = mapper.readValue(obj.toString(), Map.class);
                    genericResp.setEchec(map);
                    tracking =
                        createTracking(
                            tracking,
                            ICodeDescResponse.ECHEC_CODE,
                            request.getRequestURI(),
                            genericResp.toString(),
                            sRequest.toString(),
                            genericResp.getResponseReference()
                        );
                }
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String ligne = br.readLine();
                while (ligne != null) {
                    result += ligne;
                    ligne = br.readLine();
                }
                log.info("resp envoi loginMpay error ===== [{}]", result);
                obj = new JSONObject(result);

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(result, Map.class);

                genericResp.setEchec(map);
                genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
                genericResp.setDateResponse(Instant.now());
                genericResp.setDescription(ICodeDescResponse.ECHEC_DESCRIPTION);
                tracking =
                    createTracking(
                        tracking,
                        ICodeDescResponse.ECHEC_CODE,
                        request.getRequestURI(),
                        genericResp.toString(),
                        sRequest.toString(),
                        genericResp.getResponseReference()
                    );
            }
        } catch (Exception e) {
            log.error("Exception in loginMpay [{}]", e);
            genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
            genericResp.setDateResponse(Instant.now());
            genericResp.setDescription(ICodeDescResponse.ECHEC_DESCRIPTION + e.getMessage());
            tracking =
                createTracking(
                    tracking,
                    ICodeDescResponse.ECHEC_CODE,
                    request.getRequestURI(),
                    genericResp.getDescription(),
                    sRequest.toString(),
                    genericResp.getResponseReference()
                );
        }
        return genericResp;
    }

    public SouscriptionResponse souscription(SouscriptionRequest sRequest, HttpServletRequest request) {
        log.info("=============In loginMpay [{}]", sRequest);
        SouscriptionResponse genericResp = new SouscriptionResponse();
        Tracking tracking = new Tracking();
        tracking.dateRequest(Instant.now());
        Optional<ParamEndPoint> endPoint = endPointService.findByCodeParam("souscription");
        if (!endPoint.isPresent()) {
            genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
            genericResp.setDescription(ICodeDescResponse.SERVICE_ABSENT_DESC);
            genericResp.setDateResponse(Instant.now());
            tracking =
                createTracking(
                    tracking,
                    ICodeDescResponse.ECHEC_CODE,
                    "souscription",
                    genericResp.toString(),
                    sRequest.toString(),
                    genericResp.getResponseReference()
                );
            trackingService.save(tracking);
            return genericResp;
        }
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(sRequest.getUserName());
        loginRequest.setPassword(sRequest.getPassword());
        LoginResponse loginResponse = loginMpay(loginRequest, request);
        if (!loginResponse.getCode().equals(ICodeDescResponse.SUCCES_CODE)) {
            genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
            genericResp.setDescription(loginResponse.getDescription());
            genericResp.setData(loginResponse.getEchec());
            genericResp.setDateResponse(Instant.now());
            tracking =
                createTracking(
                    tracking,
                    ICodeDescResponse.ECHEC_CODE,
                    "souscription",
                    genericResp.toString(),
                    sRequest.toString(),
                    genericResp.getResponseReference()
                );
            trackingService.save(tracking);
            return genericResp;
        }
        HttpURLConnection conn;
        try {
            String jsonStr = new JSONObject()
                .put("msisdn", sRequest.getMsisdn())
                .put("typeIdentite", sRequest.getTypeIdentite())
                .put("numeroIdentite", sRequest.getNumeroIdentite())
                .put("cardType", sRequest.getCardType())
                .put("numCard", sRequest.getNumCard())
                .put("dateValidite", sRequest.getDateValidite())
                .put("nom", sRequest.getNom())
                .put("prenom", sRequest.getPrenom())
                .put("adresse", sRequest.getAdresse())
                .put("pays", sRequest.getPays())
                .put("codeBanque", sRequest.getCodeBanque())
                .put("codeAgence", sRequest.getCodeAgence())
                .put("userName", sRequest.getUserName())
                .put("password", sRequest.getPassword())
                .put("idSession", sRequest.getIdSession())
                .toString();
            log.info("Requete souscription wso2 = [{}]", jsonStr);
            conn = utils.doConnexion(endPoint.get().getEndPoints(), jsonStr, "application/json", null, loginResponse.getToken(), false);
            log.info("resp code envoi souscription [{}]", conn.getResponseCode());
            String result = "";
            BufferedReader br = null;
            JSONObject obj = new JSONObject();
            log.info("resp code envoi souscription [{}]", (conn != null ? conn.getResponseCode() : ""));
            if (conn != null && conn.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ligne = br.readLine();
                while (ligne != null) {
                    result += ligne;
                    ligne = br.readLine();
                }
                // result = IOUtils.toString(conn.getInputStream(), "UTF-8");
                log.info("souscription result ===== [{}]", result);
                // if(result.contains(";")) result = result.replace(";", " ");
                obj = new JSONObject(result);
                if (obj.toString() != null && !obj.isNull("codeRetour") && obj.getInt("codeRetour") == 0) {
                    genericResp.setCode(ICodeDescResponse.SUCCES_CODE);
                    genericResp.setDescription(ICodeDescResponse.SUCCES_DESCRIPTION);
                    genericResp.setDateResponse(Instant.now());
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> map = mapper.readValue(obj.toString(), Map.class);
                    genericResp.setData(map);
                    tracking =
                        createTracking(
                            tracking,
                            ICodeDescResponse.SUCCES_CODE,
                            request.getRequestURI(),
                            genericResp.toString(),
                            sRequest.toString(),
                            genericResp.getResponseReference()
                        );
                } else {
                    genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
                    genericResp.setDateResponse(Instant.now());
                    genericResp.setDescription(ICodeDescResponse.ECHEC_DESCRIPTION);
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> map = mapper.readValue(obj.toString(), Map.class);
                    genericResp.setData(map);
                    tracking =
                        createTracking(
                            tracking,
                            ICodeDescResponse.ECHEC_CODE,
                            request.getRequestURI(),
                            genericResp.toString(),
                            sRequest.toString(),
                            genericResp.getResponseReference()
                        );
                }
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String ligne = br.readLine();
                while (ligne != null) {
                    result += ligne;
                    ligne = br.readLine();
                }
                log.info("resp envoi error ===== [{}]", result);
                obj = new JSONObject(result);

                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(result, Map.class);

                genericResp.setData(map);
                genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
                genericResp.setDateResponse(Instant.now());
                genericResp.setDescription(ICodeDescResponse.ECHEC_DESCRIPTION);
                tracking =
                    createTracking(
                        tracking,
                        ICodeDescResponse.ECHEC_CODE,
                        request.getRequestURI(),
                        genericResp.toString(),
                        sRequest.toString(),
                        genericResp.getResponseReference()
                    );
            }
        } catch (Exception e) {
            log.error("Exception in loginMpay [{}]", e);
            genericResp.setCode(ICodeDescResponse.ECHEC_CODE);
            genericResp.setDateResponse(Instant.now());
            genericResp.setDescription(ICodeDescResponse.ECHEC_DESCRIPTION + e.getMessage());
            tracking =
                createTracking(
                    tracking,
                    ICodeDescResponse.ECHEC_CODE,
                    request.getRequestURI(),
                    genericResp.getDescription(),
                    sRequest.toString(),
                    genericResp.getResponseReference()
                );
        }
        return genericResp;
    }

    public Tracking createTracking(Tracking tracking, String code, String endPoint, String result, String req, String reqId) {
        // Tracking tracking = new Tracking();
        tracking.setRequestId(reqId);
        tracking.setCodeResponse(code);
        tracking.setDateResponse(Instant.now());
        tracking.setEndPoint(endPoint);
        tracking.setLoginActeur("x");
        tracking.setResponseTr(result);
        tracking.setRequestTr(req);
        tracking.setDateRequest(Instant.now());
        return tracking;
    }
}
