package com.example.digitsrecognition;

import org.tensorflow.lite.Interpreter;

import java.nio.MappedByteBuffer;

public class Classifier
{
    private Interpreter _tflite;

    public Classifier(MappedByteBuffer buffer)
    {
        _tflite = new Interpreter(buffer);
    }

    public float[] predict(float[][][][] tensor)
    {
        float [][] class_probes = new float[1][10];
        _tflite.run(tensor, class_probes);
        return class_probes[0];
    }
}
