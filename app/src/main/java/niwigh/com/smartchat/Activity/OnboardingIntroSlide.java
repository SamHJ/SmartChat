package niwigh.com.smartchat.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import niwigh.com.smartchat.Adapter.IntroViewPagerAdapter;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Model.ScreenItem;

public class OnboardingIntroSlide extends AppCompatActivity {

    private ViewPager screenPager;
    IntroViewPagerAdapter introViewPagerAdapter;
    TabLayout tabIndicator;
    Button btnNext;
    Button btnGetStarted;
    int position = 0;
    Animation btnAnimate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //as this activity is about to be launched, check if the user has already viewed the intro slide

        if(restorePrefData()){
            startActivity(new Intent(getApplicationContext(), SplashScreen.class));
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
            finish();
        }

        setContentView(R.layout.activity_onboarding_intro_slide);

        //init views
        btnNext = findViewById(R.id.next_button);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnimate = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation);



        //fill list screen
        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Lightening Messenger", "Chat with friends in the SmartChat community", R.drawable.chatter,""));
        mList.add(new ScreenItem("Awesome Engagement", "With lots of smileys, you engage with your right happy messaging platform", R.drawable.awesome_engagement,""));
        mList.add(new ScreenItem("Easy to Use", "Chat from wherever you are with any user in the SmartChat community", R.drawable.easy_to_use,""));
        mList.add(new ScreenItem("Realtime Stats", "Know how far you've reached in the SmartChat community right from your posts likes and comments", R.drawable.user_statistics,""));
        mList.add(new ScreenItem("SmartChat", "", R.drawable.team_work,""));

        // setup Viewpager
        screenPager= (ViewPager)findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this,mList);
        screenPager.setAdapter(introViewPagerAdapter);


        tabIndicator.setupWithViewPager(screenPager);

        //nextBtn click listener
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                position = screenPager.getCurrentItem();
                if (position < mList.size()){
                    position++;
                    screenPager.setCurrentItem(position);
                }

                if (position == mList.size()-1){
                    //when we reach the last screen slide
                    // show the get started button and hide the indicator and next button
                    loadLastScreen();
                }
            }
        });



        //tab layout add chang listener

        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == mList.size()-1){
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //get started button clicklistener
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getstartedIntent = new Intent(getApplicationContext(),Login.class);
                startActivity(getstartedIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);

                //use sharedpreferences to prevent the intro slide from showing up again
                savePrefsData();

                finish();
            }
        });


    }

    private boolean restorePrefData() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("intro_slide_pref", MODE_PRIVATE);
        Boolean isIntroActivityOpenedBefore = preferences.getBoolean("isIntroOpened", false);
        return isIntroActivityOpenedBefore;
    }

    private void savePrefsData() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("intro_slide_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.commit();
    }

    // show the get started button and hide the indicator and next button
    private void loadLastScreen() {
        btnNext.setVisibility(View.INVISIBLE);
        btnGetStarted.setVisibility(View.VISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);

        //add animation to get started button
        //setup the animation
        btnGetStarted.setAnimation(btnAnimate);
    }
}