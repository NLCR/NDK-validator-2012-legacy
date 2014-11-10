package com.logica.ndk.tm.utilities.premis;

import gov.loc.standards.premis.v2.AgentComplexType;
import gov.loc.standards.premis.v2.EventComplexType;
import gov.loc.standards.premis.v2.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ondrusekl
 */
public class PremisEntity<T> {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  private final T object;

  public PremisEntity(final T object) {
    this.object = object;
  }

  public T getObject() {
    return object;
  }

  public String getId() {
    if (object instanceof File) {
      final File file = (File) object;
      try {
        return file.getObjectIdentifier().get(0).getObjectIdentifierValue();
      }
      catch (final Exception e) {
        return null;
      }
    }
    else if (object instanceof EventComplexType) {
      final EventComplexType event = (EventComplexType) object;
      try {
        return event.getEventIdentifier().getEventIdentifierValue();
      }
      catch (final Exception e) {
        return null;
      }
    }
    else if (object instanceof AgentComplexType) {
      final AgentComplexType agent = (AgentComplexType) object;
      try {
        return agent.getAgentName().get(0);
      }
      catch (final Exception e) {
        return null;
      }
    }
    else {
      log.warn("Class {} not accepted", object.getClass());
      return null;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final PremisEntity<?> other = ((PremisEntity<?>) obj);
    if (getId() == null) {
      if (other.getId() != null)
        return false;
    }
    else if (!getId().equals(other.getId()))
      return false;
    return true;
  }

}
