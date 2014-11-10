package com.logica.ndk.tm.mule.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.AttributeValueExp;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.ReflectionException;
import javax.management.StringValueExp;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to stop all JMS connectors at Mule Slave node.
 * 
 * @author Rudolf Daco
 */
public class StopSlaveConnectors {
  private static final Logger LOG = LoggerFactory.getLogger(StopSlaveConnectors.class);

  private String host;
  private String port;

  public StopSlaveConnectors(String host, String port) {
    this.host = host;
    this.port = port;
  }

  public void execute() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException, MalformedObjectNameException, NullPointerException, AttributeNotFoundException, InterruptedException {
    // service:jmx:rmi:///jndi/rmi://localhost:1097/server
    JMXConnector jmxConnector = null;
    try {
      String urlString = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server";
      JMXServiceURL serviceURL = new JMXServiceURL(urlString);
      jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
      MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
      Set<ObjectName> queryNames = connection.queryNames(
          new ObjectName("Mule.slave-*:type=Connector,*"), Query.match(new AttributeValueExp("Name"), new StringValueExp("*SlaveConnector*")));
      LOG.info("Slave connectors:");
      for (ObjectName objectName : queryNames) {
        LOG.info(objectName.toString());
      }
      LOG.info("Stopping ...");
      List<StopThread> threads = new ArrayList<StopThread>();
      for (ObjectName objectName : queryNames) {
        Boolean started = (Boolean) connection.getAttribute(objectName, "Started");
        if (started != null) {
          if (started.booleanValue() == true) {
            StopThread stopThread = new StopThread(connection, objectName, "stopConnector");
            stopThread.start();
            threads.add(stopThread);
          }
          else {
            LOG.info(objectName + " already stopped");
          }
        }
      }
      for (StopThread stopThread : threads) {
        stopThread.join();
      }
    }
    finally {
      if (jmxConnector != null) {
        jmxConnector.close();
      }
    }
  }

  private class StopThread extends Thread {
    private MBeanServerConnection connection;
    private String operationName;
    private ObjectName objectName;

    public StopThread(MBeanServerConnection connection, ObjectName objectName, String operationName) {
      this.connection = connection;
      this.operationName = operationName;
      this.objectName = objectName;
    }

    @Override
    public void run() {
      try {
        connection.invoke(objectName, operationName, new Object[] {}, new String[] {});
        LOG.info(objectName + " " + operationName + " OK");
      }
      catch (Exception e) {
        LOG.error("Error at stop slave connectors.", e);
      }
    }
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      LOG.error("Incorrect parameters. Use <script_name> <host_value> <port_value>");
      System.exit(1);
    }
    String host = args[0];
    String port = args[1];
    try {
      new StopSlaveConnectors(host, port).execute();
    }
    catch (Exception e) {
      LOG.error("Error at stop slave connectors.", e);
    }
  }
}
