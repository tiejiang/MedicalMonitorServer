package com.example.medicalmonitorserver.application;
/*
* Copyright 2009 Cedric Priscal
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/



import android.util.Log;
import com.example.medicalmonitorserver.common.CCPAppManager;

public class Application extends android.app.Application {

    private static Application instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CCPAppManager.setContext(instance);
//FileAccessor.initFileAccess();
//setChattingContactId();
//initImageLoader();
//CrashHandler.getInstance().init(this);
//SDKInitializer.initialize(instance);
//CrashReport.initCrashReport(getApplicationContext(), "900050687", true);
    }


    /**
     * 单例，返回一个实例
     * @return
     */
    public static Application getInstance() {
        if (instance == null) {
            Log.d("TIEJIANG", "[Application] instance is null.");
        }
        Log.d("TIEJIANG", "[ECApplication] return instance succeed.");
        return instance;
    }
}