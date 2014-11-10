package com.logica.ndk.jbpm.rest;

import java.util.Map;

public class ConsoleClient {
    private final String hostAndPort;

    public ConsoleClient(String hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public String getHostAndPort() {
        return hostAndPort;
    }

    public void startProcess(String processId, Map<String, Object> parameters) {
        sendPostRequest("http://" + hostAndPort + "/gwt-console-server/rs/process/definition/" + processId
                + "/ndk_new_instance", parameters);
    }

    public void endInstance(String instanceId) {
        sendPostRequest("http://" + hostAndPort + "/gwt-console-server/rs/process/instance/" + instanceId
                + "/end/OBSOLETE", null);
    }

    public void signalEvent(String instanceId, String type, String eventData) {
        sendPostRequest("http://" + hostAndPort + "/gwt-console-server/rs/process/tokens/" + instanceId
                + "/ndk_signal?type=" + type + "&event=" + eventData, null);
    }

    public void instances(String processId) {
        sendGetRequest("http://" + hostAndPort + "/gwt-console-server/rs/process/definition/" + processId
                + "/instances");
    }

    private void sendGetRequest(String url) {
        HTTPService httpService = new HTTPService();
        String requestPostService = httpService.getMethod(url, null);
        if (requestPostService.contains("HTTP 401")) {
            System.out.println("requested auth");
            String authenticate = httpService.authenticate(hostAndPort, "admin", "admin");
            System.out.println("auth response: " + authenticate);
            requestPostService = httpService.getMethod(url, null);
        } else {
            System.out.println("without auth");
        }
        System.out.println("post response: " + requestPostService);
    }

    private void sendPostRequest(String url, Map<String, Object> parameters) {
        HTTPService httpService = new HTTPService();
        String requestPostService = httpService.postMethod(url, parameters);
        if (requestPostService.contains("HTTP 401")) {
            System.out.println("requested auth");
            String authenticate = httpService.authenticate(hostAndPort, "admin", "admin");
            System.out.println("auth response: " + authenticate);
            requestPostService = httpService.postMethod(url, parameters);
        } else {
            System.out.println("without auth");
        }
        System.out.println("post response: " + requestPostService);
    }
}
