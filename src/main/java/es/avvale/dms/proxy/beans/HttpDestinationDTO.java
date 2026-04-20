package es.avvale.dms.proxy.beans;

import lombok.Data;
import com.google.gson.annotations.SerializedName;

@Data
public class HttpDestinationDTO {

    @SerializedName("Name")
    private String name;

    @SerializedName("Type")
    private String type;

    @SerializedName("Description")
    private String description;

    @SerializedName("URL")
    private String url;

    @SerializedName("ProxyType")
    private String proxyType;

    @SerializedName("Authentication")
    private String authentication;

    @SerializedName("clientId")
    private String clientId;

    @SerializedName("clientSecret")
    private String clientSecret;

    @SerializedName("tokenServiceURL")
    private String tokenServiceURL;

    @SerializedName("tokenServiceURLType")
    private String tokenServiceURLType;

    @SerializedName("repositoryId")
    private String repositoryId;
}