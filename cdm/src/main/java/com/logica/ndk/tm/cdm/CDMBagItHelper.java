package com.logica.ndk.tm.cdm;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;

import java.io.File;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class CDMBagItHelper {

  private final BagFactory bagFactory = new BagFactory();
  private final DefaultCompleter defaultCompleter = new DefaultCompleter(bagFactory);
  public static final String CDM_PROPERTIES_NAME = "cdmInfo.txt";

  public void createBagInPlace(File dir, List<String> ignoreDirs) {
    updateBagInPlace(dir, ignoreDirs);
  }

  public void updateBagInPlace(File dir, List<String> ignoreDirs) {
    // vzdy prepocitaj MD5 v manifest-md5.txt a tagmanifest-md5.txt
    defaultCompleter.setClearExistingPayloadManifests(true);
    defaultCompleter.setClearExistingTagManifests(true);
    PreBag preBag = bagFactory.createPreBag(dir);
    if (ignoreDirs != null) {
      preBag.setIgnoreAdditionalDirectories(ignoreDirs);
    }
    final Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, true, false, defaultCompleter);
    IOUtils.closeQuietly(bag);
  }

  public String getDataDirectory() {
    return bagFactory.getBagConstants().getDataDirectory();
  }

  public List<String> verifyBag(File dir) {
    CompleteVerifierImpl completeVerifier = new CompleteVerifierImpl();
    ParallelManifestChecksumVerifier checksumVerifier = new ParallelManifestChecksumVerifier();
    ValidVerifierImpl verifier = new ValidVerifierImpl(completeVerifier, checksumVerifier);
    final Bag bag = bagFactory.createBag(dir);
    try {
      SimpleResult result = verifier.verify(bag);
      return (result == null || result.getMessages().isEmpty()) ? null : result.getMessages();
    }
    finally {
      IOUtils.closeQuietly(bag);
    }
  }

}
