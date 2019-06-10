package com.example.livealone4.Adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.livealone4.Activities.LiveAloneActivity;
import com.example.livealone4.Activities.MainActivity;
import com.example.livealone4.Activities.UserProfileActivity;
import com.example.livealone4.Fragments.MessageDialogFragment;
import com.example.livealone4.Models.LiveAlone;
import com.example.livealone4.Models.User;
import com.example.livealone4.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/*
    게시물 관리 어댑터

 */

public class LiveAloneAdapter extends RecyclerView.Adapter {

    private List<LiveAlone> list;
    private List<User> users;
    private Context context;

    public LiveAloneAdapter(List<LiveAlone> list, List<User> users, Context context) {
        this.list = list;
        this.context = context;
        this.users = users;

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_alone, parent, false);
        return new LiveAloneViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final LiveAlone liveAlone = list.get(position);
        final User user = users.get(position);

        ((LiveAloneViewHolder)holder).bind(liveAlone, user);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LiveAloneActivity.class);
                Pair<View, String> cardViewPair = Pair.create((View)((LiveAloneViewHolder)holder).liveAloneCardView,"card_view_transition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context, cardViewPair);
                intent.putExtra("key", liveAlone.getKey());
                ((Activity) context).startActivityForResult(intent, MainActivity.REQUEST_LIVE_ALONE_ACTIVITY ,options.toBundle());

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class LiveAloneViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView profileImageView, alertImage;
        TextView titleText, dateText, periodText, aloneTypeText, locationText, nameText, starText;
        CardView liveAloneCardView;

        public LiveAloneViewHolder(View itemView) {
            super(itemView);
            alertImage = itemView.findViewById(R.id.alert_image);
            liveAloneCardView = itemView.findViewById(R.id.live_alone_card_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
            shapeDrawable.getPaint().setColor(Color.TRANSPARENT);
            profileImageView.setBackground(shapeDrawable);
            profileImageView.setClipToOutline(true);
            profileImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            titleText = itemView.findViewById(R.id.live_alone_title_text_view);
            dateText = itemView.findViewById(R.id.live_alone_upload_dat_text_view);
            periodText = itemView.findViewById(R.id.live_alone_period_text_view);
            aloneTypeText = itemView.findViewById(R.id.live_alone_type_text_view);
            locationText = itemView.findViewById(R.id.live_alone_location_text_view);
            nameText = itemView.findViewById(R.id.live_alone_name_text_view);
            starText = itemView.findViewById(R.id.live_alone_star_text_view);

        }

        void bind(LiveAlone liveAlone, final User user){
            MainActivity.getFirebasePicture().downloadImage(liveAlone.getUid(), profileImageView);

            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("uid", user.getUid());
                    intent.putExtra("name", user.getName());
                    intent.putExtra("star",  String.format("%.2f", user.getStar()));
                    intent.putExtra("loc", user.getLocation());
                    intent.putExtra("count", user.getLivealoneCount());
                    context.startActivity(intent);

                }
            });
            if(liveAlone.getTitle().length() > 12){
                String title = liveAlone.getTitle().substring(0, 12) + " ...";
                titleText.setText(title);
            } else {
                titleText.setText(liveAlone.getTitle());
            }

            //시간 관련 텍스트뷰 (Period, Date)
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat fmt2 = new SimpleDateFormat("MM/dd");
            SimpleDateFormat fmt3 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(liveAlone.getStartPeriod());
            periodText.setText(fmt.format(cal.getTime()));
            cal.setTimeInMillis(liveAlone.getEndPeriod());
            periodText.append(" - "+ fmt2.format(cal.getTime()));
            cal.setTimeInMillis((long)liveAlone.getTimestamp());
            dateText.setText(fmt3.format(cal.getTime()));

            aloneTypeText.setText(liveAlone.getAloneType());
            locationText.setText(liveAlone.getLocation());

            starText.setText("★ " + String.format("%.2f",user.getStar()) + " (" + user.getLivealoneCount() + ")");
            nameText.setText(user.getName());

            if(user.getType1() == 1){
                alertImage.setVisibility(View.VISIBLE);
                alertImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MessageDialogFragment.showDialog(MessageDialogFragment.ALERT_ABNORMAL, context);
                    }
                });
            } else {
                alertImage.setVisibility(View.GONE);
            }
        }
    }


}
