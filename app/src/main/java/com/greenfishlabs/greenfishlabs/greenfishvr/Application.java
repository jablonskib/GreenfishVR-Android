package com.greenfishlabs.greenfishlabs.greenfishvr;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by DeathStar on 7/20/16.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/MyriadProLight.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
