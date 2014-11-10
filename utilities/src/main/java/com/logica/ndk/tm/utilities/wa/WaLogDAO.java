package com.logica.ndk.tm.utilities.wa;

import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.logica.ndk.tm.utilities.AbstractDAO;

/**
 * Table:<br>
 * <code>
 *  CREATE table wa_log (
 *    id bigint IDENTITY(1,1)PRIMARY KEY CLUSTERED,
 *    cdm_id varchar(50),
 *    files_in_wa int,
 *    created datetime
 *  );
 *  </code>
 * 
 * @author Rudolf Daco
 */
public class WaLogDAO extends AbstractDAO {

  /**
   * Insert transactionaly data into utilities database.
   */
  public void insert(final WaLogEvent waLog) {
    checkNotNull(waLog, "waLog must not be null");

    txTemplate.setReadOnly(false);
    txTemplate.execute(new TransactionCallback<Integer>() {
      @Override
      public Integer doInTransaction(final TransactionStatus arg0) {
        return jdbcTemplate.update("INSERT INTO wa_log (cdm_id, files_in_wa, created) VALUES (?,?,?)", waLog.getCdmId(), waLog.getFilesInWa(), waLog.getCreated());
      }
    });
  }
}
