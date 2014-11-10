package com.logica.ndk.tm.log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.logica.ndk.tm.log.LogEvent;
import com.logica.ndk.tm.utilities.AbstractDAO;

/**
 * Table:<br>
 * <code>
 * CREATE table log (
 *    id bigint IDENTITY(1,1)PRIMARY KEY CLUSTERED,
 *    processInstanceId varchar(10),
 *    nodeId varchar(50),
 *    eventType varchar(20),
 *    utilityName varchar(100),
 *    message varchar(250),
 *    exceptionWasThrown bit,
 *    duration bigint,
 *    created datetime
 *      );
 *  </code>
 */
public class LogDAO extends AbstractDAO {
  private static final String ALL_COLUMNS = "id,processInstanceId,nodeId,eventType,utilityName,message,exceptionWasThrown,duration,created";

  public void insert(final LogEvent logEvent) {
    txTemplate.setReadOnly(false);
    txTemplate.execute(new TransactionCallback<Integer>() {
      @Override
      public Integer doInTransaction(final TransactionStatus arg0) {
        return jdbcTemplate.update(
            "INSERT INTO log (" +
                "processInstanceId, " +
                "nodeId, " +
                "eventType, " +
                "utilityName, " +
                "message, " +
                "exceptionWasThrown, " +
                "duration, " +
                "created) VALUES (?,?,?,?,?,?,?,?)",
            logEvent.getProcessInstanceId(),
            logEvent.getNodeId(),
            logEvent.getEventType(),
            logEvent.getUtilityName(),
            logEvent.getMessage(),
            logEvent.isExceptionWasThrown(),
            logEvent.getDuration(),
            logEvent.getCreated());
      }
    });
  }

  public List<LogEvent> find(final String processInstanceId) {
    txTemplate.setReadOnly(true);
    return txTemplate.execute(new TransactionCallback<List<LogEvent>>() {
      @Override
      public List<LogEvent> doInTransaction(final TransactionStatus stat) {
        return jdbcTemplate.query("select " + ALL_COLUMNS + " from log where processInstanceId = ? order by id", new Object[] { processInstanceId }, getRowMapper());
      }
    });
  }
  
  public List<LogEvent> findActiveUtilities() {
    txTemplate.setReadOnly(true);
    return txTemplate.execute(new TransactionCallback<List<LogEvent>>() {
      @Override
      public List<LogEvent> doInTransaction(final TransactionStatus stat) {
        return jdbcTemplate.query("select " + ALL_COLUMNS + " from log where id in((select MAX (id) from log group by processInstanceId)) and eventType = 'before' order by nodeId, processInstanceId", getRowMapper());
      }
    });
  }
    

  private RowMapper<LogEvent> getRowMapper() {
    return new RowMapper<LogEvent>() {
      public LogEvent mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
        LogEvent logEvent = new LogEvent();
        logEvent.setId(rs.getLong("id"));
        logEvent.setProcessInstanceId(rs.getString("processInstanceId"));
        logEvent.setNodeId(rs.getString("nodeId"));
        logEvent.setEventType(rs.getString("eventType"));
        logEvent.setUtilityName(rs.getString("utilityName"));
        logEvent.setMessage(rs.getString("message"));
        logEvent.setExceptionWasThrown(rs.getBoolean("exceptionWasThrown"));
        logEvent.setDuration(rs.getLong("duration"));
        logEvent.setCreated(rs.getTimestamp("created"));
        return logEvent;
      }
    };
  }
}
