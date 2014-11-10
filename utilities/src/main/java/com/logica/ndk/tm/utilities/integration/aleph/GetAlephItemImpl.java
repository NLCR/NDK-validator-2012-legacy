package com.logica.ndk.tm.utilities.integration.aleph;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import javax.annotation.Nullable;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.google.common.base.Function;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.integration.aleph.exception.AlephUnaccessibleException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.ItemNotFoundException;
import com.logica.ndk.tm.utilities.integration.aleph.exception.XMLParsingException;

/**
 * @author ondrusekl
 */
public class GetAlephItemImpl extends BaseGetAleph {

  private static final String ITEM_BARCODE_XPATH = "/item-data/item[barcode='%s']";

  public AlephItem getItem(final String barCode, String docNum, String libraryId, @Nullable String localBase) throws AlephUnaccessibleException, ItemNotFoundException, Exception {
    checkNotNull(docNum, "docNum must not be null");
    checkNotNull(barCode, "barCode must not be null");
    checkNotNull(libraryId, "libraryId must not be null");

    log.trace("getItem started");

    log.info("Getting aleph item by barCode: " + barCode + ", docNum: " + docNum + "libraryId:" + libraryId + ", localBase: " + localBase);

    // FIND QUERY //
    String url = constructItemDataUrl(docNum, libraryId, localBase);
    log.debug("url: " + url);
    String itemDataString = getAlephResult(url);
    log.debug("itemsData: " + itemDataString);

    try {
      Document document = DocumentHelper.parseText(itemDataString);
      Node item = document.selectSingleNode(format(ITEM_BARCODE_XPATH, barCode));
      if (item == null) {
        throw new ItemNotFoundException(format("No item found for barcode %s", barCode), ErrorCodes.GET_ALEPH_ITEM_NO_ITEM_FOUND);
      }
      log.trace("getItem finished");
      return NODE_TO_ALEPH_ITEM.apply((Element) item);
    }
    catch (DocumentException e) {
      throw new XMLParsingException("Exception during parse response", e);
    }
  }

  private static transient Function<Element, AlephItem> NODE_TO_ALEPH_ITEM = new Function<Element, AlephItem>() {
    @Override
    public AlephItem apply(Element element) {
      AlephItem item = new AlephItem();
      for (Object ob : element.elements()) {
        final Element child = (Element) ob;
        if ("rec-key".equals(child.getName())) {
          item.setRecKey(child.getText());
        }
        else if ("barcode".equals(child.getName())) {
          item.setBarCode(child.getText());
        }
        else if ("collection".equals(child.getName())) {
          item.setCollection(child.getText());
        }
        else if ("item-status".equals(child.getName())) {
          item.setItemStatus(child.getText());
        }
        else if ("note".equals(child.getName())) {
          item.setNote(child.getText());
        }
        else if ("call-no-1".equals(child.getName())) {
          item.setCallNo1(child.getText());
        }
        else if ("call-no-2".equals(child.getName())) {
          item.setCallNo2(child.getText());
        }
        else if ("description".equals(child.getName())) {
          item.setDescription(child.getText());
        }
        else if ("chronological-i".equals(child.getName())) {
          item.setChronologicalI(child.getText());
        }
        else if ("chronological-j".equals(child.getName())) {
          item.setChronologicalJ(child.getText());
        }
        else if ("chronological-k".equals(child.getName())) {
          item.setChronologicalK(child.getText());
        }
        else if ("enumeration-a".equals(child.getName())) {
          item.setEnumerationA(child.getText());
        }
        else if ("enumeration-b".equals(child.getName())) {
          item.setEnumerationB(child.getText());
        }
        else if ("enumeration-c".equals(child.getName())) {
          item.setEnumerationC(child.getText());
        }
        else if ("library".equals(child.getName())) {
          item.setLibrary(child.getText());
        }
        else if ("on-hold".equals(child.getName())) {
          item.setOnHold(child.getText());
        }
        else if ("requested".equals(child.getName())) {
          item.setRequested(child.getText());
        }
        else if ("expected".equals(child.getName())) {
          item.setExpected(child.getText());
        }
      }

      return item;
    }
  };

}
