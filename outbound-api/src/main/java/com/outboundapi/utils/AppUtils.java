/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.outboundapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.outboundapi.models.ApiError;
import com.outboundapi.models.TipsErrorResponse;
import okhttp3.OkHttpClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class AppUtils {

    public static Date now() {
        return Calendar.getInstance(TimeZone.getTimeZone("Africa/Nairobi")).getTime();
    }

    public static String getDate(String format) {
        return new SimpleDateFormat(format).format(AppUtils.now());
    }

    public ApiError mapErrorResponse(String errorResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        try {
            return objectMapper.readValue(errorResponse, ApiError.class);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            System.out.print(e.getMessage());
        }
        return null;
    }

    public String convertToJson(Object object, boolean prettyPrint) {
        try {
            ObjectWriter ow = new ObjectMapper().writer();
            if (prettyPrint) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
            }
            return ow.writeValueAsString(object);
        } catch (Exception ex) {
            return " An error has occurred " + ex.getMessage();
        }
    }

    public static String convertToJson(Object object) {
        try {
            ObjectWriter ow = new ObjectMapper().writer();
            return ow.writeValueAsString(object);
        } catch (Exception ex) {
            ;
            return null;
        }
    }

    public static Map<String, Object> convertJsonToObject(String string) {
        ObjectMapper om = new ObjectMapper();
        try {
            return (Map<String, Object>) (om.readValue(string, Map.class));
        } catch (Exception e) {
            return null;
        }

    }

    public static Map<String, String> readXML(String xml, String tagName, String[] elements){
        Document doc = convertStringToXMLDocument( xml );
        Map<String, String> response = new HashMap<>();
        //Verify XML document is build correctly
        assert doc != null;
        System.out.println(doc.getFirstChild().getNodeName());
        NodeList responseList = doc.getElementsByTagName(tagName);
        for (int index = 0; index < responseList.getLength(); index++) {

            Node nNode = responseList.item(index);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                for (String element: elements){
                    response.put(element, eElement.getElementsByTagName(element).item(0).getTextContent());
                }
            }
        }
        return response;
    }

    private static Document convertStringToXMLDocument(String xmlString) {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();
            //Parse the content to Document object
            return builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.out.println("Error " + e.getMessage());
        }
        return null;
    }

    public static TipsErrorResponse mapTipsErrorResponse(String errorResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        try {
            return objectMapper.readValue(errorResponse, TipsErrorResponse.class);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            System.out.print(e.getMessage());
        }
        return null;
    }

    public static Map<String, String> validateInternalRequests(Map<String, String> headers){
        // check if channel-id is provided in the headers
        // check if ip is whitelisted
        // check if requestId has been provided
        Map<String,String> response = new HashMap<>();
        String status = "success";
        String errors = "";
        if(headers.isEmpty()){
            errors = "The headers are empty";
            status = "fail";
        }
        if(headers.get("channel-id")==null || headers.get("channel-id").isEmpty()){
            errors = "channel-id header not present or empty";
            status = "fail";
        }
        if(headers.get("request-id") == null || headers.get("request-id").isEmpty()){
            errors = "request-id header not present or empty";
            status = "fail";
        }
        response.put("status", status);
        response.put("errors", errors);
        return response;
    }

    public static void bypassSSL(OkHttpClient.Builder builder, String traceId){
        //boolean bypassSSl = true;
        try{
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] trustManager = trustCerts();
            sslContext.init(null, trustManager, new java.security.SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManager[0]);
            builder.hostnameVerifier((hostname, session) -> true);
        }catch (Exception e){
            System.out.printf("%s - An error has occurred - %s", traceId, e.getMessage());
        }
    }

    private static TrustManager[] trustCerts(){
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }

}
