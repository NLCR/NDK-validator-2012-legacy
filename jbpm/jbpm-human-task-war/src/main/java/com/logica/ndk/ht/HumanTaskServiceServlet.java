package com.logica.ndk.ht;

import org.apache.commons.io.IOUtils;
import org.drools.SystemEventListenerFactory;
import org.jbpm.task.*;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.UserGroupCallbackManager;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExpressionCompiler;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HumanTaskServiceServlet extends HttpServlet {

  private static final long serialVersionUID = 5L;

  public void init() throws ServletException {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.task");
    TaskService taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
    TaskServiceSession taskSession = taskService.createSession();
    // Add users
    Map vars = new HashMap();
    Reader reader = null;
    try {
      reader = new InputStreamReader(HumanTaskServiceServlet.class.getResourceAsStream("LoadUsers.mvel"));
      Map<String, User> users = (Map<String, User>) eval(reader, vars);
      if (users != null) {
        for (User user : users.values()) {
          taskSession.addUser(user);
        }
      }
      reader = new InputStreamReader(HumanTaskServiceServlet.class.getResourceAsStream("LoadGroups.mvel"));
      Map<String, Group> groups = (Map<String, Group>) eval(reader, vars);
      if (groups != null) {
        for (Group group : groups.values()) {
          taskSession.addGroup(group);
        }
      }
      // try to get the usergroup callback properties
      reader = new InputStreamReader(HumanTaskServiceServlet.class.getResourceAsStream("jbpm.usergroup.callback.properties"));
      if (reader != null) {
        Properties callbackproperties = new Properties();
        try {
          callbackproperties.load(reader);
          UserGroupCallbackManager.getInstance().setCallbackFromProperties(callbackproperties);
          System.out.println("Task service registered usergroup callback ...");
        }
        catch (Exception e) {
          System.out.println("Task service unable to register usergroup callback ...");
        }
      }
    }
    finally {
      IOUtils.closeQuietly(reader);
    }

    // start server
    MinaTaskServer server = new MinaTaskServer(taskService);
    Thread thread = new Thread(server);
    thread.start();
    taskSession.dispose();
    System.out.println("Task service started correctly !");
    System.out.println("Task service running ...");
  }

  public static Object eval(Reader reader, Map vars) {
    try {
      return eval(readerToString(reader), vars);
    }
    catch (IOException e) {
      throw new RuntimeException("Exception Thrown", e);
    }
  }

  public static String readerToString(Reader reader) throws IOException {
    int charValue = 0;
    StringBuffer sb = new StringBuffer(1024);
    while ((charValue = reader.read()) != -1) {
      //result = result + (char) charValue;
      sb.append((char) charValue);
    }
    return sb.toString();
  }

  public static Object eval(String str, Map vars) {
    ExpressionCompiler compiler = new ExpressionCompiler(str.trim());

    ParserContext context = new ParserContext();
    context.addPackageImport("org.jbpm.task");
    context.addPackageImport("java.util");

    context.addImport("AccessType", AccessType.class);
    context.addImport("AllowedToDelegate", AllowedToDelegate.class);
    context.addImport("Attachment", Attachment.class);
    context.addImport("BooleanExpression", BooleanExpression.class);
    context.addImport("Comment", Comment.class);
    context.addImport("Deadline", Deadline.class);
    context.addImport("Deadlines", Deadlines.class);
    context.addImport("Delegation", Delegation.class);
    context.addImport("Escalation", Escalation.class);
    context.addImport("Group", Group.class);
    context.addImport("I18NText", I18NText.class);
    context.addImport("Notification", Notification.class);
    context.addImport("OrganizationalEntity", OrganizationalEntity.class);
    context.addImport("PeopleAssignments", PeopleAssignments.class);
    context.addImport("Reassignment", Reassignment.class);
    context.addImport("Status", Status.class);
    context.addImport("Task", Task.class);
    context.addImport("TaskData", TaskData.class);
    context.addImport("TaskSummary", TaskSummary.class);
    context.addImport("User", User.class);

    return MVEL.executeExpression(compiler.compile(context), vars);
  }

  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    // response.sendError(1001, "GET Method Not Allowed Here");
    doPost(request, response);
  }

  protected void doPost(HttpServletRequest reqquest,
      HttpServletResponse response) throws ServletException, IOException {
    response.sendError(1001, "POST Method Not Allowed Here");
  }
}
