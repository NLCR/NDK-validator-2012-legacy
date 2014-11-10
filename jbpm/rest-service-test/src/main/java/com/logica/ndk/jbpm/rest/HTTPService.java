package com.logica.ndk.jbpm.rest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class HTTPService {
    public static String KEY_USERNAME = "j_username";
    public static String KEY_PASSWORD = "j_password";
    private HttpClient httpClient;

    public HTTPService() {
        httpClient = new HttpClient();
    }

    public String authenticate(String hostAndPort, String username, String password) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(KEY_USERNAME, username);
        data.put(KEY_PASSWORD, password);
        return postMethod("http://" + hostAndPort + "/gwt-console-server/rs/process/j_security_check", data);
    }
    
    public String getMethod(String url, Map<String, Object> parameters) {
        String responseString = "";
        GetMethod getMethod = new GetMethod(url);
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            int status = httpClient.executeMethod(getMethod);
            System.out.println("status: " + status);
            InputStream inputStream = getMethod.getResponseBodyAsStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
            responseString = stringBuilder.toString();
            /*
            sb.append(theMethod.getResponseBodyAsString());
            System.out.println("JSon Result: => " + sb.toString());             
             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            getMethod.releaseConnection();
        }
        return responseString;
    }

    public String postMethod(String url, Map<String, Object> parameters) {
        String responseString = "";
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        NameValuePair[] data = new NameValuePair[parameters.size()];
        Set<String> keys = parameters.keySet();
        int i = 0;
        for (Iterator<String> keysIterator = keys.iterator(); keysIterator.hasNext();) {
            String keyString = keysIterator.next();
            String value = parameters.get(keyString).toString();
            data[i] = new NameValuePair(keyString, value);
            i++;
        }
        PostMethod postMethod = new PostMethod(url);
        postMethod.setRequestHeader("ContentType", "application/x-www-form-urlencoded");
        postMethod.setRequestBody(data);
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            int status = httpClient.executeMethod(postMethod);
            System.out.println("status: " + status);
            InputStream inputStream = postMethod.getResponseBodyAsStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
            responseString = stringBuilder.toString();
            /*
            sb.append(theMethod.getResponseBodyAsString());
            System.out.println("JSon Result: => " + sb.toString());             
             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            postMethod.releaseConnection();
        }
        return responseString;
    }
}
