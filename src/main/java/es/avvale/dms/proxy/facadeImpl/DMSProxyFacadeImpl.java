package es.avvale.dms.proxy.facadeImpl;

import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import es.avvale.dms.proxy.beans.HttpDestinationDTO;
import es.avvale.dms.proxy.facade.DMSProxyFacade;
import es.avvale.dms.proxy.restClient.DMSConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DMSProxyFacadeImpl implements DMSProxyFacade {

    @Autowired
    private DMSConnection dmsConnection;

    @Autowired
    private Environment environment;

    @Override
    public Map<String, Object> downloadPdf(String objectId) throws Exception {
        HttpDestinationDTO httpDestination = dmsConnection.
                getDestinationFromBTP(environment.getProperty("destination.dms.name"));
        String token = dmsConnection.getToken(httpDestination.getTokenServiceURL(),
                httpDestination.getClientId(), httpDestination.getClientSecret());
        String host = httpDestination.getUrl();
        String repositoryId = httpDestination.getRepositoryId();
        String url = host + "/browser/" +repositoryId + "/root?cmisaction=download&objectId=" + objectId;
        return dmsConnection.connectTOAPISAPCloudDMS(url, token, HttpMethod.GET, false);
    }
}
