package com.example.digitsrecognition;

import androidx.appcompat.app.AppCompatActivity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.opencv.android.OpenCVLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IRecognition
{
    private BarChart _chart;
    private PaintView _paintView;
    private Classifier _classifier;
    private ImageProcessing _processing;
    private int DIGITS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        OpenCVLoader.initDebug();
        _processing = new ImageProcessing();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _paintView = findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        _paintView.init(this, metrics);
        _chart = findViewById(R.id.chart);

        final Button buttonClear = findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                _paintView.clear();
                _clearCharts();
            }
        });

        final Button buttonColor = findViewById(R.id.colorButton);
        buttonColor.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if(_paintView.getBrushColor() == Color.WHITE)
                {
                    buttonColor.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    _paintView.setBrushColor(Color.BLACK);
                }
                else
                {
                    buttonColor.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    _paintView.setBrushColor(Color.WHITE);
                }
            }
        });

        _createChart();

        try
        {
            _classifier = new Classifier(_loadModelFile());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public void startRecognition()
    {
        if(_paintView.isEmpty())
            Toast.makeText(this, "Image is empty!",
                    Toast.LENGTH_SHORT).show();
        else
        {
            Bitmap resizedImage=Bitmap.createBitmap(_paintView.getBitmap(), 0,0,_paintView.getWidth(), _paintView.getHeight());
            try
            {
                float [] res = _classifier.predict(_processing.imageToTensor(resizedImage));
                List<Float> scores = new ArrayList<>();

                for(int i = 0; i < res.length; ++i)
                    scores.add(res[i] * 100);

                _updateChart(scores);
            }
            catch(Exception ex)
            {
                _clearCharts();
            }
        }
    }

    private void _createChart()
    {
        List<String> labels = new ArrayList<>();

        labels.add("0");
        labels.add("1");
        labels.add("2");
        labels.add("3");
        labels.add("4");
        labels.add("5");
        labels.add("6");
        labels.add("7");
        labels.add("8");
        labels.add("9");

        _chart.setDrawBarShadow(false);
        _chart.setDrawValueAboveBar(true);
        _chart.getDescription().setEnabled(false);
        _chart.setPinchZoom(false);
        _chart.setScaleEnabled(false);
        _chart.setDrawGridBackground(false);
        _chart.getAxisRight().setEnabled(false);
        _chart.getAxisLeft().setEnabled(false);
        _chart.setFitBars(true);
        _chart.getLegend().setEnabled(false);
        _chart.setViewPortOffsets(0,0, 0,60);

        _chart.getAxisLeft().setAxisMinimum(0f);
        _chart.getAxisRight().setAxisMinimum(0f);

        _chart.getAxisRight().setAxisMaximum(114f);
        _chart.getAxisLeft().setAxisMaximum(114f);

        XAxis xAxis = _chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(DIGITS);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextSize(16);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        List<Float> scores = new ArrayList<>(Collections.nCopies(DIGITS, (float)0.0));

        _updateChart(scores);

    }

    private void _updateChart(List<Float> scores)
    {
        List<BarEntry> yVals = new ArrayList<>();

        for (int i = 0; i < DIGITS; i++)
            yVals.add(new BarEntry(i, scores.get(i)));

        BarDataSet dataSet;
        dataSet = new BarDataSet(yVals, "SCORE");
        dataSet.setColor(Color.rgb(69, 121, 181));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);
        data.setValueTextSize(12);
        data.setHighlightEnabled(false);

        _chart.setData(data);
        _chart.animateY(300);
        _chart.invalidate();
    }

    private void _clearCharts()
    {
        List<Float> scores = new ArrayList<>(Collections.nCopies(DIGITS, 0f));
        _updateChart(scores);
    }

    private MappedByteBuffer _loadModelFile() throws IOException
    {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("mnist.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
