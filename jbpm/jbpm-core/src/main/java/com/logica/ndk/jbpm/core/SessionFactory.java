package com.logica.ndk.jbpm.core;

import com.logica.ndk.jbpm.core.integration.impl.HumanTaskService;
import com.logica.ndk.jbpm.core.integration.impl.JPAWorkingMemoryDbLoggerExtend;
import com.logica.ndk.jbpm.core.integration.impl.TaskManagement;
import com.logica.ndk.jbpm.core.listener.ProcessEventListenerImpl;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.SystemException;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.WorkingMemory;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.command.impl.KnowledgeCommandContext;
import org.drools.compiler.BPMN2ProcessFactory;
import org.drools.compiler.ProcessBuilderFactory;
import org.drools.event.*;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.ResourceChangeScannerConfiguration;
import org.drools.io.ResourceFactory;
import org.drools.marshalling.impl.ProcessMarshallerFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessRuntimeFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.transaction.TransactionManagerLookup;
import org.jbpm.bpmn2.BPMN2ProcessProviderImpl;
import org.jbpm.integration.console.shared.GuvnorConnectionUtils;
import org.jbpm.marshalling.impl.ProcessMarshallerFactoryServiceImpl;
import org.jbpm.process.builder.ProcessBuilderFactoryServiceImpl;
import org.jbpm.process.instance.ProcessRuntimeFactoryServiceImpl;
import org.jbpm.process.workitem.wsht.CommandBasedWSHumanTaskHandler;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class SessionFactory {
  private static final Logger LOG = LoggerFactory.getLogger(SessionFactory.class);

  //private static StatelessKnowledgeSession kssession;
  private static StatefulKnowledgeSession ksession;
  private static StatefulKnowledgeSession oldSession;
  private static JPAWorkingMemoryDbLoggerExtend dbLogger;
  private static Long sessionExpTime;
  private static int hoursToExp = TmConfig.instance().getInt("jbpmws.secondsToExp");
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd HH:mm:ss");

  private static KnowledgeBase kbase = initGuvnor();
  private static KnowledgeSessionConfiguration config = initKnowledgeSessionConfiguration();
  private static Environment env = initEnvProperties();
  private static Properties jbpmconsoleproperties = initJbpmConsoleProp();
  private static ProcessEventListenerImpl eventListenerImpl = new ProcessEventListenerImpl();

  private static EntityManagerFactory emf;

  private static int sessionCounter = 0;

  private SessionFactory() {
  }

  public static synchronized StatefulKnowledgeSession getSession() {
    if (ksession == null) {
      initSessionObjects();
    }

    /*LOG.info("Stact trace: " + new SystemException());
    
    if (new Date().after(new Date(sessionExpTime))) {
      LOG.info("Session expires!, creating new. Session counter: " + sessionCounter);
      while(sessionCounter > 0){
        try {
          Thread.sleep(100l);
        }
        catch (InterruptedException e) {
          LOG.error("Interupted");
        }
        LOG.info("Session counter: " + sessionCounter);
      }
      if(oldSession != null){
        oldSession.dispose();
      }
      oldSession = ksession;
      initSessionObjects();
    }
    sessionCounter++;
    LOG.info("Get session: " + sessionCounter);*/
    return ksession;
  }

  public static void returnSession() {
    /*sessionCounter --;
    LOG.info("Session return: " + sessionCounter);
    if(sessionCounter < 0){
      sessionCounter = 0;
    }*/

  }

  public static synchronized JPAWorkingMemoryDbLoggerExtend getDbLogger() {
    if (dbLogger == null) {
      initSessionObjects();
    }
    return dbLogger;
  }

  private static void initSessionObjects() {
    ksession = newStatefulKnowledgeSession();
    if (dbLogger != null) {
      LOG.info("Disposing dbLogger before crating newone");
      dbLogger.dispose();
    }
    dbLogger = new JPAWorkingMemoryDbLoggerExtend(ksession);

    /*Calendar calendar = Calendar.getInstance();
    Date now = new Date();
    calendar.setTime(now);
    calendar.add(Calendar.SECOND, hoursToExp);
    LOG.info("Session expires at " + dateFormat.format(calendar.getTime()));
    sessionExpTime = calendar.getTime().getTime();*/
  }

  private static Properties initJbpmConsoleProp() {
    Properties jbpmconsoleproperties = new Properties();
    InputStream resourceAsStream = null;
    try {
      resourceAsStream = SessionFactory.class.getResourceAsStream("/jbpm.console.properties");
      jbpmconsoleproperties.load(resourceAsStream);
    }
    catch (Exception e) {
      LOG.error("Could not load jbpm.console.properties", e);
      throw new RuntimeException("Could not load jbpm.console.properties", e);
    }
    finally {
      if (resourceAsStream != null) {
        try {
          resourceAsStream.close();
        }
        catch (IOException e) {
          LOG.error("Can't close resource stream.", e);
        }
      }
    }
    return jbpmconsoleproperties;
  }

  private static KnowledgeBase initGuvnor() {
    KnowledgeBase kbase = null;
    GuvnorConnectionUtils guvnorUtils = new GuvnorConnectionUtils();
    if (guvnorUtils.guvnorExists()) {
      try {
        ResourceChangeScannerConfiguration sconf = ResourceFactory.getResourceChangeScannerService().newResourceChangeScannerConfiguration();
        sconf.setProperty("drools.resource.scanner.interval", "10");
        ResourceFactory.getResourceChangeScannerService().configure(sconf);
        ResourceFactory.getResourceChangeScannerService().start();
        ResourceFactory.getResourceChangeNotifierService().start();
        KnowledgeAgentConfiguration aconf = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
        aconf.setProperty("drools.agent.newInstance", "false");
        KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent("Guvnor default", aconf);
        kagent.applyChangeSet(ResourceFactory.newReaderResource(guvnorUtils.createChangeSet()));
        kbase = kagent.getKnowledgeBase();
      }
      catch (Throwable t) {
        LOG.error("Could not load processes from Guvnor: " + t.getMessage());
      }
    }
    else {
      LOG.warn("Could not connect to Guvnor.");
    }
    if (kbase == null) {
      kbase = KnowledgeBaseFactory.newKnowledgeBase();
    }
    return kbase;
  }

  private static KnowledgeSessionConfiguration initKnowledgeSessionConfiguration() {
    Properties sessionconfigproperties = new Properties();
    sessionconfigproperties.put("drools.processInstanceManagerFactory", "org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory");
    sessionconfigproperties.put("drools.processSignalManagerFactory", "org.jbpm.persistence.processinstance.JPASignalManagerFactory");
    KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(sessionconfigproperties);
    return config;
  }

  private static Environment initEnvProperties() {
    emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");
    //Environment env = KnowledgeBaseFactory.newEnvironment();
    env = new CustomEnvironment();
    env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

    // get transaction manager from hibernate configuration (see persistence.xml)
    TransactionManagerLookup transactionManagerLookup = ((SessionFactoryImplementor) ((HibernateEntityManagerFactory) emf).getSessionFactory()).getSettings().getTransactionManagerLookup();
    TransactionManager transactionManager = transactionManagerLookup.getTransactionManager(null);
    // this is very important step to add transactionManager into env. Else transaction manager will not be initialized correctly for jbpm.
    env.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
    return env;
  }

  private static StatefulKnowledgeSession newStatefulKnowledgeSession() {
    try {
      LOG.info("Class loader: " + SessionFactory.class.getClassLoader().toString());
      try {
        throw new SystemException();
      }
      catch (SystemException ex) {
        LOG.error("Stact trace", ex);
      }

      // u can call readProcessDefinitionsFromFolder here to read additional process definitions from local directory
      StatefulKnowledgeSession ksession = null;
      //StatefulKnowledgeSession ksession = null;

      env = initEnvProperties();
      try {
        LOG.info("Loading session data ...");
        int sessionId = 1; //defult one
        try {
          sessionId = TmConfig.instance().getInt("process.sessionId");
        }
        catch (Exception e) {
          //do nothing, we will use default sesion id
        }
        LOG.info("Session id to be use = " + sessionId);
        //ksession = kbase.
        ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, config, env);
        LOG.info("Session was loaded! " + ksession.getId());
      }
      catch (RuntimeException e) {
        LOG.info("Error loading session data for old session: " + e.getMessage());
        if (e instanceof IllegalStateException) {
          Throwable cause = ((IllegalStateException) e).getCause();
          if (cause instanceof InvocationTargetException) {
            cause = cause.getCause();
            if (cause != null && "Could not find session data for id 1".equals(cause.getMessage())) {
              LOG.info("Creating new session data ...");
              ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, config, env);
            }
            else {
              LOG.error("Error creating new session data: " + cause);
              throw e;
            }
          }
          else {
            LOG.error("Error creating new session data: " + cause);
            throw e;
          }
        }
        else {
          LOG.error("Error creating new session data: " + e.getMessage());
          throw e;
        }
        LOG.info("Session was created! " + ksession.getId());
      }
      if ("Mina".equals(TaskManagement.TASK_SERVICE_STRATEGY)) {
        CommandBasedWSHumanTaskHandler handler = new CommandBasedWSHumanTaskHandler(ksession);
        handler.setConnection(jbpmconsoleproperties.getProperty("jbpm.console.task.service.host"),
            new Integer(jbpmconsoleproperties.getProperty("jbpm.console.task.service.port")));
        ksession.getWorkItemManager().registerWorkItemHandler(
            "Human Task", handler);
        handler.connect();
      }
      else if ("Local".equals(TaskManagement.TASK_SERVICE_STRATEGY)) {
        TaskService taskService = HumanTaskService.getService();
        SyncWSHumanTaskHandler handler = new SyncWSHumanTaskHandler(new LocalTaskService(taskService.createSession()), ksession);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
      }
      final org.drools.event.AgendaEventListener agendaEventListener = new org.drools.event.AgendaEventListener() {
        public void activationCreated(ActivationCreatedEvent event,
            WorkingMemory workingMemory) {
        }

        public void activationCancelled(ActivationCancelledEvent event,
            WorkingMemory workingMemory) {
        }

        public void beforeActivationFired(BeforeActivationFiredEvent event,
            WorkingMemory workingMemory) {
        }

        public void afterActivationFired(AfterActivationFiredEvent event,
            WorkingMemory workingMemory) {
        }

        public void agendaGroupPopped(AgendaGroupPoppedEvent event,
            WorkingMemory workingMemory) {
        }

        public void agendaGroupPushed(AgendaGroupPushedEvent event,
            WorkingMemory workingMemory) {
        }

        public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event,
            WorkingMemory workingMemory) {
        }

        public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event,
            WorkingMemory workingMemory) {
          workingMemory.fireAllRules();
        }

        public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event,
            WorkingMemory workingMemory) {
        }

        public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event,
            WorkingMemory workingMemory) {
        }
      };
      ((StatefulKnowledgeSessionImpl) ((KnowledgeCommandContext) ((CommandBasedStatefulKnowledgeSession) ksession)
          .getCommandService().getContext()).getStatefulKnowledgesession()).session.addEventListener(agendaEventListener);
      ksession.addEventListener(eventListenerImpl);

      return ksession;
    }
    catch (Throwable t) {
      throw new RuntimeException(
          "Could not initialize stateful knowledge session: "
              + t.getMessage(), t);
    }
  }

  /**
   * Read some process definitions from local directory. These definitions will be added to kbase which already contains
   * definitions from guvnor repo.
   * 
   * @param kbase
   * @param jbpmconsoleproperties
   */
  protected void readProcessDefinitionsFromFolder(KnowledgeBase kbase, Properties jbpmconsoleproperties) {
    String directory = System.getProperty("jbpm.console.directory") == null ? jbpmconsoleproperties.getProperty("jbpm.console.directory") :
        System.getProperty("jbpm.console.directory");
    if (directory == null || directory.length() < 1) {
      LOG.error("jbpm.console.directory property not found");
    }
    else {
      File file = new File(directory);
      if (!file.exists()) {
        throw new IllegalArgumentException("Could not find " + directory);
      }
      if (!file.isDirectory()) {
        throw new IllegalArgumentException(directory + " is not a directory");
      }
      ProcessBuilderFactory.setProcessBuilderFactoryService(new ProcessBuilderFactoryServiceImpl());
      ProcessMarshallerFactory.setProcessMarshallerFactoryService(new ProcessMarshallerFactoryServiceImpl());
      ProcessRuntimeFactory.setProcessRuntimeFactoryService(new ProcessRuntimeFactoryServiceImpl());
      BPMN2ProcessFactory.setBPMN2ProcessProvider(new BPMN2ProcessProviderImpl());
      KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
      for (File subfile : file.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".bpmn") || name.endsWith("bpmn2");
        }
      })) {
        LOG.info("Loading process from file system: " + subfile.getName());
        kbuilder.add(ResourceFactory.newFileResource(subfile), ResourceType.BPMN2);
      }
      kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }
  }

  public static EntityManagerFactory getEntityManagerFactory() {
    if (emf == null) {
      getSession();
    }
    return emf;
  }

}
