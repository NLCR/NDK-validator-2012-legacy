/**
 * 
 */
package com.logica.ndk.commons.utils.id;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

/**
 * @author kovalcikm
 */
public class ISSNUtilsTest {

  @Test
  public void testNormalize() {
    String issn = "03004414aa";
    String normalizedISBN = ISSNUtils.normalize(issn);
    assertThat(normalizedISBN)
        .isNotNull()
        .isEqualTo("0300-4414");
    
    issn = "03004414";
    normalizedISBN = ISSNUtils.normalize(issn);
    assertThat(normalizedISBN)
        .isNotNull()
        .isEqualTo("0300-4414");
    
    issn = "aa03004414";
    normalizedISBN = ISSNUtils.normalize(issn);
    assertThat(normalizedISBN)
        .isNotNull()
        .isEqualTo("0300-4414");
    
    issn = "03004414";
    normalizedISBN = ISSNUtils.normalize(issn);
    assertThat(normalizedISBN)
        .isNotNull()
        .isEqualTo("0300-4414");
  }

  @Test
  public void testValidate() {
    String issn = "0388-595X";
    assertThat(ISSNUtils.validate(issn))
        .isTrue();

    issn = "0388-595x";
    assertThat(ISSNUtils.validate(issn))
        .isTrue();

    issn = "0388-5954";
    assertThat(ISSNUtils.validate(issn))
        .isFalse();

    issn = "0388595x";
    assertThat(ISSNUtils.validate(issn))
        .isFalse();
  }
}
