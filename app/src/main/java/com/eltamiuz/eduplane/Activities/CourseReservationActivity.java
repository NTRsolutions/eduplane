package com.eltamiuz.eduplane.Activities;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.eltamiuz.eduplane.Models.category;
import com.eltamiuz.eduplane.R;
import com.eltamiuz.eduplane.Volley.RegisterCourses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CourseReservationActivity extends AppCompatActivity {
    private static final int NOTIFICATION_ID = 1000;
    @BindView(R.id.sliderDetalis)
    SliderLayout sliderLayout;
    @BindView(R.id.detailsDetalis)
    TextView textViewDetalis;
    @BindView(R.id.startDetalis)
    TextView textViewStart;
    @BindView(R.id.endDetalis)
    TextView textViewEnd;
    @BindView(R.id.backReservation)
    ImageView imageViewBack;
    String itemName,clientName,clientPhone,type,start_at,end_at;
    int itemid,clientId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_reservation);
        initScreen();
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(this);
        int position = getIntent().getIntExtra("position",0);
        type = categoryActivity.type;
        category category = getObject(position);
        updateUI(category);
        getData();
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }

    private void getData() {
        final SharedPreferences sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
        clientName = sharedPreferences.getString("name", null);
        clientPhone = sharedPreferences.getString("phone", null);
        clientId = sharedPreferences.getInt("id", 0);
    }
    private void updateUI(category category) {
        for (String name1 : category.getUrls()) {
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(""+type)
                    .image(name1)
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop);
            sliderLayout.addSlider(textSliderView);
        }
        textViewDetalis.setText(category.getDisc());
        start_at = category.getStart_at();
        end_at = category.getEnd_at();
        textViewStart.setText("بداية حجز الكورس"+category.getStart_at());
        textViewEnd.setText("نهاية حجز الكورس"+category.getEnd_at());
        itemid=category.getId();
        itemName=category.getName();
    }
    private category getObject(int position) {
        return categoryActivity.categoryModelArrayList.get(position);
    }
    private void initScreen() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout_courses_reservation);
    }
    public void finishReservation(View view) {
        Log.e("detalisID",String.valueOf(itemid));
        Log.e("detalisID",itemName);
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("جارى التسجيل");
        progressDialog.setIcon(R.drawable.logo);
        progressDialog.show();
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonResponse = new JSONArray(response);
                    getJsonData(jsonResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(CourseReservationActivity.this, "لم يتم تسحيل الكورس", Toast.LENGTH_SHORT).show();
                }
            }
        };

        RegisterCourses loginPatiRequest = new RegisterCourses(String.valueOf(itemid),
                                                                    itemName,
                                                                    type
                                                                    ,start_at
                                                                    ,end_at
                                                                    ,String.valueOf(clientId)
                                                                    ,clientName
                                                                    ,clientPhone,
                                                                    "1",responseListener);
        RequestQueue queue = Volley.newRequestQueue(CourseReservationActivity.this);
        queue.add(loginPatiRequest);

    }

    private void WaitForCourse() {
        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        //icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(R.drawable.logos);

        // This intent is fired when notification is clicked
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://stacktips.com/"));
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Set the intent that will fire when the user taps the notification.
//        builder.setContentIntent(pendingIntent);

        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_1));

        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle("يتم حجز الكورس");

        // Content text, which appears in smaller text below the title
        builder.setContentText("يتم حجز الكورس الان ");

        // The subtext, which appears under the text on newer devices.
        // This will show-up in the devices with Android 4.2 and above only
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void getJsonData(JSONArray jsonResponse) {
        try {
            JSONObject jsonObject = jsonResponse.getJSONObject(0);
            int flag  = jsonObject.getInt("flag");
            if(flag == 0){
                WaitForCourse();
                progressDialog.dismiss();
               // Toast.makeText(this, "تم حجز الكورس مسبقا", Toast.LENGTH_SHORT).show();
            }else{
                WaitForCourse();

                progressDialog.dismiss();
               // Toast.makeText(this, "تم حجز الكورس ", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
