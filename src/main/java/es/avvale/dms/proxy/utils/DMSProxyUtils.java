package es.avvale.dms.proxy.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.Charset;
import java.util.Base64;

@Component
@Log4j2
public class DMSProxyUtils {

    @Autowired
    private DMSProxyRestClient restClient;

    public String getToken(String authUrl, String authClientId, String authClientSecret) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        HttpHeaders headers = new HttpHeaders();
        try {
            headers.setBasicAuth(authClientId, authClientSecret);
        } catch (Throwable t) {
            String auth = authClientId + ":" + authClientSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.add("Authorization", authHeader);
        }

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        return restClient.exchange(authUrl + "/oauth/token", httpEntity, HttpMethod.POST);
    }

    public String removeQuotesMarks(Object literal) {
        String literalStr;
        if (literal == null) {
            literalStr = "";
        } else {
            literalStr = literal.toString().replace("\"", "").replaceFirst("\\\\", "");
        }
        return literalStr;
    }
}
