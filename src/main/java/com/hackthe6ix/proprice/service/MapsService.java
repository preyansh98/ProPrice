package com.hackthe6ix.proprice.service;

import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hackthe6ix.proprice.domain.request.PlacesRequest;
import com.hackthe6ix.proprice.domain.response.ProductMapsResponse;
import com.hackthe6ix.proprice.utils.GMapsConstants;
import com.hackthe6ix.proprice.utils.Keys;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@Service
public class MapsService {

    private static final Logger logger = LoggerFactory.getLogger(MapsService.class);

    public ProductMapsResponse queryPlaces(String productName, Double user_lat, Double user_long) throws URISyntaxException, IOException {
        ProductMapsResponse resp = null;

        HttpClient httpClient = HttpClients.createDefault();

        URIBuilder builder = new URIBuilder().setHost(GMapsConstants.GOOGLE_MAPS_ENDPOINT)
                                        .setParameter("key", Keys.GOOGLE_MAPS_API_KEY)
                                        .setParameter("input", productName)
                                        .setParameter("inputtype", GMapsConstants.GOOGLE_MAPS_INPUT_TYPE)
                                        .setParameter("fields", genFields(user_lat, user_long));

        HttpGet getRequest = new HttpGet(builder.build());

        System.out.println("REQUEST::: " + getRequest.toString());
        HttpResponse httpResponse = httpClient.execute(getRequest);

        if(httpResponse.getStatusLine().getStatusCode() == HttpStatusCodes.STATUS_CODE_OK){

            String json = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

            ProductMapsResponse responseToFE = new ProductMapsResponse();

            JsonObject topCandidate = jsonObject.getAsJsonArray("candidates").get(0).getAsJsonObject();

            responseToFE.setAddress(topCandidate.get("formatted_address").toString());
            responseToFE.setName(topCandidate.get("name").toString());
            responseToFE.setDest_lat((topCandidate.get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat")).getAsDouble());
            responseToFE.setDest_long((topCandidate.get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng")).getAsDouble());
            responseToFE.setOpen(Boolean.valueOf(topCandidate.get("opening_hours").getAsJsonObject().get("open_now").getAsBoolean()));

            resp = responseToFE;
        } else {
            throw new IllegalStateException("Google Maps response failed");
        }
        return resp;
    }

    private String genFields(Double user_lat, Double user_long){
        StringBuilder sBuilder = new StringBuilder();

        sBuilder.append("formatted_address,name,rating,opening_hours,geometry");

//        sBuilder.append("geometry,formatted_address,name,opening_hours,rating&locationbias=circle:2000)@")
//                .append((user_lat)).append(",")
//                .append((user_long));

        return sBuilder.toString();
    }

}
