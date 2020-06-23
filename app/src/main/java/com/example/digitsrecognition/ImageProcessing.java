package com.example.digitsrecognition;
import android.graphics.Bitmap;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.Utils;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessing
{
    //Resize image to size 28x28 that is used in MNIST and convert to tensor
    public float[][][][] imageToTensor(Bitmap image)
    {
        Mat mat = _preprocess(image);
        Mat cnt = _cropContours(mat);

        Imgproc.resize(cnt, cnt, new Size(28, 28));
        return _createTensor(cnt);
    }

    private Mat _preprocess(Bitmap image)
    {
        Mat mat = new Mat();
        Utils.bitmapToMat(image, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mat, mat, 128,255, Imgproc.THRESH_BINARY_INV);
        return mat;
    }

    //Find digit contour and create new image that fits it
    private Mat _cropContours(Mat image)
    {
        //Find all contours in image and then find total bounding rectangle
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Point> allContours = new ArrayList<>();

        for(int i = 0; i < contours.size(); ++i)
            allContours.addAll(contours.get(i).toList());

        MatOfPoint contoursPoints = new MatOfPoint();
        contoursPoints.fromList(allContours);
        Rect br = Imgproc.boundingRect(contoursPoints);
        Mat contourImage = new Mat(image, br);

        //Create black image where digit should be placed
        float scale = 1.2f;
        int side = (int)(Math.max(br.height, br.width) * scale);
        Mat bg = new Mat(new Size(side , side), CvType.CV_8UC1, new Scalar(0));

        //Place digit in the center of image
        Point center = new Point(side / 2, side / 2);

        Rect r = new Rect((int)(center.x - contourImage.width() / 2),
                (int)(center.y - contourImage.height() / 2),
                contourImage.width(),
                contourImage.height());
        Mat roi = new Mat(bg, r);
        contourImage.copyTo(roi);
        return bg;
    }

    //Create normalized tensor that is used as input to NN.
    //Tensor shape is (batch_size, width, height, channels)
    private float[][][][] _createTensor(Mat image)
    {
        float matrix[][][][] = new float[1][image.height()][image.width()][1];
        int rows = image.rows();
        int cols = image.cols();

        for(int i = 0; i < rows; ++i)
        {
            for(int j = 0; j < cols; ++j)
            {
                double[] pixel = image.get(i, j);
                matrix[0][i][j][0] = (float)(pixel[0] / 255);
            }
        }
        return matrix;
    }
}
