package com.logica.ndk.tm.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Map of parameters to used in WS interfaces.
 * 
 * @author Rudolf Daco
 */
@Deprecated
public class ParamMapOld implements Serializable {
	private static final long serialVersionUID = 5304946825306188714L;
	private List<ParamMapItem> items = new ArrayList<ParamMapItem>();

	public List<ParamMapItem> getItems() {
		return items;
	}

	public void setItems(List<ParamMapItem> items) {
		this.items = items;
	}

	public void addItem(ParamMapItem item) {
		items.add(item);
	}
}
