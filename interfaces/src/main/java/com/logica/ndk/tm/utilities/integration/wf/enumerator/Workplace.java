/**
 * 
 */
package com.logica.ndk.tm.utilities.integration.wf.enumerator;

/**
 * @author kovalcikm
 */
public class Workplace extends Enumerator {
  Activity activity;
  Enumerator locality;

  public Workplace() {
  }

  public Activity getActivity() {
    return activity;
  }

  public void setActivity(Activity activity) {
    this.activity = activity;
  }

  public Enumerator getLocality() {
    return locality;
  }
  
  public void setLocality(Enumerator locality) {
    this.locality = locality;
  }

}
