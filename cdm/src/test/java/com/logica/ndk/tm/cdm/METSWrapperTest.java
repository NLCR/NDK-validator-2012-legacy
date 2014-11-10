package com.logica.ndk.tm.cdm;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.apsr.mtk.base.Agent;
import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.FileGrp;
import au.edu.apsr.mtk.base.FileSec;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.MetsHdr;
import au.edu.apsr.mtk.base.StructMap;
import au.edu.apsr.mtk.ch.METSReader;

@Ignore
public class METSWrapperTest {
	private static final Logger LOG = LoggerFactory.getLogger(METSWrapperTest.class);
	private static final String METS_FILE = "METS_ANL000001.xml";

	private static METS mets;

	@Ignore
	public void test() throws Exception {
		LOG.debug("Reading " + METS_FILE + " ...");
		InputStream in = getClass().getClassLoader().getResourceAsStream(METS_FILE);
		METSReader mr = new METSReader();

		LOG.debug("Mapping to DOM...");
		mr.mapToDOM(in);
		METSWrapper mw = new METSWrapper(mr.getMETSDocument());

		LOG.debug("Validating...");
		mw.validate("http://www.loc.gov/standards/mets/mets.xsd");

		LOG.debug("Getting object...");
		mets = mw.getMETSObject();

		System.out.println("Package Type of " + mets.getType() + ", using profile: " + mets.getProfile());

		MetsHdr mh = mets.getMetsHdr();
		if (mh != null) {
			System.out.println("Package create date: " + mh.getCreateDate());
			System.out.println("Package last modified date: " + mh.getLastModDate());

			List<Agent> agents = mh.getAgents();
			for (Iterator<Agent> i = agents.iterator(); i.hasNext();) {
				Agent a = i.next();
				System.out.println("Agent " + a.getName() + " has role " + a.getRole());
			}
		}

		FileSec fileSec = mets.getFileSec();
		if (fileSec != null) {
			List<FileGrp> fgs = fileSec.getFileGrps();
			for (Iterator<FileGrp> i = fgs.iterator(); i.hasNext();) {
				FileGrp fg = i.next();
				System.out.println("FileGrp of use " + fg.getUse());
			}
		}

		List<StructMap> sms = mets.getStructMaps();

		System.out.println("Package has " + sms.size() + " structMap(s)");

		// let's look at the first StructMap
		StructMap sm = sms.get(0);

		showDivInfo(sm.getDivs());
	}

	private static void showDivInfo(List<Div> divs) throws METSException {
		for (Iterator<Div> divi = divs.iterator(); divi.hasNext();) {
			Div div = divi.next();

			System.out.println("Div type of " + div.getType() + " with DMDID " + div.getDmdID()
					+ " contains metadata of type " + mets.getDmdSec(div.getDmdID()).getMDType());

			showDivInfo(div.getDivs());
		}
	}

}
