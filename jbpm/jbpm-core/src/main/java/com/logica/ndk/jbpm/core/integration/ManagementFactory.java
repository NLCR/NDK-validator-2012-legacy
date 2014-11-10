/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.logica.ndk.jbpm.core.integration;

import com.logica.ndk.jbpm.core.integration.impl.ActiveWorkItemService;
import com.logica.ndk.jbpm.core.integration.impl.ActiveWorkItemServiceImpl;
import com.logica.ndk.jbpm.core.integration.impl.MaintainanceService;
import com.logica.ndk.jbpm.core.integration.impl.MaintainanceServiceImpl;
import com.logica.ndk.jbpm.core.integration.impl.ProcessManagementImpl;
import com.logica.ndk.jbpm.core.integration.impl.TaskManagement;
import com.logica.ndk.jbpm.core.integration.impl.UserManagement;
import com.logica.ndk.jbpm.core.integration.impl.WorkItemInfoService;
import com.logica.ndk.jbpm.core.integration.impl.WorkItemInfoServiceImpl;

public class ManagementFactory {

	public ProcessManagement createProcessManagement() {
		return new ProcessManagementImpl();
	}

	public TaskManagement createTaskManagement() {
		return new TaskManagement();
	}

	public UserManagement createUserManagement() {
		return new UserManagement();
	}

	public ActiveWorkItemService createActiveWorkItemService() {
	  return new ActiveWorkItemServiceImpl();
	}
	
  public WorkItemInfoService createWorkItemInfoService() {
    return new WorkItemInfoServiceImpl();
  }
	
  public MaintainanceService createMaintainanceService() {
    return new MaintainanceServiceImpl();
  }
}
