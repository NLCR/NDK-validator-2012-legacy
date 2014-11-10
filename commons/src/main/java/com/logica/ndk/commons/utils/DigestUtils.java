/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.logica.ndk.commons.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Miscellaneous methods for calculating digests.
 * <p>
 * Mainly for internal use within the framework; consider <a href="http://commons.apache.org/codec/">Apache Commons
 * Codec</a> for a more comprehensive suite of digest utilities.
 * 
 * @author Arjen Poutsma
 * @since 3.0
 * @see org.apache.commons.codec.digest.DigestUtils
 */
public abstract class DigestUtils {

  private static final String MD5_ALGORITHM_NAME = "MD5";

  private static final char[] HEX_CHARS =
  { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  /**
   * Calculate the MD5 digest of the given bytes.
   * 
   * @param bytes
   *          the bytes to calculate the digest over
   * @return the digest
   */
  public static byte[] md5Digest(final byte[] bytes) {
    return digest(MD5_ALGORITHM_NAME, bytes);
  }

  /**
   * Return a hexadecimal string representation of the MD5 digest of the given
   * bytes.
   * 
   * @param bytes
   *          the bytes to calculate the digest over
   * @return a hexadecimal digest string
   */
  public static String md5DigestAsHex(final byte[] bytes) {
    return digestAsHexString(MD5_ALGORITHM_NAME, bytes);
  }

  /**
   * Append a hexadecimal string representation of the MD5 digest of the given
   * bytes to the given {@link StringBuilder}.
   * 
   * @param bytes
   *          the bytes to calculate the digest over
   * @param builder
   *          the string builder to append the digest to
   * @return the given string builder
   */
  public static StringBuilder appendMd5DigestAsHex(final byte[] bytes, final StringBuilder builder) {
    return appendDigestAsHex(MD5_ALGORITHM_NAME, bytes, builder);
  }

  /**
   * Creates a new {@link MessageDigest} with the given algorithm. Necessary
   * because {@code MessageDigest} is not thread-safe.
   */
  private static MessageDigest getDigest(final String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    }
    catch (final NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", ex);
    }
  }

  private static byte[] digest(final String algorithm, final byte[] bytes) {
    return getDigest(algorithm).digest(bytes);
  }

  private static String digestAsHexString(final String algorithm, final byte[] bytes) {
    final char[] hexDigest = digestAsHexChars(algorithm, bytes);
    return new String(hexDigest);
  }

  private static StringBuilder appendDigestAsHex(final String algorithm, final byte[] bytes, final StringBuilder builder) {
    final char[] hexDigest = digestAsHexChars(algorithm, bytes);
    return builder.append(hexDigest);
  }

  private static char[] digestAsHexChars(final String algorithm, final byte[] bytes) {
    final byte[] digest = digest(algorithm, bytes);
    return encodeHex(digest);
  }

  private static char[] encodeHex(final byte[] bytes) {
    final char chars[] = new char[32];
    for (int i = 0; i < chars.length; i = i + 2) {
      final byte b = bytes[i / 2];
      chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
      chars[i + 1] = HEX_CHARS[b & 0xf];
    }
    return chars;
  }

}
