package com.logica.ndk.tm.utilities;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author ondrusekl
 */
public abstract class AbstractDAO {

  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  protected JdbcTemplate jdbcTemplate;
  protected TransactionTemplate txTemplate;

  public JdbcTemplate getJdbcTemplate() {
    return checkNotNull(jdbcTemplate, "jdbcTemplate must not be null");
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public TransactionTemplate getTxTemplate() {
    return checkNotNull(txTemplate, "txTemplate must not be null");
  }

  public void setTxTemplate(TransactionTemplate txTemplate) {
    this.txTemplate = txTemplate;
  }

}
