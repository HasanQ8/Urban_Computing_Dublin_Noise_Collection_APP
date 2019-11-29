package com.hasanfakhra.noise_detector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.hasanfakhra.noise_detector.Surrounding.dbCount;




public class MainActivity extends Activity
{



    //https://github.com/neopixl/PixlUI for custom fonts
    //https://firebase.google.com/docs/firestore/manage-data/add-data for firebase reference
    //https://github.com/PhilJay/MPAndroidChart for charts



    ArrayList<Entry> yVals;
    boolean refreshed = false;
    public static Typeface tf;
    CurrentLocation gps;
    Button mapButton;
    ImageButton refreshButton;
    LineChart mChart;
    TextView latid;
    TextView longid;
    TextView decid;
    long currentTime = 0;
    long savedTime = 0;
    boolean isChart = false;
    boolean clicked=false;
    boolean recordtrack=false;
    FileOutputStream os;

    FirebaseFirestore db = FirebaseFirestore.getInstance();



    /* Decibel */
    private boolean bListener = true;
    private boolean isThreadRun = true;
    private Thread thread;
    float volume = 10000;
    int refresh = 0;
    private MyMediaRecorder mRecorder;
    String myroute = "My Route";





    @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if(recordtrack) {
                    super.handleMessage(msg);
                    DecimalFormat df1 = new DecimalFormat("####.0");
                    if (msg.what == 1) {
                        if (!isChart) {
                            initChart();
                            return;
                        }

                        decid.setText(df1.format(dbCount));
                        updateData(dbCount, 0);


                        if (refresh == 1) {
                            long now = new Date().getTime();
                            now = now - currentTime;
                            now = now / 1000;
                            refresh = 0;
                        } else {
                            refresh++;
                        }
                    }
                }
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        requestPermissions();
        longid = (TextView) findViewById(R.id.longid);
        latid = (TextView) findViewById(R.id.latid);
        tf = Typeface.createFromAsset(this.getAssets(), "fonts/Let_s go Digital Regular.ttf");
        decid = (TextView) findViewById(R.id.decid);

        gps = new CurrentLocation(MainActivity.this);


        db = FirebaseFirestore.getInstance();

        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "start tracking");
                recordtrack=true;


               if (clicked){

                   onResume();
                   clicked=false;
               }

                if(gps.canGetLocation())
                {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    longid.setText("" + longitude);
                    latid.setText("" + latitude);
                    longid.setTypeface(tf);
                    latid.setTypeface(tf);

                    Log.d("Service", "Sending intent to start service");
                    Toast toast = Toast.makeText(getApplicationContext(), "starting to record sound amplitude", Toast.LENGTH_SHORT);
                    toast.show();

                }
                else
                {
                    gps.showSettingsAlert();
                }

            }
        });

        // define exit button
        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            public void onClick(View v) {
                Log.d(TAG, "Stop Tracking");
                writeSoundToFirebase();

                clicked=true;
                recordtrack=false;
                onPause();
                Toast toast = Toast.makeText(getApplicationContext(), "stopped recording sound amplitude", Toast.LENGTH_SHORT);
                toast.show();
            }
        });









        tf = Typeface.createFromAsset(this.getAssets(), "fonts/Let_s go Digital Regular.ttf");
        decid.setTypeface(tf);
        longid.setTypeface(tf);
        latid.setTypeface(tf);





        mapButton = (Button) findViewById(R.id.dublinlocation);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SoundHeatMap.class);
                startActivity(intent);

            }
        });
        refreshButton = (ImageButton) findViewById(R.id.refreshbutton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshed = true;
                Surrounding.minDB = 100;
                dbCount = 0;
                Surrounding.lastDbCount = 0;
                Surrounding.maxDB = 0;
                initChart();
            }
        });

        mRecorder = new MyMediaRecorder();


    }

    private void updateData(float val, long time) {
        if(mChart==null){
            return;
        }
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            LineDataSet set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            Entry entry=new Entry(savedTime,val);
            set1.addEntry(entry);
            if(set1.getEntryCount()>200){
                set1.removeFirst();
                set1.setDrawFilled(false);
            }
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            savedTime++;
        }
    }
    private void initChart() {
        if(mChart!=null){
            if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {
                savedTime++;
                isChart=true;
            }
        }else{
            currentTime=new Date().getTime();
            mChart = (LineChart) findViewById(R.id.chart1);
            mChart.setViewPortOffsets(50, 20, 5, 60);
            // no description text
            mChart.setDescription("Current Noise Level");
            // enable touch gestures
            mChart.setTouchEnabled(true);
            // enable scaling and dragging
            mChart.setDragEnabled(false);
            mChart.setScaleEnabled(true);
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(false);
            mChart.setDrawGridBackground(false);
            //mChart.setMaxHighlightDistance(400);
            XAxis x = mChart.getXAxis();
            x.setLabelCount(8, false);
            x.setEnabled(true);
            x.setTypeface(tf);
            x.setTextColor(Color.BLACK);
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setDrawGridLines(true);
            x.setAxisLineColor(Color.BLACK);
            YAxis y = mChart.getAxisLeft();
            y.setLabelCount(6, false);
            y.setTextColor(Color.BLACK);
            y.setTypeface(tf);
            y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            y.setDrawGridLines(false);
            y.setAxisLineColor(Color.BLUE);
            y.setAxisMinValue(0);
            y.setAxisMaxValue(120);
            mChart.getAxisRight().setEnabled(true);
            yVals = new ArrayList<Entry>();
            yVals.add(new Entry(0,0));
            LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
            set1.setValueTypeface(tf);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.02f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setCircleColor(Color.BLUE);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.BLUE);
            set1.setFillAlpha(100);
            set1.setDrawHorizontalHighlightIndicator(false);
            set1.setFillFormatter(new FillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return -10;
                }
            });
            LineData data;
            if (mChart.getData() != null &&
                    mChart.getData().getDataSetCount() > 0) {
                data =  mChart.getLineData();
                data.clearValues();
                data.removeDataSet(0);
                data.addDataSet(set1);
            }else {
                data = new LineData(set1);
            }

            data.setValueTextSize(9f);
            data.setDrawValues(false);
            mChart.setData(data);
            mChart.getLegend().setEnabled(false);
            mChart.animateXY(2000, 2000);
            mChart.invalidate();
            isChart=true;
        }

    }
    private void startListenAudio() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        if(bListener) {
                            volume = mRecorder.getMaxAmplitude();
                            if(volume > 0 && volume < 1000000) {
                                Surrounding.setDbCount(20 * (float)(Math.log10(volume)));
                                // Update with thread
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                            }
                        }
                        if(refreshed){
                            Thread.sleep(1200);
                            refreshed=false;
                        }else{
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        bListener = false;
                    }
                }
            }
        });
        thread.start();
    }

    private void createFile() {
    File file = FileUtil.createFile(this, "sound_level.csv");
    os = null;
    try {
        os = new FileOutputStream(file);
        os.write("Time, Amplitude".getBytes());
    } catch (IOException e) {
        Log.e("CSV", "File not found when creating file output stream");
        e.printStackTrace();
    }
}

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void writeSoundToFirebase() {

        Date date = new Date();



        TextView decibView= (TextView) findViewById(R.id.decid);

        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        double decib = dbCount;

        Map<String, String> dataToSave = new HashMap<>();
        dataToSave.put("Time", date.toString());
        dataToSave.put("Longitutde", longitude+"");
        dataToSave.put("Latitude",latitude+"");
        dataToSave.put("Noise Level",decib+"");
        db.collection("Route").document(date.toString()).set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Writing to firestore","Route was saved");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Writing to firestore", "Document was not saved",e);
            }
        });
    }

    public void startRecord(File fFile){
        try{
            mRecorder.setMyRecAudioFile(fFile);
            if (mRecorder.startRecorder()) {
                startListenAudio();
            }else{
                Toast.makeText(this, getString(R.string.activity_recStartErr), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(this, getString(R.string.activity_recBusyErr), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        File file = FileUtil.createFile(this, "sound_level.csv");
        if (file != null) {
            startRecord(file);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.activity_recFileErr), Toast.LENGTH_LONG).show();
        }
        bListener = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        bListener = false;
        mRecorder.delete(); //Stop recording and delete the recording file
        thread = null;
        isChart=false;
    }

    @Override
    protected void onDestroy() {
        if (thread != null) {
            isThreadRun = false;
            thread = null;
        }
        mRecorder.delete();
        super.onDestroy();
    }


    public void requestPermissions(){
        android.location.LocationManager lm = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        boolean isNetwork = lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);

        ArrayList<String> permissions = new ArrayList<>();
        ArrayList<String> permissionsToRequest;

        int ALL_PERMISSIONS_RESULT = 101;

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);


        if (!isGPS && !isNetwork) {
            showSettingsAlert();
        } else {
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                }
            }
        }
    }


    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }
    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }
    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
}