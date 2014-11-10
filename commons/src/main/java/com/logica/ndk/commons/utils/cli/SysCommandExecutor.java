package com.logica.ndk.commons.utils.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;



/**
 * Usage of following class can go as ...
 * <P>
 * 
 * <PRE>
 * <CODE>
 * 		SysCommandExecutor cmdExecutor = new SysCommandExecutor();
 * 		cmdExecutor.setOutputLogDevice(new LogDevice());
 * 		cmdExecutor.setErrorLogDevice(new LogDevice());
 * 		int exitStatus = cmdExecutor.runCommand(commandLine);
 * </CODE>
 * </PRE>
 * 
 * </P>
 * OR
 * <P>
 * 
 * <PRE>
 * <CODE>
 * 		SysCommandExecutor cmdExecutor = new SysCommandExecutor(); 		
 * 		int exitStatus = cmdExecutor.runCommand(commandLine);
 * 
 * 		String cmdError = cmdExecutor.getCommandError();
 * 		String cmdOutput = cmdExecutor.getCommandOutput(); 
 * </CODE>
 * </PRE>
 * 
 * </P>
 */
public class SysCommandExecutor
{
  private static final Logger log = LoggerFactory.getLogger(SysCommandExecutor.class);
  private static final long DEFAULT_COMMAND_TIMEOUT = 0; // 0 means no timeout
  private ILogDevice fOuputLogDevice = null;
  private ILogDevice fErrorLogDevice = null;
  private String fWorkingDirectory = null;
  private List fEnvironmentVarList = null;

  private StringBuffer fCmdOutput = null;
  private StringBuffer fCmdError = null;
  private AsyncStreamReader fCmdOutputThread = null;
  private AsyncStreamReader fCmdErrorThread = null;
  
  private String outputEncoding = null;
  
  public SysCommandExecutor() {}
  
  public SysCommandExecutor(String outputEncoding) {
    this.outputEncoding = outputEncoding;
  }

  public void setOutputLogDevice(ILogDevice logDevice)
  {
    fOuputLogDevice = logDevice;
  }

  public void setErrorLogDevice(ILogDevice logDevice)
  {
    fErrorLogDevice = logDevice;
  }

  public void setWorkingDirectory(String workingDirectory) {
    fWorkingDirectory = workingDirectory;
  }

  public void setEnvironmentVar(String name, String value)
  {
    if (fEnvironmentVarList == null)
      fEnvironmentVarList = new ArrayList();

    fEnvironmentVarList.add(new EnvironmentVar(name, value));
  }

  public String getCommandOutput() {
    return fCmdOutput.toString();
  }

  public String getCommandError() {
    return fCmdError.toString();
  }
  
  public int runCommand(String commandLine) throws Exception {
    return runCommand(commandLine, DEFAULT_COMMAND_TIMEOUT);
  }

  public int runCommand(String commandLine, long timeoutInMillis) throws Exception
  {
    /* run command */
    Process process = runCommandHelper(commandLine);

    Integer pid = getPid(process);
    log.info("Process with pid: " + pid + " created for sys commmand: " + commandLine + " timeout: " + timeoutInMillis);
    /* start output and error read threads */
    startOutputAndErrorReadThreads(process.getInputStream(), process.getErrorStream());

    /* wait for command execution to terminate */
    int exitStatus = -1;
    CommandWaitingThread waitingThread = null;
    try {
      waitingThread = new CommandWaitingThread(process);
      waitingThread.start();
      // dame procesu isty cas na vykonanie
      waitingThread.join(timeoutInMillis);
      // ak sa proces este neukoncil hodime vynimku
      if (waitingThread.getExitStatus() != null) {
        exitStatus = waitingThread.getExitStatus();
      } else {
        log.error("Timeout reached: " + timeoutInMillis);
        throw new TimeoutException("Timeout reached: " + timeoutInMillis);
      }
    }
    catch (TimeoutException e) {
      throw e;
    }
    catch (Throwable ex) {
      log.error(ex.getMessage(), ex);
      throw new Exception(ex);
    }
    finally {
      try {
        // pockame aby sa thready naozaj ukoncili a precitalo sa vsetko zo streamov. Ak sa neukoncia do timeout pokracuje sa dalej
        fCmdOutputThread.join(1000);
        fCmdErrorThread.join(1000);
        // ukoncenie veci ak neboli ukoncene
        process.destroy();
        fCmdOutputThread.interrupt();
        fCmdErrorThread.interrupt();
        waitingThread.interrupt();
      }
      catch (Throwable ex) {
        log.error(ex.getMessage(), ex);
        throw new Exception(ex);
      }
    }

    return exitStatus;
  }
  
  private Integer getPid(Process process) throws Exception {
    if (process.getClass().getName().equals("java.lang.Win32Process") ||
        process.getClass().getName().equals("java.lang.ProcessImpl")) {
      /* determine the pid on windows plattforms */
      try {
        Field f = process.getClass().getDeclaredField("handle");
        f.setAccessible(true);
        long handl = f.getLong(process);
        Kernel32 kernel = Kernel32.INSTANCE;
        W32API.HANDLE handle = new W32API.HANDLE();
        handle.setPointer(Pointer.createConstant(handl));
        return kernel.GetProcessId(handle);
      }
      catch (Exception e) {
        log.error("Error at getting pid for sys command process!", e);
        throw e;
      }
    }
    return null;
  }

  private Process runCommandHelper(String commandLine) throws IOException
  {
    Process process = null;
    if (fWorkingDirectory == null)
      process = Runtime.getRuntime().exec(commandLine, getEnvTokens());
    else
      process = Runtime.getRuntime().exec(commandLine, getEnvTokens(), new File(fWorkingDirectory));

    return process;
  }

  private void startOutputAndErrorReadThreads(InputStream processOut, InputStream processErr)
  {
    fCmdOutput = new StringBuffer();
    fCmdOutputThread = new AsyncStreamReader(processOut, fCmdOutput, fOuputLogDevice, "OUTPUT", outputEncoding);
    fCmdOutputThread.start();

    fCmdError = new StringBuffer();
    fCmdErrorThread = new AsyncStreamReader(processErr, fCmdError, fErrorLogDevice, "ERROR", outputEncoding);
    fCmdErrorThread.start();
  }

  private void notifyOutputAndErrorReadThreadsToStopReading()
  {
    fCmdOutputThread.stopReading();
    fCmdErrorThread.stopReading();
  }

  private String[] getEnvTokens()
  {
    if (fEnvironmentVarList == null)
      return null;

    String[] envTokenArray = new String[fEnvironmentVarList.size()];
    Iterator envVarIter = fEnvironmentVarList.iterator();
    int nEnvVarIndex = 0;
    while (envVarIter.hasNext() == true)
    {
      EnvironmentVar envVar = (EnvironmentVar) (envVarIter.next());
      String envVarToken = envVar.fName + "=" + envVar.fValue;
      envTokenArray[nEnvVarIndex++] = envVarToken;
    }

    return envTokenArray;
  }
}

class AsyncStreamReader extends Thread
{
  private StringBuffer fBuffer = null;
  private InputStream fInputStream = null;
  private String fThreadId = null;
  private boolean fStop = false;
  private ILogDevice fLogDevice = null;
  private String outputEncoding = null;

  private String fNewLine = null;

  public AsyncStreamReader(InputStream inputStream, StringBuffer buffer, ILogDevice logDevice, String threadId, String encoding)
  {
    fInputStream = inputStream;
    fBuffer = buffer;
    fThreadId = threadId;
    fLogDevice = logDevice;
    outputEncoding = encoding;

    fNewLine = System.getProperty("line.separator");
  }

  public String getBuffer() {
    return fBuffer.toString();
  }

  public void run()
  {
    try {
      readCommandOutput();
    }
    catch (Exception ex) {
      //ex.printStackTrace(); //DEBUG
    }
  }

  private void readCommandOutput() throws IOException
  {
    InputStreamReader reader = null;
    if (outputEncoding != null) {
      reader = new InputStreamReader(fInputStream, outputEncoding);
    }
    else {
      reader = new InputStreamReader(fInputStream);
    }
    BufferedReader buffer = new BufferedReader(reader);
    String line = null;
    try {
      while ((line = buffer.readLine()) != null)
      {
        fBuffer.append(line + fNewLine);
        printToDisplayDevice(line);
      }
    } finally {
      buffer.close();
    }
    //printToConsole("END OF: " + fThreadId); //DEBUG
  }

  public void stopReading() {
    fStop = true;
  }

  private void printToDisplayDevice(String line)
  {
    if (fLogDevice != null)
      fLogDevice.log(line);
    else
    {
      //printToConsole(line);//DEBUG
    }
  }

  private synchronized void printToConsole(String line) {
    System.out.println(line);
  }
  
}

class EnvironmentVar
{
  public String fName = null;
  public String fValue = null;

  public EnvironmentVar(String name, String value)
  {
    fName = name;
    fValue = value;
  }
}

class CommandWaitingThread extends Thread {
  private final Process process;
  private Integer exitStatus;

  public CommandWaitingThread(Process process) {
    this.process = process;
  }

  public void run() {
    try {
      exitStatus = process.waitFor();
    }
    catch (InterruptedException ignore) {
      return;
    }
  }

  public Integer getExitStatus() {
    return exitStatus;
  }

  public void setExitStatus(int exitStatus) {
    this.exitStatus = exitStatus;
  }
  
}