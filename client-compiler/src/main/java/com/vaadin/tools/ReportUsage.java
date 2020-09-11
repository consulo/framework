/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tools;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ReportUsage {

    public static final String ANONYMOUS_ID = "";

    // for compatibility with old checks
    private static final String USER_AGENT_BASE = "GWT Freshness Checker";

    private static final String COMPILER = "Compiler"; //$NON-NLS-1$

    private static final String E_QPARAM = "&e="; //$NON-NLS-1$

    private static final String R_QPARAM = "&r=unknown"; //$NON-NLS-1$

    private static final String ID_QPARAM = "&id="; //$NON-NLS-1$

    private static final String V_QPARAM = "?v="; //$NON-NLS-1$

    private static final String USER_AGENT = "User-Agent"; //$NON-NLS-1$

    // Use the GWT Freshness checker URL to store usage reports.
    private static final String QUERY_URL = "https://tools.vaadin.com/version/currentversion.xml"; //$NON-NLS-1$

    // Preferences keys
    private static final String FIRST_LAUNCH = "firstLaunch"; //$NON-NLS-1$
    private static final String LAST_PING = "lastPing";

    public static final long ONE_DAY = 24 * 60 * 60 * 1000;

    // for testing only
    public static void main(String[] args) {
        report();
    }

    public static FutureTask<Void> checkForUpdatesInBackgroundThread() {
        FutureTask<Void> task = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ReportUsage.report();
                return null;
            }
        });
        Thread checkerThread = new Thread(task, "Vaadin Update Checker");
        checkerThread.setDaemon(true);
        checkerThread.start();
        return task;
    }

    public static void report() {
        // stub
    }
}
