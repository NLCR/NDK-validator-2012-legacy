package com.logica.ndk.tm.utilities.solr;

import org.junit.Ignore;
import org.junit.Test;

import com.logica.ndk.tm.utilities.solr.SolrClient;

public class SolrClientIT {
  @Ignore
  public void indexFile() {
    new SolrClient().indexFile("doc01", "c:\\NDK\\data_test\\_wa\\warc\\dump_000000_heritrix\\urn-uuid-000ad0ce-cf12-4402-a6f0-33067f13dab1.data");
  }
}