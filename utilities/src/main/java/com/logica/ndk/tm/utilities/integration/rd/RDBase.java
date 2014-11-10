package com.logica.ndk.tm.utilities.integration.rd;

import static java.lang.String.format;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.rd.exception.BadDigitizationStateException;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistry;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationRegistryService;
import com.logica.ndk.tm.utilities.integration.rd.ws.client.DigitizationState;

/**
 * Parent for all Digitization regiser operations.
 * 
 * @author ondrusekl
 */
public abstract class RDBase extends AbstractUtility {

  protected DigitizationRegistry registry;

  public void initConnection() {
    try {
      registry = new DigitizationRegistryService(new URL(TmConfig.instance().getString("rd.wsdlLocation")),
          new QName(TmConfig.instance().getString("rd.qnameUri"), TmConfig.instance().getString("rd.qnameLocalService"))).
          getDigitizationRegistryPort();

      String userName = TmConfig.instance().getString("rd.username");

      if (userName != null && !userName.isEmpty()) {
        BindingProvider bp = (BindingProvider) registry;
        Map<String, Object> rc = bp.getRequestContext();
        rc.put(BindingProvider.USERNAME_PROPERTY, userName);
        rc.put(BindingProvider.PASSWORD_PROPERTY, TmConfig.instance().getString("rd.password"));
      }
    }
    catch (Exception e) {
      throw new SystemException("Cannot initialize conenction to registry", e);
    }

  }

  protected DigitizationState toDigitizationState(String state) {
    if (state == null) {
      return null;
    }

    for (DigitizationState digitizationState : DigitizationState.values()) {
      if (digitizationState.name().equalsIgnoreCase(state)) {
        return digitizationState;
      }
    }
    throw new BadDigitizationStateException(format("State %s is not valid state string for toDigitizationState. Choices are %s", state, Arrays.asList(DigitizationState.values()), ErrorCodes.RD_BASE_NOT_VALID_STATE));
  }

}
