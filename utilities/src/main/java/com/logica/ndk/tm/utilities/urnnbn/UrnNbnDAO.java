package com.logica.ndk.tm.utilities.urnnbn;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.logica.ndk.tm.utilities.AbstractDAO;

/**
 * Table:<br>
 * <code>
 * CREATE table urn_nbn (
 *      id bigint IDENTITY(1,1)PRIMARY KEY CLUSTERED,
 *      registrar varchar(10),
 *      value varchar(50) unique,
 *      cdm_id varchar(50),
 *      assigned_datetime datetime
 *      );
 *  </code>
 * 
 * @author ondrusekl
 */
public class UrnNbnDAO extends AbstractDAO {

  /**
   * Insert transactionaly resedved data into utilities database.
   * 
   * @param registrar
   *          code of registrar
   * @param urnNbnValues
   *          reserved values
   * @return count of updated rows
   */
  public int insertReserverdUrnNbnsIntoDb(final String registrar, final List<String> urnNbnValues) {
    checkNotNull(urnNbnValues, "urnNbnValues must not be null");

    txTemplate.setReadOnly(false);
    return txTemplate.execute(new TransactionCallback<Integer>() {
      @Override
      public Integer doInTransaction(final TransactionStatus arg0) {
        int count = 0;
        for (final String urnNbnValue : urnNbnValues) {
          count += jdbcTemplate.update("INSERT INTO urn_nbn (registrar, value) VALUES (?,?)", registrar.toLowerCase(), urnNbnValue);
        }
        return count;
      }
    });
  }

  /**
   * Get for cout of unused reserved URN:NBN id database.
   * 
   * @param registrar
   *          code of registrar
   * @return count of URN:NBN
   */
  public int getReservedUnusedCount(final String registrar) {

    txTemplate.setReadOnly(true);
    return txTemplate.execute(new TransactionCallback<Integer>() {
      @Override
      public Integer doInTransaction(final TransactionStatus stat) {
        return jdbcTemplate.queryForInt("SELECT count(*) FROM urn_nbn WHERE cdm_id IS NULL AND registrar = ?", registrar.toLowerCase());
      }
    });
  }

  public String assignUrnNbnFromDb(final String registrar, final String cdmId) throws DataAccessException {
    log.info("Method assignUrnNbnFromDb started. Registrar:" + registrar);
    txTemplate.setReadOnly(false);
    return txTemplate.execute(new TransactionCallback<String>() {
      @Override
      public String doInTransaction(final TransactionStatus stat) {
        final String value = jdbcTemplate.queryForObject("SELECT TOP 1 value FROM urn_nbn WHERE cdm_id IS NULL AND registrar = ?", String.class, registrar.toLowerCase());
        log.debug("!URN from database = " + value);
        jdbcTemplate.update("UPDATE urn_nbn SET cdm_id = ?, assigned_datetime = ? WHERE value = ?", cdmId, new Timestamp(System.currentTimeMillis()), value);
        return value;
      }
    });
  }
}
