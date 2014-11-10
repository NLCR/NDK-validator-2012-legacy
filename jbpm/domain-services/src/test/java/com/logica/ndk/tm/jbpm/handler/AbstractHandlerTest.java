package com.logica.ndk.tm.jbpm.handler;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import com.logica.ndk.commons.utils.test.TestUtils;

@Ignore
public class AbstractHandlerTest {

  private static AbstractHandler CLIENT_HANDLER;

  private static JmsTemplate JMS_TEMPLATE_MOCK = mock(JmsTemplate.class);

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    CLIENT_HANDLER = TestUtils.invokeConstructor(AbstractHandler.class, new MockSpringContextHelper());
    TestUtils.setField(CLIENT_HANDLER, "jmsTemplate", JMS_TEMPLATE_MOCK);
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    reset(JMS_TEMPLATE_MOCK);
  }

  @Ignore
  public void testGetSyncClient() {
    Bean bean = CLIENT_HANDLER.getClient(Bean.class);

    assertThat(bean).isNotNull();
    assertThat(bean).isInstanceOf(TestBean.class);
  }

  @Ignore
  public void testGetAsyncClient() {
    Assert.fail("Not implemented yet");
  }
}
