package com.logica.ndk.tm.repair;

import com.logica.ndk.tm.utilities.transformation.UpdateMetsFilesImpl;

public class CreateMetsFilesImpl {

	public void create(String cdmId) {
		CreateMetsFilesImpl createMets = new CreateMetsFilesImpl();
		UpdateMetsFilesImpl updateMetsFilesImpl = new UpdateMetsFilesImpl();
		createMets.create(cdmId);
		updateMetsFilesImpl.execute(cdmId);
	}
	
	public static void main(String[] args) {
    new CreateMetsFilesImpl().create("8f930c10-a365-11e3-b833-005056827e52");
	}
}
