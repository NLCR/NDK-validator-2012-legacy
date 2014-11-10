package com.logica.ndk.tm.utilities.transformation.jpeg2000;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;
import com.logica.ndk.tm.utilities.kakadu.KakaduException;
import com.logica.ndk.tm.utilities.kakadu.KakaduService;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord.Operation;
import com.logica.ndk.tm.utilities.transformation.TransformationException;

public class ConvertToJpeg2000LTPImpl extends AbstractUtility {

  private final CDM cdm = new CDM();

  private static final String FORMAT_DESIGNATION_NAME = "image/jp2";
  private static final String FORMAT_REGISTRY_KEY = "fmt/151";
  private static final String PRESERVATION_LEVEL_VALUE = "preservation";
  private static final String AGENT_ROLE = "software";

  private static final String ALOWED_POSTFIXES = "utility.fileChar.imgExtensions";

  private static final String RECURSIVE_FILTER = "utility.fileChar.recursive";

  private static String COMMON_PATH = "utility.convertToJpeg2000.profile.";
  private static String COMMON_PATH_LTP = COMMON_PATH + "ltp.";

  private static String agentName = TmConfig.instance().getString(COMMON_PATH_LTP + "agentName");
  private static String agentVersion = TmConfig.instance().getString(COMMON_PATH_LTP + "agentVersion");
  private static Boolean cleanTarget = TmConfig.instance().getBoolean(COMMON_PATH_LTP + "cleanTarget");

  public Integer execute(String cdmId, String source, String target, String profile) throws TransformationException {
    final OperationResult result = new OperationResult();
    checkNotNull(source);
    checkNotNull(target);
    checkNotNull(profile);

    String profileFullName = COMMON_PATH + profile;

    File sourceDir = new File(source);
    File targetDir = new File(target);
    if (!sourceDir.exists() || !sourceDir.isDirectory()) {
      log.error("sourceDir " + source + " dir doesn't exist!");
      throw new TransformationException("sourceDir " + source + " dir doesn't exist!");
    }
    if (targetDir.exists()) {
      if (!targetDir.isDirectory()) {
        log.error("target " + target + " is not correct dir path!");
        throw new TransformationException("target " + target + " is not correct dir path!");
      }
      else {
        if (cleanTarget) {
          try {
            FileUtils.cleanDirectory(targetDir);
            log.info("Cleaning target directory succesful");
          }
          catch (IOException e) {
            log.error("Error while cleaning target directory. " + e.getCause());
          }
        }
      }
    }
    else {
      log.info("targetDir " + targetDir + " dir doesn't exist. Going to create!");
      if (targetDir.mkdirs() == false) {
        log.error("error at creating target directory " + target + " !");
        throw new TransformationException("error at creating target directory " + target + " !");
      }
    }
    IOFileFilter fileFilter = null;

    fileFilter = new WildcardFileFilter(TmConfig.instance().getStringArray("utility.convertToJpeg2000.sourceExt"), IOCase.INSENSITIVE);

    final Collection<File> listFiles = FileUtils.listFiles(sourceDir, fileFilter, FileFilterUtils.falseFileFilter());
    for (final File file : listFiles) {
      if (!file.isDirectory()) {
        log.debug("File to transform: " + file.getAbsolutePath());
        OperationResult kakaduResult;
        KakaduService kakaduService;
        try {
          kakaduService = new KakaduService();
          kakaduResult = kakaduService.compress(file, targetDir, profileFullName);
        }
        catch (final KakaduException e) {
          throw new TransformationException(e);
        }
        if (kakaduResult.getState().equals(State.ERROR)) {
          result.setState(State.ERROR);
          // generate event
          if (!isEmpty(cdmId)) {
            generateEvent(kakaduService, file, cdmId, PremisCsvRecord.OperationStatus.FAILED, targetDir);
          }
        }
        // generate event
        if (!isEmpty(cdmId)) {
          generateEvent(kakaduService, file, cdmId, PremisCsvRecord.OperationStatus.OK, targetDir);
        }
        result.getResultMessage().append(kakaduResult.getResultMessage());
      }
      else {
        log.debug("Directory cannot be transformed " + file.getAbsolutePath());
      }
    }
    final boolean recursive = TmConfig.instance().getBoolean(RECURSIVE_FILTER, false);
    final String[] allowedPostfixes = TmConfig.instance().getStringArray(ALOWED_POSTFIXES);
    final IOFileFilter wildCardFilter = new WildcardFileFilter(allowedPostfixes, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
    Collection<File> listFilesAfterProcess = FileUtils.listFiles(targetDir, wildCardFilter, dirFilter);
    int countOfFilesAfterProcess = listFilesAfterProcess.size();
    log.debug("Output directory " + targetDir.getAbsolutePath() + " contains : " + countOfFilesAfterProcess);
    if (State.ERROR.equals(result.getState())) {
      log.error(result.getState().toString() + " : " + result.getResultMessage().toString());
      throw new TransformationException(result.getState().toString() + " : " + result.getResultMessage().toString());
    }
    return countOfFilesAfterProcess;
  }

  private boolean isEmpty(String s) {
    if (s == null) {
      return true;
    }
    if (s.length() == 0) {
      return true;
    }
    return false;
  }

  private void generateEvent(final KakaduService agent, final File file, final String cdmId, final PremisCsvRecord.OperationStatus status, final File targetDir) {
    final PremisCsvRecord record = new PremisCsvRecord(
        new Date(),
        getUtlilityName(),
        getUtilityVersion(),
        Operation.derivation_mc_creation,
        targetDir.getName(),
        agentName,
        agentVersion,
        agent.getCmd(),
        AGENT_ROLE,
        file,
        status,
        FORMAT_DESIGNATION_NAME,
        FORMAT_REGISTRY_KEY,
        PRESERVATION_LEVEL_VALUE); //todo kovalmil: aka hodnota formatDesignation pri ConvertToJpeg2000 ?
    cdm.addTransformationEvent(cdmId, record, null);
  }
}
