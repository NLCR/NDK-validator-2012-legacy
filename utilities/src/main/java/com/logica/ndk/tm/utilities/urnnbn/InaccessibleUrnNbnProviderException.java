package com.logica.ndk.tm.utilities.urnnbn;

import org.springframework.web.client.RestClientException;

public class InaccessibleUrnNbnProviderException extends RestClientException {  

  public InaccessibleUrnNbnProviderException(String message) {
    super(message);
  } 

  public InaccessibleUrnNbnProviderException(String message, Throwable cause) {
    super(message, cause);
  }

}
