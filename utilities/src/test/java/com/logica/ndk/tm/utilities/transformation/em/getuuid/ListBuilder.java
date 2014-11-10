package com.logica.ndk.tm.utilities.transformation.em.getuuid;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krchnacekm
 */
public class ListBuilder<T> {
     private List<T> list = new ArrayList<T>();
     
     public ListBuilder() {
         
     }
     
     public ListBuilder add(T listItem) {
         list.add(listItem);
         return this;
     }
     
     public List<T> build() {
         return list;
     }
}
