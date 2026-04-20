package es.avvale.dms.proxy.facade;

import java.util.Map;

public interface DMSProxyFacade {
    Map<String,Object> downloadPdf(String objectId) throws Exception;
}
