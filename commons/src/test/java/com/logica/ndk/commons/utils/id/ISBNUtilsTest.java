package com.logica.ndk.commons.utils.id;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;


public class ISBNUtilsTest {

  @Test
  public void testNormalize() {
    String isbn = "80-7051-079-X (asd.)";
    String normalizedISBN = ISBNUtils.normalize(isbn);
    assertThat(normalizedISBN)
        .isNotNull()
        .isEqualTo("80-7051-079-X");

    isbn = "isbn:978-80-87087-28-X (váz.)";
    assertThat(ISBNUtils.normalize(isbn))
        .isNotNull()
        .isEqualTo("978-80-87087-28-X");
  }

  @Ignore
  public void testValidate() {
    String isbn = "978-80-87087-28-2 (váz.)";
    assertThat(ISBNUtils.validate(isbn))
        .isTrue();

    isbn = "978-80-87087-28-2-1 (váz.)";
    assertThat(ISBNUtils.validate(isbn))
        .isFalse();

    isbn = "978-80-87387-28-2 (váz.)";
    assertThat(ISBNUtils.validate(isbn))
        .isFalse();

    isbn = "ISBN 80-204-0105-X";
    assertThat(ISBNUtils.validate(isbn))
        .isTrue();

    isbn = "ISBN 80-214-0105-X";
    assertThat(ISBNUtils.validate(isbn))
        .isFalse();
    
    isbn = "80-7051-079-X";
    assertThat(ISBNUtils.validate(isbn)).isTrue();
  }

}
