/**
 * 
 */
package com.logica.ndk.tm.repair;

import org.dom4j.Node;

import com.google.common.base.Preconditions;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * Extrats element from main METS by given xpath expression
 * 
 * @author kovalcikm
 * 
 */
public class ExtractFromMetsImpl extends AbstractUtility {

	public String execute(String cdmId, String xpathExpr) {
		Preconditions.checkNotNull(cdmId);
		Preconditions.checkNotNull(xpathExpr);

		log.info("ExtractFromMetsImpl started. cdmId: " + cdmId);
		log.info("Xpath expression: " + xpathExpr);

		CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();
		Node node = cdmMetsHelper.getNodeFromMets(xpathExpr, cdm, cdmId);

		log.info("Found value: " + node.getText());
		return node.getText();
	}

	public static void main(String[] args) {
		new ExtractFromMetsImpl().execute("27c55a90-720e-11e2-bf7c-5ef3fc9ae867", "//mods:mods[@ID='" + "MODS_TITLE_0001" + "']/mods:identifier[@type='uuid']");
		new ExtractFromMetsImpl().execute("27c55a90-720e-11e2-bf7c-5ef3fc9ae867", "//mods:mods[@ID='" + "MODS_VOLUME_0001" + "']/mods:identifier[@type='uuid']");
		new ExtractFromMetsImpl().execute("27c55a90-720e-11e2-bf7c-5ef3fc9ae867", "//mods:mods[@ID='" + "MODS_ISSUE_0001" + "']/mods:identifier[@type='uuid']");

	}

}
