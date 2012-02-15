/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.stratos.automation.test.bps.manageScenarios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.*;
import org.wso2.carbon.admin.service.utils.FrameworkSettings;
import org.wso2.carbon.system.test.core.RequestSender;
import org.wso2.carbon.system.test.core.TestTemplate;
import org.wso2.carbon.system.test.core.utils.TenantDetails;
import org.wso2.carbon.system.test.core.utils.TenantListCsvReader;

public class BpelProcessManagementClient extends TestTemplate {

    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelProcessManagementClient.class);
    String backEndUrl = null;
    String serviceUrl = null;
    AdminServiceBpelUploader bpelUploader;
    AdminServiceBpelPackageManager bpelManager;
    AdminServiceBpelProcessManager bpelProcrss;
    AdminServiceBpelInstanceManager bpelInstance;
    AdminServiceAuthentication adminServiceAuthentication;
    RequestSender requestSender;

    @Override
    public void init() {
        FrameworkSettings.getFrameworkProperties();
        backEndUrl = FrameworkSettings.BPS_BACKEND_URL;
        adminServiceAuthentication = new AdminServiceAuthentication(backEndUrl);
        System.out.println(FrameworkSettings.BPS_BACKEND_URL);
        testClassName = BpelProcessManagementClient.class.getName();
        if (FrameworkSettings.getStratosTestStatus()) {
            TenantDetails bpsTenant = TenantListCsvReader.getTenantDetails(3);
            serviceUrl = "http://" + FrameworkSettings.BPS_SERVER_HOST_NAME + "/services/t/" + bpsTenant.getTenantName().split("@")[1];
            sessionCookie = adminServiceAuthentication.login(bpsTenant.getTenantName(), bpsTenant.getTenantPassword(), FrameworkSettings.BPS_SERVER_HOST_NAME);
        } else {
            serviceUrl = "http://" + FrameworkSettings.BPS_SERVER_HOST_NAME + "/" + FrameworkSettings.BPS_SERVER_HTTP_PORT + "/services";
            sessionCookie = adminServiceAuthentication.login("admin", "admin", FrameworkSettings.BPS_SERVER_HOST_NAME);
        }
        bpelUploader = new AdminServiceBpelUploader(backEndUrl);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);
        bpelInstance = new AdminServiceBpelInstanceManager(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
        bpelUploader.deployBPEL("LoanService", "LoanService", sessionCookie);
    }

    @Override
    public void runSuccessCase() {
        try {
            String processID = bpelProcrss.getProcessId("XKLoanService");
            bpelProcrss.setStatus(processID, "RETIRED");
            assertTrue("PPEL process is not set as RETIRED", bpelProcrss.getStatus(processID).equals("RETIRED"));
            assertFalse("Service is still available", requestSender.isServiceAvailable(serviceUrl + "/XKLoanService"));
            Thread.sleep(5000);
            bpelProcrss.setStatus(processID, "ACTIVE");
            Thread.sleep(5000);
            assertTrue("PPEL process is not set as ACTIVE", bpelProcrss.getStatus(processID).equals("ACTIVE"));
            assertTrue("Service is not available", !requestSender.isServiceAvailable(serviceUrl + "/XKLoanService"));
        } catch (InterruptedException e) {
            log.error("Process management failed" + e.getMessage());
            fail(e.getMessage());
        }
    }

    @Override
    public void cleanup() {
        bpelManager.undeployBPEL("LoanService");
        adminServiceAuthentication.logOut();
    }
}