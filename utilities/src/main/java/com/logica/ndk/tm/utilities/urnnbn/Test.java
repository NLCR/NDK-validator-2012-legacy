package com.logica.ndk.tm.utilities.urnnbn;

import org.springframework.util.DigestUtils;

public class Test {
  
  public static void main(final String[] args) {
    System.out.println(DigestUtils.md5DigestAsHex("Libor".getBytes()));
    System.out.println(org.apache.commons.codec.digest.DigestUtils.md5Hex("Libor"));
  }

}
