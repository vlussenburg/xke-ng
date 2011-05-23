package com.xebia.xcoss.axcv;

import android.os.Bundle;
import android.view.Window;

public class CVSplashLoader extends XebiaConferenceActivity {
	
	private int count = 0;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.loader);
        
        // https://github.com/commonsguy/cw-andtutorials/blob/master/08-Threads/LunchList/src/apt/tutorial/LunchList.java
        /*
        RotateAnimation anim = new RotateAnimation(0f, 350f, 15f, 15f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(700);

        // Start animating the image
        final ImageView splash = (ImageView) findViewById(R.id.splash);
        splash.startAnimation(anim);

        // Later.. stop the animation
        splash.setAnimation(null);
        clearAnimation()
        */
//        setProgressBarVisibility(true);
//        setProgress(count++);
//        setProgressBarVisibility(false);
//        enableMenu(true);
    }
}