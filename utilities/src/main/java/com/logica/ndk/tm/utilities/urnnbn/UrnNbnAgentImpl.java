package com.logica.ndk.tm.utilities.urnnbn;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.http.ResponseEntity;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
public class UrnNbnAgentImpl extends UrnNbnClient {

  private static volatile boolean IS_RUNNING = false;

  private static final int DEFAULT_SIZE = TmConfig.instance().getInt("utility.urnNbn.reservationSize");
  private static final String[] REGISTRAR_CODES = TmConfig.instance().getStringArray("utility.urnNbn.registrarCodes");
  private static final int THRESHOLD = TmConfig.instance().getInt("utility.urnNbn.unusedThreshold");
  private static final String SERVER_URL = TmConfig.instance().getString("utility.urnNbn.serverUrls.reserve");

  public String execute() {

    if (IS_RUNNING) {
      log.warn("UrnNbnAgent is steel running, executing skipped");
      return ResponseStatus.URN_NRN_AGENT_STEEL_RUNNING;
    }
    else {

      log.info("fillReservations started");

      try {
        IS_RUNNING = true;

        for (final String registrarCode : REGISTRAR_CODES) {
          final int unused = urnNbnDao.getReservedUnusedCount(registrarCode);
          final int threshold = THRESHOLD * (DEFAULT_SIZE / 100);
          log.info("Unused: " + unused + " ,Threshold: " + threshold);
          if (unused < threshold) {
            log.info("Unused URN:NBN ({}) in DB is lower than threshold ({})", unused, threshold);
            reserveUrnNbn(DEFAULT_SIZE - unused, registrarCode);
          }
        }

        log.info("fillReservations finished");
        return ResponseStatus.RESPONSE_OK;
      }
      finally {
        IS_RUNNING = false;
      }
    }
  }

  private String reserveUrnNbn(final Integer size, final String registrarCode) {
    checkNotNull(registrarCode, "registrarCode must not be null");
    checkArgument(!registrarCode.isEmpty(), "registrarCode must not be empty");

    log.info("reserveUrnNbn started. Requestted size is {} for {}", size, registrarCode);

    final String realUrl = new StringBuilder(SERVER_URL.replace("${sigla}", registrarCode.toLowerCase()))
        .append("?size=")
        .append(size != null ? size : DEFAULT_SIZE)
        .toString();

    log.info("URL for urnnbn reservations: " + realUrl);

//    final ResponseEntity<String> response = sendAndReceive(null, realUrl);
    //TODO toto udelat stejne jako v AssingUrnNbnImpl
    String responseBody = null;
    try {
      responseBody = getUrnNbn(realUrl);
      log.debug("Response: {}", responseBody);
    }
    catch (IOException e) { //request ended with error
      int statusCode;
      try {
        statusCode = urlConn.getResponseCode();
      }
      catch (IOException e2) {
        log.warn("Unable to retrieve error response code from connection.");
        throw new SystemException("Unable to retrieve error response code from connection.", ErrorCodes.URNNBN_RETRIEVING_ERROR);
      }
      String message = streamToString(urlConn.getErrorStream());

      log.info("Exception in Resolver request. Status code: " + statusCode + ", Message: " + message);
    }

    try {
      final Document document = DocumentHelper.parseText(responseBody);

      final Node errorNode = document.selectSingleNode("error");
      if (errorNode != null) { // handle error
        final Node messageNode = searchNode(document, "message");
        final String message = messageNode != null ? messageNode.getText() : null;
        final Node codeNode = searchNode(document, "code");
        final String code = codeNode != null ? codeNode.getText() : null;
        throw new SystemException(format("Operation failed with code %s (%s)", code, ErrorCodes.URNNBN_UPDATE_FAILED));
      }
      // handle response
      final List<Node> urnNbnNodes = searchNodes(document, "urnNbn");
      if (urnNbnNodes == null) {
        throw new SystemException("Response has wrong structure");
      }
      log.info("Urnnbn nodes found in Document:"+urnNbnNodes.size());

      final List<String> urnNbnValues = Lists.transform(urnNbnNodes, TRANSFORM);

      // save resever values into DB
      log.info("Reserved {} URN:NBNs, saving into DB", urnNbnValues.size());
      urnNbnDao.insertReserverdUrnNbnsIntoDb(registrarCode, urnNbnValues);

      log.info("reserveUrnNbn finished");
      return ResponseStatus.RESPONSE_OK;

    }
    catch (final DocumentException e) {
      throw new SystemException("Error during parsing response", ErrorCodes.WRONG_RESPONSE_STRUCTURE);
    }
  }
  private final Function<Node, String> TRANSFORM = new Function<Node, String>() {
    @Override
    public String apply(final Node node) {
      return node != null ? node.getText() : null;
    }
  };

}
