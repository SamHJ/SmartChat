package niwigh.com.smartchat.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import niwigh.com.smartchat.Model.ScreenItem;
import niwigh.com.smartchat.R;

public class IntroViewPagerAdapter extends PagerAdapter {

    Context mContext;
    List<ScreenItem> mListScreen;

    public IntroViewPagerAdapter(Context mContext, List<ScreenItem> mListScreen){
        this.mContext = mContext;
        this.mListScreen = mListScreen;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutscreen = inflater.inflate(R.layout.layout_screen, null);

        ImageView imgSlide = layoutscreen.findViewById(R.id.intro_img);
        TextView introTitle = layoutscreen.findViewById(R.id.intro_title);
        TextView introDescription = layoutscreen.findViewById(R.id.intro_description);
        TextView intro_short_desc = layoutscreen.findViewById(R.id.intro_short_desc);



        introTitle.setText(mListScreen.get(position).getTitle());
        introDescription.setText(mListScreen.get(position).getDescription());
        imgSlide.setImageResource(mListScreen.get(position).getScreenImg());
        intro_short_desc.setText(mListScreen.get(position).getShortDesc());


        container.addView(layoutscreen);

        return layoutscreen;
    }

    @Override
    public int getCount() {
        return mListScreen.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object){
        container.removeView((View)object);
    }
}