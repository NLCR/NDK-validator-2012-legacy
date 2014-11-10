package com.logica.ndk.tm.master.ws.security;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

public class ServiceKeystorePasswordCallback implements CallbackHandler {

  private static Map<String, String> passwords;

  public ServiceKeystorePasswordCallback() {
  }

  public ServiceKeystorePasswordCallback(Map<String, String> passwords) {
    ServiceKeystorePasswordCallback.passwords = passwords;
  }

  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    for (int i = 0; i < callbacks.length; i++) {
      WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
      String pass = passwords.get(pc.getIdentifier());
      if (pass != null) {
        pc.setPassword(pass);
        return;
      }
    }
  }
}
