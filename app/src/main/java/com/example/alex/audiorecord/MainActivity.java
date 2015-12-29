package com.example.alex.audiorecord;

import java.util.*;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.Bundle;
import android.os.Environment;

//button environment
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;

//dates
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;

//charts
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.graphics.Color;
import android.graphics.Point;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

//timer
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.util.Log;

public class MainActivity extends Activity {
    Button play,stop,record; //reset
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;
    //int currentAmplitude;

    private static final String TAG = "MainActivity";

    private TextView timerValue;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    //flag used for threading
    Boolean recFlag;
    long interval = 40; //interval to reset timer


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenChart();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss"); //date format for file saving
        Date now = new Date();

        play=(Button)findViewById(R.id.button3);
        stop=(Button)findViewById(R.id.button2);
        record=(Button)findViewById(R.id.button);
        //reset = (Button)findViewById(R.id.button4);
        timerValue = (TextView) findViewById(R.id.timerValue);


        stop.setEnabled(false);
        play.setEnabled(false);
        //reset.setEnabled(false);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SleepRecording/" ;
        File dir = new File(path);
        dir.mkdirs();
        outputFile = path +  formatter.format(now) + "recording.3gp";
        //external storage to create file "recording"

        myAudioRecorder=new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);//creates .mp4 file for media use
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);//default sampling rate is 44.8k
        myAudioRecorder.setOutputFile(outputFile);


        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //myAudioRecorder.setMaxDuration(60000);test
                try {
                    myAudioRecorder.prepare();
                    //amplitude between start and stop
                    myAudioRecorder.start();
                    //timer
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    recFlag=true;
                }
                catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                record.setEnabled(false);
                stop.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAudioRecorder.stop();
                myAudioRecorder.reset();
                myAudioRecorder.release();
                myAudioRecorder  = null;

                stop.setEnabled(false);
                play.setEnabled(true);
                //timer
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                recFlag = false;
                Toast.makeText(getApplicationContext(), "Audio recorded successfully",Toast.LENGTH_LONG).show();
            }
        });



        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException,SecurityException,IllegalStateException {
                //replayGraph(); --> change this to a dynamic thread *******************************************************8
                MediaPlayer m = new MediaPlayer();
                try {
                    m.setDataSource(outputFile);//setdatasource does not work
                }

                catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    m.prepare();
                }

                catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
                //reset.setEnabled(true);
            }
        });
    }
    private GraphicalView mChart;

    XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
    // Create XYMultipleSeriesRenderer to customize the whole chart
    XYMultipleSeriesRenderer mRenderer=new XYMultipleSeriesRenderer();
    // Create XY Series for X Series.
    XYSeries xSeries=new XYSeries("Sound");
    XYSeriesRenderer Xrenderer=new XYSeriesRenderer();


    private void OpenChart()
    {
        // Create a Dataset to hold the XSeries.
        xSeries.add(0, 0);
        // Add X series to the Dataset.
        dataset.addSeries(xSeries);

        // Create XYSeriesRenderer to customize XSeries

        Xrenderer.setColor(Color.CYAN);
        Xrenderer.setPointStyle(PointStyle.CIRCLE);
        Xrenderer.setDisplayChartValues(false);
        Xrenderer.setLineWidth(3);
        Xrenderer.setFillPoints(true);
        FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ALL);
        fill.setColor(Color.CYAN);
        Xrenderer.addFillOutsideLine(fill);

        mRenderer.setXTitle("TIME");
        mRenderer.setYTitle("SOUND");
        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setXLabels(0);
        mRenderer.setPanEnabled(false);
        mRenderer.setShowGrid(false);
        //mRenderer.setClickEnabled(true);
        mRenderer.setYAxisMax(32767);
        mRenderer.setYAxisMin(0); //capped at max amplitude
        mRenderer.setShowLegend(false);

        // Adding the XSeriesRenderer to the MultipleRenderer.
        mRenderer.addSeriesRenderer(Xrenderer);

        LinearLayout chart_container=(LinearLayout)findViewById(R.id.Chart_layout);

        // Creating an intent to plot line chart using dataset and multipleRenderer

        mChart=(GraphicalView)ChartFactory.getLineChartView(getBaseContext(), dataset, mRenderer);

// Add the graphical view mChart object into the Linear layout .
        chart_container.addView(mChart);
    }


        //timer thread
        private Runnable updateTimerThread = new Runnable() {
            public void run() {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

                updatedTime = timeSwapBuff + timeInMilliseconds;
                //add points to graph every 100 ms?
                if ((updatedTime % interval ==0) && (recFlag == true)){

                    long temp = updatedTime;
                    int amp = myAudioRecorder.getMaxAmplitude();
                    xSeries.add(updatedTime, amp);
                    Log.v(TAG, "temp: "+ temp + " amp: " + amp);
                    mChart.repaint();
                }
                int secs = (int) (updatedTime / 1000);
                int mins = secs / 60;
                int hours = mins / 60;
                mins = mins % 60;
                secs = secs % 60;
                int milliseconds = (int) (updatedTime % 1000);
                timerValue.setText (" " + hours  + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs) + ":" + String.format("%03d", milliseconds));
                customHandler.postDelayed(this, 0);
            }
        };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}