package org.apache.commons.id.uuid;

import org.apache.commons.id.uuid.state.ReadOnlyResourceStateImpl;

import junit.framework.TestCase;

/**
 * @author Rudolf Daco
 *
 */
public class VersionOneGeneratorTest extends TestCase {
  public void test() throws Exception {
    System.setProperty(ReadOnlyResourceStateImpl.CONFIG_FILENAME_KEY, "uuid1.state");    
    UUID timeUUID = UUID.timeUUID();
    System.out.println(timeUUID.toString());
    timeUUID = UUID.timeUUID();
    System.out.println(timeUUID.toString());
    timeUUID = UUID.timeUUID();
    System.out.println(timeUUID.toString());
  }
}
