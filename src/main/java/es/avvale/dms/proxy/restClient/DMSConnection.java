package es.avvale.dms.proxy.restClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import es.avvale.dms.proxy.beans.HttpDestinationDTO;
import es.avvale.dms.proxy.constants.DMSProxyConstants;
import es.avvale.dms.proxy.utils.DMSProxyRestClient;
import es.avvale.dms.proxy.utils.DMSProxyUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.identity.client.UaaContext;
import org.cloudfoundry.identity.client.UaaContextFactory;
import org.cloudfoundry.identity.client.token.GrantType;
import org.cloudfoundry.identity.client.token.TokenRequest;
import org.cloudfoundry.identity.uaa.oauth.token.CompositeAccessToken;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Log4j2
public class DMSConnection {

    @Autowired
    private DMSProxyRestClient restClient;

    @Autowired
    private DMSProxyUtils utils;


    public String getToken(String urlToken, String clientId, String clientKey) throws Exception {
        if (StringUtils.isEmpty(urlToken) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientKey)) {
            throw new Exception("DMS Credentials not found");
        }
        String json = utils.getToken(urlToken, clientId, clientKey);
        JSONObject obj = new JSONObject(json);
        return obj.getString("access_token");
    }

    public Map<String,Object> connectTOAPISAPCloudDMS(String url, String token,
                                          HttpMethod method, boolean forString) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(null, headers);
        log.info("calling to {}", url);
        ResponseEntity<byte[]> resp;
        ResponseEntity<String> respString;
        String bodyResponse = "";
        Map<String,Object> map = new HashMap<>();
        try {
            if (forString){
                respString = restClient.exchange(url, method, httpEntity, String.class);
                if (respString.getStatusCode().value() < 200 && respString.getStatusCode().value() > 210) {
                    throw new Exception(respString.getBody().toString());
                }
                log.info(respString.getBody());
                map.put("response", respString);
            }else{
                resp = restClient.exchange(url, method, httpEntity, byte[].class);
                if (resp.getStatusCode().value() < 200 && resp.getStatusCode().value() > 210) {
                    throw new Exception(resp.getBody().toString());
                }
                String contentDisposition = resp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
                String fileName = null;
                if (contentDisposition != null) {
                    ContentDisposition cd = ContentDisposition.parse(contentDisposition);
                    if (cd.getFilename() != null) {
                        fileName = cd.getFilename();
                    }
                }
                map.put("file", resp.getBody());
                map.put("filename", fileName);
            }
        } catch (Exception e) {
            log.error("Error calling API: {} , error: {}", url, e.getMessage());
            throw e;
        }
        return map;
    }


    public HttpDestinationDTO getDestinationFromBTP(String destinationName) throws Exception {
        log.info("getDestinationFromBTP || IN");
        JSONObject jsonObj = new JSONObject(System.getenv(DMSProxyConstants.VCAP_SERVICES));

        JSONArray jsonArr = jsonObj.getJSONArray(DMSProxyConstants.instance_destination);
        JSONObject credentials = jsonArr.getJSONObject(0).getJSONObject(DMSProxyConstants.credentials);
        String clientid = credentials.getString(DMSProxyConstants.clientid);
        String clientsecret = credentials.getString(DMSProxyConstants.clientsecret);
        /** XSUAA SERVICE **/
        jsonArr = jsonObj.getJSONArray(DMSProxyConstants.instance_xsuaa);
        JSONObject xsuaaCredentials = jsonArr.getJSONObject(0).getJSONObject(DMSProxyConstants.credentials);
        URI xsuaaUrl = new URI(xsuaaCredentials.getString(DMSProxyConstants.url));

        // Buscamos el token para la obtención de los datos de HTTP Destination
        UaaContextFactory factory =
                UaaContextFactory.factory(xsuaaUrl).authorizePath(DMSProxyConstants.pathAuthorize).tokenPath(DMSProxyConstants.pathToken);
        TokenRequest tokenRequest = factory.tokenRequest();
        tokenRequest.setGrantType(GrantType.CLIENT_CREDENTIALS);
        tokenRequest.setClientId(clientid);
        tokenRequest.setClientSecret(clientsecret);
        UaaContext xsUaaContext = factory.authenticate(tokenRequest);
        CompositeAccessToken jwtToken = new CompositeAccessToken("");
        jwtToken = xsUaaContext.getToken();

        // Obtenemos la url de la instancia del destino
        String uri = credentials.getString(DMSProxyConstants.uri) + DMSProxyConstants.urlDestination + destinationName;
        Map<String, Object> map = connectTOAPISAPCloudDMS(uri,jwtToken.toString(),HttpMethod.GET,true);
        String response = map.get("response").toString();
        response = response.replaceAll("[\\r\\n]+", " ");
        Pattern pattern = Pattern.compile("\\{(.*)\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        String jsonContent;
        if (matcher.find()) {
            jsonContent = "{" + matcher.group(1) + "}";
        }else{
            jsonContent = null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        HttpDestinationDTO destination = new HttpDestinationDTO();
        destination.setName(utils.removeQuotesMarks(jsonNode.get(DMSProxyConstants.NAME)));
        destination.setUrl(utils.removeQuotesMarks(jsonNode.get(DMSProxyConstants.URL)));
        destination.setClientSecret(utils.removeQuotesMarks(jsonNode.get(DMSProxyConstants.CLIENT_SECRET)));
        destination.setClientId(utils.removeQuotesMarks(jsonNode.get(DMSProxyConstants.CLIENT_ID)));
        destination.setRepositoryId(utils.removeQuotesMarks(jsonNode.get(DMSProxyConstants.REPOSITORY_ID)));
        destination.setTokenServiceURL(utils.removeQuotesMarks(jsonNode.get(DMSProxyConstants.TOKEN_SERVICE_URL)));

        return destination;

    }


}
