package com.amarsaikhan.ascs.answersheetcheckersystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.circle;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "MYTAG";
    Mat mRgba, mRgbaF, mRgbaT, tmp2;
    Mat tmp = new Mat();
    Mat detectedEdges = new Mat();
    public boolean isCaptured;
    ArrayList<Kontours> kontours;
    private CameraBridgeViewBase cameraBridgeViewBase;

    List<MatOfPoint> contours;
    List<MatOfPoint> maxContours;
    List<MatOfPoint> contours2;
    List<MatOfPoint2f> newContours;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    cameraBridgeViewBase.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "Opencv successfully loaded");
        } else {
            Log.d(TAG, "opencv not loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "This Started App", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        Float x = cameraBridgeViewBase.getRotationX();
        Float y = cameraBridgeViewBase.getRotationY();
        //Toast.makeText(this, String.valueOf(x) + " " + String.valueOf(y), Toast.LENGTH_SHORT).show();
        cameraBridgeViewBase.setCvCameraViewListener(this);
        isCaptured = false;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.capture);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCaptured = true;
                Snackbar.make(view, "Captured", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            //
            Log.d(TAG, "OpenCV loader error");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV loaded successfully");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Toast.makeText(this, String.valueOf(width) + "X" + String.valueOf(height), Toast.LENGTH_SHORT).show();
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }



    public static List<MatOfPoint> getSquareContours(List<MatOfPoint> contours) {

        List<MatOfPoint> squares = null;

        for (MatOfPoint c : contours) {

         /*   if ((ContourUtils.isContourSquare(c)){

                if (squares == null)
                    squares = new ArrayList<MatOfPoint>();
                squares.add(c);
            }*/
        }

        return squares;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
       kontours = new ArrayList<Kontours>();

        Log.e(TAG, "this");
        mRgba = inputFrame.rgba();
        contours = new ArrayList<MatOfPoint>();
        contours2 = new ArrayList<MatOfPoint>();
        maxContours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        //Mat drawing = Mat.zeros(mRgba.size(), CvType.CV_8UC3);
        MatOfPoint2f screenCnt = new MatOfPoint2f();
        double maxArea = 0;
        // List<MatOfPoint2f> screenCnt = new ArrayList<>();

        Core.rotate(mRgba, mRgba, Core.ROTATE_90_CLOCKWISE); //ROTATE_180 or ROTATE_90_COUNTERCLOCKWISE

        Imgproc.cvtColor(mRgba, tmp, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(tmp, detectedEdges, new Size(5, 5));
        Imgproc.Canny(detectedEdges, detectedEdges, 75, 200);
        //List<MatOfPoint> lContours = largeContours(detectedEdges,0);

        Imgproc.findContours(detectedEdges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<MatOfPoint> squareContours = getSquareContours(contours);
        Scalar color = new Scalar(0, 255, 0);
        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea) {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
            kontours.add(new Kontours(contourArea, contourIdx));
        }
        ArrayList<Kontours> maxKontours = new ArrayList<Kontours>();
        maxVal = 0;
        while (kontours.size() != 0) {
            int in = 0;
            for (int i = 0; i < kontours.size(); i++) {
                if (maxVal < kontours.get(i).area) {
                    maxVal = kontours.get(i).area;
                    in = kontours.get(i).areaIdx;
                }
            }
            maxKontours.add(new Kontours(maxVal, in));
            kontours.remove(in);

        }
        if (maxKontours.size() > 5) {
            for (int i = 0; i < 5; i++) {
                maxContours.add(contours.get(maxKontours.get(i).areaIdx));
            }


            MatOfPoint2f approx = new MatOfPoint2f();

            for (int i = 0; i < maxContours.size(); i++) {

                MatOfPoint2f dst = new MatOfPoint2f();
                double epsilon = 0.02 * Imgproc.arcLength(new MatOfPoint2f(maxContours.get(i).toArray()), true);
                Imgproc.approxPolyDP(new MatOfPoint2f(maxContours.get(i).toArray()), approx, epsilon, true);
                Log.d("approx:", String.valueOf(approx.total()));
                if (approx.total() == 4) {
                    MatOfPoint approxf1 = new MatOfPoint();
                    approx.convertTo(approxf1, CvType.CV_32S);
                    contours2.add(approxf1);
                    for (MatOfPoint mop : contours2) {
                        for (Point p : mop.toList()) {
                            Imgproc.circle(mRgba, new Point(p.x, p.y), 10, color, 4);
                        }
                    }
                    //Log.d("size:", String.valueOf(contours2.size()));
                    for (int r = 0; r < approx.total(); r++) {
                        //Imgproc.drawContours(mRgba, contours2, -1, color, 2);
                        Imgproc.drawContours(mRgba, contours2, -1, color, 8);
                        //Imgproc.drawContours(mRgba, contours2, r, color, 8, hierarchy, 0, new Point());
                    }
                }

            }
        }

        if (isCaptured) {
            Log.d("FILE", "fileruu orson");
            isCaptured = false;
            saveFrame();
            cameraBridgeViewBase.disableView();
            onCameraViewStopped();
        }
        /*mRgba = inputFrame.rgba();
        tmp2 = new Mat();
        tmp2 = inputFrame.rgba();
        //Core.rotate(tmp2, tmp2, Core.ROTATE_90_COUNTERCLOCKWISE);
        //Core.rotate(mRgba, mRgba, Core.ROTATE_90_CLOCKWISE);
        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGR2GRAY);

        //convert the image to black and white does (8 bit)
        Imgproc.Canny(mRgba, mRgba, 50, 50);

        //apply gaussian blur to smoothen lines of dots
        Imgproc.GaussianBlur(mRgba, mRgba, new  org.opencv.core.Size(5, 5), 5);

        //find the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mRgba, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        int maxAreaIdx = -1;
        Log.d("size",Integer.toString(contours.size()));
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint largest_contour = contours.get(0);
        //largest_contour.ge
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
        //Imgproc.drawContours(imgSource,contours, -1, new Scalar(0, 255, 0), 1);

        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize*0.05, true);
                if (approxCurve_temp.total() == 4) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    approxCurve=approxCurve_temp;
                    largest_contour = temp_contour;
                }
            }
        }
        double[] temp_double;
        temp_double = approxCurve.get(0,0);
        if(temp_double.length>3) {
            Point p1 = new Point(temp_double[0], temp_double[1]);
            //Core.circle(imgSource,p1,55,new Scalar(0,0,255));
            //Imgproc.warpAffine(sourceImage, dummy, rotImage,sourceImage.size());
            temp_double = approxCurve.get(1, 0);
            Point p2 = new Point(temp_double[0], temp_double[1]);
            // Core.circle(imgSource,p2,150,new Scalar(255,255,255));
            temp_double = approxCurve.get(2, 0);
            Point p3 = new Point(temp_double[0], temp_double[1]);
            //Core.circle(imgSource,p3,200,new Scalar(255,0,0));
            temp_double = approxCurve.get(3, 0);
            Point p4 = new Point(temp_double[0], temp_double[1]);
            //Core.circle(imgSource,p4,100,new Scalar(0,0,255));
            List<Point> source = new ArrayList<Point>();
            source.add(p1);
            source.add(p2);
            source.add(p3);
            source.add(p4);
            Imgproc.circle(tmp2, p1, 10, new Scalar(0, 255, 0), 4);
            Imgproc.circle(tmp2, p2, 10, new Scalar(0, 255, 0), 4);
            Imgproc.circle(tmp2, p3, 10, new Scalar(0, 255, 0), 4);
            Imgproc.circle(tmp2, p4, 10, new Scalar(0, 255, 0), 4);
            Log.d("POINT", p1.toString());
            //Mat startM = Converters.vector_Point2f_to_Mat(source);
            //Mat result= new Mat();
            //result = warp(mRgba,startM);
        }*/
        return mRgba;
    }
    public static Mat warp(Mat inputMat, Mat startM) {

        int resultWidth = 1200;
        int resultHeight = 680;

        Point ocvPOut4 = new Point(0, 0);
        Point ocvPOut1 = new Point(0, resultHeight);
        Point ocvPOut2 = new Point(resultWidth, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, 0);

        if (inputMat.height() > inputMat.width()) {
            // int temp = resultWidth;
            // resultWidth = resultHeight;
            // resultHeight = temp;

            ocvPOut3 = new Point(0, 0);
            ocvPOut4 = new Point(0, resultHeight);
            ocvPOut1 = new Point(resultWidth, resultHeight);
            ocvPOut2 = new Point(resultWidth, 0);
        }

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);

        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);

        return outputMat;
    }
    public void saveFrame(){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            Bitmap bmp = null;
            try {
                bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mRgba, bmp);
            } catch (CvException e) {
                Log.d("Exception", e.getMessage());
            }
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            Log.d("Exception2", e.getMessage());
        }
    }
    public static List<MatOfPoint> largeContours(Mat im, int area) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        //Imgproc.findContours(im, contours, hierarchy, 1, Imgproc.RETR_LIST);
        Imgproc.findContours(im, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        // FIXME: stream requires Android sdk >= 24
        // return contours.stream()
        //         .filter(cnt -> Imgproc.contourArea(cnt) > area)
        //         .collect(Collectors.toList());

        List<MatOfPoint> ret = new ArrayList<>();
        for (MatOfPoint cnt : contours) {
            if (Imgproc.contourArea(cnt) > area) {
                ret.add(cnt);
            }
        }
        return ret;
    }


}
// double epsilon = 0.02*Imgproc.arcLength(new MatOfPoint2f(max_contour.toArray()[i]),true);
// MatOfPoint2f approx = new MatOfPoint2f();
// Imgproc.approxPolyDP(new MatOfPoint2f(max_contour.toArray()[i]),approx,epsilon,true);
// MatOfPoint points = new MatOfPoint(approx.toArray());
// if(points.total()==4){
//     Imgproc.drawContours(mRgba, Collections.singletonList(points), -1, color, 2);
// }
// double epsilon = 0.1*Imgproc.arcLength(new MatOfPoint2f(max_contour.toArray()),true);
// MatOfPoint2f approx = new MatOfPoint2f();
// Imgproc.approxPolyDP(new MatOfPoint2f(max_contour.toArray()),approx,epsilon,true);
// if(approx.elemSize()==4){
//     for(int l=0; l<approx.elemSize(); l++){
//         Log.d("APPROX", String.valueOf(approx.elemSize()));
//Imgproc.drawContours(mRgba, approx.toArray(), -1, color, 2);
//    }
//  }
//
//  if (maxValIdx == 4) {
//      Imgproc.drawContours(mRgba, contours, maxValIdx, new Scalar(0, 255, 0), 5);
//  }
// for(int i=0; i<contours.size(); i++){
//     Imgproc.drawContours(mRgba, contours, i, color, 2, 8, hierarchy, 0, new Point());
// }
        /*

        Iterator<MatOfPoint> iterator = contours.iterator();
        while (iterator.hasNext()){
            MatOfPoint contour = iterator.next();
            double area = Imgproc.contourArea(contour);
            if(area > maxArea){
                maxArea = area;
                max_contour = contour;
            }
        }
        Log.e("MAX_C", String.valueOf(max_contour.elemSize()));
        for(int j=0; j<max_contour.cols(); j++){
            for(int k=0; k<max_contour.rows(); k++){
                Log.d("VALUE", String.valueOf(max_contour.get(j, k)));
            }
        }


*/





        /*
    MatOfPoint max_contour = new MatOfPoint();

            Iterator<MatOfPoint> iterator = contours.iterator();
            while (iterator.hasNext()){
                MatOfPoint contour = iterator.next();
                double area = Imgproc.contourArea(contour);
                if(area > maxArea){
                    maxArea = area;
                    max_contour = contour;
                }
            }
        MatOfPoint2f thisContour2f = new MatOfPoint2f();
        hierarchy.convertTo(thisContour2f, CvType.CV_32FC2);
           // Log.d("hierarchy", String.valueOf(hierarchy));
           for (int i = 0; i < contours.size(); i++) {
                double epsilon = 0.02*Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()),true);

                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(new MatOfPoint2f(hierarchy),approx,epsilon,true);
                screenCnt = approx;
                if(approx.total()==4){
                    screenCnt = approx;
                   // for(int t=0; t<approx.total(); t++){
                   //     screenCnt.add(approx.get);
                   // }
                }
                //Scalar color = new Scalar(Math.random() * 255, Math.random() * 255, Math.random() * 255);
                //Imgproc.drawContours(mRgba, contours, i, color, 2, 8, hierarchy, 0, new Point());
            }
//            Scalar color = new Scalar(0,255,0);
//            if(screenCnt.size().equals(4)){
//                for(int i=0; i < screenCnt.total(); i++) {
//                    Imgproc.drawContours(mRgba, screenCnt.get(i, i,), -1, color, 2, 8);
//                }
//            }
            Log.e(TAG, "screencnt:"+String.valueOf(screenCnt.get(0,0)));
            double[] temp_double;
            temp_double = screenCnt.get(0,0);
            Point p1 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(mRgba,p1,55,new Scalar(0,0,255));
            //Imgproc.warpAffine(sourceImage, dummy, rotImage,sourceImage.size());
            temp_double = screenCnt.get(1,0);
            Point p2 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(mRgba,p2,150,new Scalar(255,255,255));
            temp_double = screenCnt.get(2,0);
            Point p3 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(mRgba,p3,200,new Scalar(255,0,0));
            temp_double = screenCnt.get(3,0);
            Point p4 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(mRgba,p4,100,new Scalar(0,0,255));

//            List<Point> source = new ArrayList<Point>();
//            source.add(p1);
//            source.add(p2);
//            source.add(p3);
//            source.add(p4);
//            for(int i=0; i < screenCnt.total(); i++) {
//                  Imgproc.drawContours(mRgba, screenCnt.get(i, 0), -1, color, 2, 8);
//               }


        //Imgproc.cornerHarris(detectedEdges, detectedEdges,7,5 ,0.05);
        /*MatOfPoint2f  dist = new MatOfPoint2f();
        MatOfPoint2f  src = new MatOfPoint2f();
        Mat homography = Calib3d.findHomography(src, dist, Calib3d.RANSAC, 10);
        Imgproc.warpPerspective(mRgba, outputMat, homography, new Size(mRgba.cols(), mRgba.rows()));*/