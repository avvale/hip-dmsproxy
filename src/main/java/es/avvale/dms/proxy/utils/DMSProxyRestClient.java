package es.avvale.dms.proxy.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

@Component
@Log4j2
public class DMSProxyRestClient extends RestTemplate {

    private RestTemplate restTemplate;

    private Gson gson;

    private static DMSProxyRestClient restClients;

    public static DMSProxyRestClient RestClientsFactory() {
        if (restClients == null) {
            restClients = new DMSProxyRestClient();
        }
        return restClients;
    }

    private DMSProxyRestClient() {
        this.restTemplate = this.restTemplateFactory();
    }

    @SuppressWarnings("rawtypes")
    private RestTemplate restTemplateFactory() {
        if (this.restTemplate == null) {
            this.restTemplate = new RestTemplate();
            HttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
            HttpMessageConverter stringHttpMessageConverternew = new StringHttpMessageConverter();
            List<HttpMessageConverter<?>> converter = new ArrayList<HttpMessageConverter<?>>();
            converter.add(formHttpMessageConverter);
            converter.add(stringHttpMessageConverternew);
            restTemplate.setMessageConverters(converter);
            gson = new Gson();
        }
        return this.restTemplate;
    }

    @SuppressWarnings("unused")
    public  String exchange(String url, HttpEntity<?> httpEntity, HttpMethod httpMethod) {
        log.info("M:exchange httpEntity {} url {} httpMethod{} ", httpEntity, url, httpMethod);
        return restTemplate.exchange(url, httpMethod, httpEntity, String.class).getBody();
    }
}
