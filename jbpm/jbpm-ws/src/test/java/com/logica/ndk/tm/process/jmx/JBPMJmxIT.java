package com.logica.ndk.tm.process.jmx;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;

public class JBPMJmxIT {
    @Test
    public void testState() {
        // Get a connection to the JBoss AS MBean server on localhost
        String host = "localhost";
        int port = 1090;
        long instanceId = 3006;
        try {
            String urlString = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
            System.out.println(urlString);
            JMXServiceURL serviceURL = new JMXServiceURL(urlString);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName("com.logica.ndk:type=JBPMMBean");
            MBeanInfo mBeanInfo = connection.getMBeanInfo(name);
            MBeanOperationInfo[] operations = mBeanInfo.getOperations();
            for (int i = 0; i < operations.length; i++) {
                System.out.println(operations[i]);
            }
            Object result = connection.invoke(name, "state", new Object[]{instanceId}, new String[]{"long"});
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testEnd() {
      // Get a connection to the JBoss AS MBean server on localhost
      String host = "localhost";
      int port = 1090;
      long instanceId = 3006;
      try {
          String urlString = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
          System.out.println(urlString);
          JMXServiceURL serviceURL = new JMXServiceURL(urlString);
          JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
          MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
          ObjectName name = new ObjectName("com.logica.ndk:type=JBPMMBean");
          MBeanInfo mBeanInfo = connection.getMBeanInfo(name);
          MBeanOperationInfo[] operations = mBeanInfo.getOperations();
          for (int i = 0; i < operations.length; i++) {
              System.out.println(operations[i]);
          }
          Object result = connection.invoke(name, "endInstance", new Object[]{instanceId, null}, new String[]{"long", "java.lang.String"});
          System.out.println(result);
      } catch (Exception e) {
          e.printStackTrace();
      }
    }
}
