import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

public class Main {
    public static void main(String args[]) throws Exception {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);    //Загружаем библиотеку OpenCV

        Imgcodecs imageCodecs = new Imgcodecs();         //Создаем экземпляр класса imagecodecs

        Mat src_gray = new Mat();                        //бинарное изображение
        Mat src;                                         //исходное изображение
        int thresh = 100;                                //пороговое значение

        String file = "C:/Users/Public/maze/maze12.jpg"; //путь до изображения
        src = imageCodecs.imread(file);                  //загружаем изображение из файла

        if (src.empty()) {                               //обработка ошибки загрузки
            System.out.println("Could not open or find the image!\n");
            // предложить загрузить заново
        }
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2RGB);        //конвертирование изображения
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(src, src, 0, 255, Imgproc.THRESH_OTSU);
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_GRAY2BGR);

        Mat canny_output = new Mat();
        Imgproc.Canny(src_gray, canny_output, thresh, thresh * 2);              //поиск границ
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();                         //массив контуров
        Mat hierarchy = new Mat();                                                       //иерархия контуров
        Imgproc.findContours(canny_output, contours, hierarchy, 0, 1);      //поиск контуров
        Mat res = Mat.zeros(canny_output.size(), CvType.CV_8UC3);                        //итоговое изображение с контурами

        double[] green = {0, 255, 0};

        List<Point> all_contours = new ArrayList<Point>();       //массив всех контуров
        for (MatOfPoint current_contour : contours) {            //заполнение массива
            all_contours.addAll(current_contour.toList());
        }

        for (Point p : all_contours) {
            res.put((int) p.y, (int) p.x, green);                 //прорисовка контуров
        }

        MatOfPoint all_cont_mat = new MatOfPoint();
        all_cont_mat.fromList(all_contours);

        Rect boundingRect = Imgproc.boundingRect(all_cont_mat);                //прямоугольник вокруг контура
        Imgproc.rectangle(res, boundingRect, new Scalar(255, 0, 0));

        List<MatOfPoint> hull_list = new ArrayList<>();                        //выпуклая оболочка
        MatOfPoint contour = all_cont_mat;
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(contour, hull);                                     //поиск выпуклой оболочки

        Point[] points_of_hull = new Point[hull.rows()];
        Point[] array_of_contour = contour.toArray();
        List<Integer> hull_ind = hull.toList();
        for (int i = 0; i < hull_ind.size(); i++) {
            points_of_hull[i] = array_of_contour[hull_ind.get(i)];
        }
        hull_list.add(new MatOfPoint(points_of_hull));

        for (int i = 0; i < hull_list.size(); i++) {
            Scalar color = new Scalar(0, 0, 255);
            Imgproc.drawContours(res, hull_list, i, color);                   //прорисовка оболочки
        }


        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", res, matOfByte);        //перевод изображения в байтовую матрицу

        byte[] byteArray = matOfByte.toArray();                //перевод байтовой матрицы в массив

        InputStream in = new ByteArrayInputStream(byteArray);  //перевод в буферизованное изображение
        BufferedImage bufImage = ImageIO.read(in);

        JFrame frame = new JFrame();                           //создаем окно

        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));   //показвваем изображение
        frame.pack();
        frame.setVisible(true);
    }
}
