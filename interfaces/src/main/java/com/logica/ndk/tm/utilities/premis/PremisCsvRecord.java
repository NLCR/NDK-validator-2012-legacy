package com.logica.ndk.tm.utilities.premis;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.logica.ndk.commons.utils.DateUtils;

/**
 * CSV record for one event.
 * 
 * @author ondrusekl
 */
public class PremisCsvRecord {

  public static final String[] HEADER = new String[] { "dateTime", "utility", "utilityVersion", "operation", "eventDir", "agent", "agentVersion", "agentNote", "agentRole", "file", "status", "formatDesignationName", "formatRegistryKey", "preservationLevelValue" };

  private Date dateTime;
  private String utility;
  private String utilityVersion;
  private Operation operation;
  private String eventDir;
  private String agent;
  private String agentVersion;
  private String agentNote;
  private String agentRole;
  private File file;
  private OperationStatus status;
  private String formatDesignationName;
  private String formatRegistryKey;
  private String preservationLevelValue;
  private String id;
  private String eventId;

  public PremisCsvRecord(final Date dateTime, final String utility, final String utilityVersion, final Operation operation, final String eventDir, final String agent, final String agentVersion, final String agentNote, final String agentRole, final File file, final OperationStatus status,
      final String formatDesignationName,
      final String formatRegistryKey, final String preservationLevelValue) {
    final String fileName = file.getName();

    this.id = IdPremisRecordPrefixEnum.getPrefixByEventDir(eventDir) + (fileName.contains(".") ? fileName.substring(0, fileName.indexOf(".")) : fileName);
    this.dateTime = dateTime;
    this.utility = utility;
    this.utilityVersion = utilityVersion;
    this.operation = operation;
    this.eventDir = eventDir;
    this.agent = agent;
    this.agentVersion = agentVersion;
    this.agentNote = agentNote;
    this.agentRole = agentRole;
    this.file = file;
    this.status = status;
    this.formatDesignationName = formatDesignationName;
    this.formatRegistryKey = formatRegistryKey;
    this.preservationLevelValue = preservationLevelValue;
  }

  // pouziva sa pre WA, kde potrebujeme zadat id explicitne aby sme nemuseli menit enumerator
  public PremisCsvRecord(final Date dateTime, final String utility, final String utilityVersion, final Operation operation, final String eventDir, final String agent, final String agentVersion, final String agentNote, final String agentRole, final File file, final OperationStatus status,
      final String formatDesignationName,
      final String formatRegistryKey, final String preservationLevelValue, String id) {
    final String fileName = file.getName();

    this.id = id;
    this.dateTime = dateTime;
    this.utility = utility;
    this.utilityVersion = utilityVersion;
    this.operation = operation;
    this.eventDir = eventDir;
    this.agent = agent;
    this.agentVersion = agentVersion;
    this.agentNote = agentNote;
    this.agentRole = agentRole;
    this.file = file;
    this.status = status;
    this.formatDesignationName = formatDesignationName;
    this.formatRegistryKey = formatRegistryKey;
    this.preservationLevelValue = preservationLevelValue;
  }

  public String[] asCsvRecord() {
    final String fileRelativePath = getRelativePath();
    return new String[] { DateUtils.toXmlDateTime(dateTime).toXMLFormat(), utility, utilityVersion, operation.name(), eventDir, agent, agentVersion, agentNote, agentRole, fileRelativePath, status.name(), formatDesignationName, formatRegistryKey, preservationLevelValue };
  }

  public String getRelativePath() {
    File cdmRootDir = file.getParentFile();
    while (cdmRootDir != null) {
      if (cdmRootDir.getName().startsWith("CDM_")) {
        break;
      }
      cdmRootDir = cdmRootDir.getParentFile();
    }

    final String fileRelativePath = file.getAbsolutePath().replace(cdmRootDir.getAbsolutePath(), "");
    return fileRelativePath;
  }

  public String getId() {
    return id;
  }

  public Date getDateTime() {
    return dateTime;
  }

  public void setDateTime(final Date dateTime) {
    this.dateTime = dateTime;
  }

  public String getUtility() {
    return utility;
  }

  public void setUtility(final String utility) {
    this.utility = utility;
  }

  public String getUtilityVersion() {
    return utilityVersion;
  }

  public void setUtilityVersion(final String utilityVersion) {
    this.utilityVersion = utilityVersion;
  }

  public Operation getOperation() {
    return operation;
  }

  public void setOperation(final Operation operation) {
    this.operation = operation;
  }

  public String getEventDir() {
    return eventDir;
  }

  public void setEventDir(final String eventDir) {
    this.eventDir = eventDir;
  }

  public String getAgent() {
    return agent;
  }

  public void setAgent(final String agent) {
    this.agent = agent;
  }

  public String getAgentVersion() {
    return agentVersion;
  }

  public void setAgentVersion(final String agentVersion) {
    this.agentVersion = agentVersion;
  }

  public String getAgentNote() {
    return agentNote;
  }

  public void setAgentNote(String agentNote) {
    this.agentNote = agentNote;
  }

  public File getFile() {
    return file;
  }

  public String getAgentRole() {
    return agentRole;
  }

  public void setAgentRole(String agentRole) {
    this.agentRole = agentRole;
  }

  public void setFile(final File file) {
    this.file = file;
  }

  public OperationStatus getStatus() {
    return status;
  }

  public void setStatus(final OperationStatus status) {
    this.status = status;
  }

  public String getFormatDesignationName() {
    return formatDesignationName;
  }

  public void setFormatDesignationName(String formatDesignationName) {
    this.formatDesignationName = formatDesignationName;
  }

  public String getFormatRegistryKey() {
    return formatRegistryKey;
  }

  public void setFormatRegistryKey(String formatRegistryKey) {
    this.formatRegistryKey = formatRegistryKey;
  }

  public String getPreservationLevelValue() {
    return preservationLevelValue;
  }

  public void setPreservationLevelValue(String preservationLevelValue) {
    this.preservationLevelValue = preservationLevelValue;
  }

  public enum Operation {
    create_config,
    convert_image, rsync,
    capture_digitalization {
      public String toString() {
        return "capture/digitalization";
      }
    },
    capture_xml_creation {
      public String toString() {
        return "capture/XML_creation";
      }
    },
    capture_txt_creation {
      public String toString() {
        return "capture/TXT_creation";
      }
    },
    migration_mc_creation {
      public String toString() {
        return "migration/MC_creation";
      }
    },
    migration_warc_creation {
      public String toString() {
        return "migration/WARC_creation";
      }
    },
    migration_arc_creation {
      public String toString() {
        return "migration/ARC_creation";
      }
    },
    migration_flat_creation {
      public String toString() {
        return "migration/convert_image";
      }
    },
    creation_arc_creation {
      public String toString() {
        return "creation/ARC_creation";
      }
    },
    creation_warc_creation {
      public String toString() {
        return "creation/WARC_creation";
      }
    },
    derivation_mc_creation {
      public String toString() {
        return "derivation/MC_creation";
      }
    },
    derivation_uc_creation {
      public String toString() {
        return "derivation/UC_creation";
      }
    },
    deletion_ps_deletion {
      public String toString() {
        return "deletion/PS_deletion";
      }
    },
    deletion_tiff_deletion {
      public String toString() {
        return "deletion/TIFF_deletion";
      }
    },
    deletion_arc_deletion {
      public String toString() {
        return "deletion/ARC_deletion";
      }
    },
    derivation_postprocessing_creation {
      public String toString() {
        return "derivation/POSTPROCESSING_creation";
      }
    };

    public static Operation getEnumFromString(String param) {
      for (Operation operation : values()) {
        if (operation.toString().equals(param)) {
          return operation;
        }
      }
      return capture_digitalization;
    }

  }

  public enum OperationStatus {
    OK,
    FAILED
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((agent == null) ? 0 : agent.hashCode());
    result = prime * result + ((agentVersion == null) ? 0 : agentVersion.hashCode());
    result = prime * result + ((agentNote == null) ? 0 : agentNote.hashCode());
    result = prime * result + ((agentRole == null) ? 0 : agentRole.hashCode());
    result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
    result = prime * result + ((eventDir == null) ? 0 : eventDir.hashCode());
    result = prime * result + ((file == null) ? 0 : file.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((operation == null) ? 0 : operation.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
    result = prime * result + ((utility == null) ? 0 : utility.hashCode());
    result = prime * result + ((utilityVersion == null) ? 0 : utilityVersion.hashCode());
    result = prime * result + ((formatDesignationName == null) ? 0 : formatDesignationName.hashCode());
    result = prime * result + ((formatRegistryKey == null) ? 0 : formatRegistryKey.hashCode());
    result = prime * result + ((preservationLevelValue == null) ? 0 : preservationLevelValue.hashCode());
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
    final PremisCsvRecord other = (PremisCsvRecord) obj;
    if (agent == null) {
      if (other.agent != null)
        return false;
    }
    else if (!agent.equals(other.agent))
      return false;
    if (agentVersion == null) {
      if (other.agentVersion != null)
        return false;
    }
    else if (!agentVersion.equals(other.agentVersion))
      return false;

    if (agentNote == null) {
      if (other.agentNote != null)
        return false;
    }
    else if (!agentNote.equals(other.agentNote))
      return false;

    if (agentRole == null) {
      if (other.agentRole != null)
        return false;
    }
    else if (!agentRole.equals(other.agentRole))
      return false;
    if (dateTime == null) {
      if (other.dateTime != null)
        return false;
    }

    if (dateTime == null) {
      if (other.dateTime != null)
        return false;
    }
    else if (!dateTime.equals(other.dateTime))
      return false;
    if (eventDir == null) {
      if (other.eventDir != null)
        return false;
    }
    else if (!eventDir.equals(other.eventDir))
      return false;
    if (file == null) {
      if (other.file != null)
        return false;
    }
    else if (!file.equals(other.file))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (operation == null) {
      if (other.operation != null)
        return false;
    }
    else if (!operation.equals(other.operation))
      return false;
    if (status != other.status)
      return false;
    if (utility == null) {
      if (other.utility != null)
        return false;
    }
    else if (!utility.equals(other.utility))
      return false;
    if (utilityVersion == null) {
      if (other.utilityVersion != null)
        return false;
    }
    else if (!utilityVersion.equals(other.utilityVersion))
      return false;

    else if (!formatDesignationName.equals(other.formatDesignationName))
      return false;

    else if (!formatRegistryKey.equals(other.formatRegistryKey))
      return false;

    else if (!preservationLevelValue.equals(other.preservationLevelValue))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return String.format("PremisCsvRecord [id=%s, dateTime=%s, utility=%s, utilityVersion=%s, operation=%s, eventDir=%s, agent=%s, agentVersion=%s, file=%s, status=%s, formatDesignationName=%s, formatRegistryKey=%s, preservationLevelValue=%s]", id, dateTime, utility, utilityVersion, operation,
        eventDir, agent, agentVersion, agentNote,
        file, status,
        formatDesignationName, formatRegistryKey, preservationLevelValue);
  }

  public enum IdPremisRecordPrefixEnum {
    TXT("TXT_"), postprocessingData("PS_"), masterCopy("MC_"), ALTO("XML_"), flatData("PS_"), userCopy("UC_"), TH("TH_"), preview("PRV_"), masterCopy_TIFF("masterCopy_TIFF_"), WARC("WARC_"), ARC("ARC_"), rawData("RAW_"), originalData("ORIGINAL_");

    private final String prefix;

    public static String getPrefixByEventDir(final String eventDir) {
      for (final IdPremisRecordPrefixEnum value : values()) {

        if (value.name().equals(eventDir)) {
          return value.prefix;
        }
      }
      throw new IllegalArgumentException("IdPremisRecordPrefixEnum not definied for dirLabel: " + eventDir);
    }

    private IdPremisRecordPrefixEnum(String prefix) {
      this.prefix = prefix;
    }
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

}
