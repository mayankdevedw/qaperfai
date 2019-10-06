package com.qaperf.ai.utils;


import com.qaperf.ai.xpathparser.JSHelpers;
import com.qaperf.ai.xpathparser.TreePathExtractor;
import org.apache.log4j.Logger;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ComputerVision {

    private static Logger LOG=Logger.getLogger(ComputerVision.class);
    static float nndrRatio = 1.0f;

    public void saveScreenshot(WebDriver d, String filename) {

        try {
            BufferedImage img =new AShot().shootingStrategy(ShootingStrategies.viewportRetina(100, 0, 0,2)).coordsProvider(new WebDriverCoordsProvider())
                    .takeScreenshot(d).getImage();
            ImageIO.write(img, "png", new File(filename));
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }


    public void saveVisualLocator(WebDriver d, String s, WebElement we, String vl) {

        try {
           getUniqueVisualLocator(d, s, we, vl);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void getUniqueVisualLocator(WebDriver d, String filename, WebElement element, String webElementImageName) throws IOException {

        File destFile = new File(filename);
        BufferedImage img = ImageIO.read(destFile);

        File visualLocator = new File(webElementImageName);

        int scale = 0;
        getScaledSubImage(d, element, visualLocator);

        while (!isUnique(destFile.getAbsolutePath(), visualLocator.getAbsolutePath())) {
            scale += 2;
            if (scale == 6) {
                saveVisualCrop(d, filename, element, webElementImageName);
                return;
            } else {
                getScaledSubImage(d, img, element, visualLocator, scale);
            }
        }

    }

    private void getScaledSubImage(WebDriver driver, WebElement element, File visualLocator){

        try {
            highlightElement(element, driver);
            BufferedImage img =new AShot().shootingStrategy(ShootingStrategies.viewportRetina(100, 0, 0,2)).coordsProvider(new WebDriverCoordsProvider())
                    .takeScreenshot(driver, element).getImage();
            ImageIO.write(img, "png", visualLocator);
        } catch (InterruptedException | IOException e1) {
            e1.printStackTrace();
        }

    }


    private void saveVisualCrop(WebDriver d, String s, WebElement we, String vl) {

        try {
            getPreciseElementVisualCrop(d, s, we, vl);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void getPreciseElementVisualCrop(WebDriver d, String filename, WebElement element, String webElementImageName) throws IOException {

        File destFile = new File(filename);
        BufferedImage img = ImageIO.read(destFile);

        File visualLocator = new File(webElementImageName);

        getPreciseSubImage(d, img, element, visualLocator);

    }

    public String detect(WebDriver d, String templateFile, By by) {

        saveScreenshot(d, templateFile + "_temp.png");
        List<Integer> point = detect(templateFile + "_temp.png", templateFile);
        if (point.size() == 2) {
            WebElement targetElement = (WebElement) ((JavascriptExecutor) d).executeScript(
                    "return document.elementFromPoint(arguments[0], arguments[1])", point.get(0), point.get(1));

            if (targetElement == null) {
                // element may be outside viewport, attempt to scroll and find it using custom
                // javascript
                  ((JavascriptExecutor) d).executeScript(
                        JSHelpers.SCROLL_TO_ELEMENT_OUTSIDE_VIEWPORT.getValue(), point.get(0), point.get(1));

            }
            return TreePathExtractor.suggestNewXpath(d, targetElement, by);

        }
        return null;

    }




    private List<Integer> detect(String imageFile, String templateFile) {

        Point result = null;

        Set<Point> allMatches = new HashSet<>();

        /* run SIFT and FAST to check for the presence/absence of the template image. */
        boolean isPresent = true;

        if (isPresent) {
            result = matchWithTemplateFile(templateFile, imageFile);
        }
        int x = Integer.parseInt(String.valueOf(result.x + 1).split("\\.")[0]);
        int y = Integer.parseInt(String.valueOf(result.y + 1).split("\\.")[0]);
        return Arrays.asList(x, y);

    }
    private Point matchWithTemplateFile(String templateFile, String imageFile) {

        LOG.debug("Searching the template position in the reference image");


        Mat img = Imgcodecs.imread(imageFile);
        Mat templ = Imgcodecs.imread(templateFile);


        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        /* Do the Matching and Normalize. */
        Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());



        List<Point> matches = new LinkedList<Point>();

        for (int i = 0; i < result_rows; i++) {
            for (int j = 0; j < result_cols; j++) {

                if (result.get(i, j)[0] >= 0.99) {
                    matches.add(new Point(i, j));
                }
            }
        }


        if (matches.size() == 0) {
            LOG.error("WARNING: No matches found!");
        } else if (matches.size() > 1) {
            LOG.error("WARNING: Multiple matches: " + matches.size());
        }

        MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;

        return new Point(matchLoc.x + templ.cols() / 2, matchLoc.y + templ.rows() / 2);
    }




    private void getScaledSubImage(WebDriver driver, BufferedImage img, WebElement element, File visualLocator, int scale) throws IOException {

        org.openqa.selenium.Point elementCoordinates = null;

        try {
            elementCoordinates = element.getLocation();
        } catch (StaleElementReferenceException e) {
            LOG.error("Element State has been changed");
        }

        try {
            highlightElement(element, driver);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        Rectangle rect = new Rectangle(width, height);
        BufferedImage subImage = null;

        int min_offset_x = Math.min(element.getLocation().getX(), img.getWidth() - rect.width - element.getLocation().getX());
        int min_offset_y = Math.min(element.getLocation().getY(), img.getHeight() - rect.height - element.getLocation().getY());
        int offset = Math.min(min_offset_x, min_offset_y);


        offset = offset / scale;


        try {
            if (element.getTagName().equals("option")) {

                WebElement thisShouldBeTheSelect = element.findElement(By.xpath(".."));
                new Actions(driver).moveToElement(thisShouldBeTheSelect).perform();

                elementCoordinates = thisShouldBeTheSelect.getLocation();
                subImage = img.getSubimage(elementCoordinates.getX() - offset, elementCoordinates.getY() - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            } else {
                subImage = img.getSubimage(elementCoordinates.getX() - offset, elementCoordinates.getY() - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            }
        } catch (RasterFormatException e) {
            LOG.error("WARNING: " + e.getMessage());
        }

        ImageIO.write(subImage, "png", visualLocator);
        subImage.flush();

    }


    private void getPreciseSubImage(WebDriver driver, BufferedImage img, WebElement element, File visualLocator) throws IOException {

        org.openqa.selenium.Point elementCoordinates = null;

        try {
            elementCoordinates = element.getLocation();
        } catch (StaleElementReferenceException e) {
            LOG.info("test might have changed its state");
        }

        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        Rectangle rect = new Rectangle(width, height);
        BufferedImage subImage = null;

        int offset = 0;

        try {
            if (element.getTagName().equals("option")) {

                WebElement thisShouldBeTheSelect = element.findElement(By.xpath(".."));
                new Actions(driver).moveToElement(thisShouldBeTheSelect).perform();

                if (LOG.isDebugEnabled()) {
                    LOG.error("\n\nthisShouldBeTheSelect.getLocation(): " + thisShouldBeTheSelect.getLocation());
                    LOG.error("element.getLocation(): " + element.getLocation());
                }

                elementCoordinates = thisShouldBeTheSelect.getLocation();
                subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            } else {
                subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            }
        } catch (RasterFormatException e) {
            LOG.error("WARNING: " + e.getMessage());
        }

        ImageIO.write(subImage, "png", visualLocator);
        subImage.flush();

    }


    private  void highlightElement(WebElement element, WebDriver driver) throws InterruptedException {

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: yellow; border: 2px solid yellow;");
        Thread.sleep(100);
        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");

    }

   private boolean isUnique(String inFile, String templateFile) {
        //Instantiating the Imgcodecs class
        Imgcodecs imageCodecs = new Imgcodecs();
        Mat img = imageCodecs.imread(inFile);
        Mat templ = imageCodecs.imread(templateFile);

        // / Create the result matrix
        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // Do the Matching and Normalize
        Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        List<Point> matches = new LinkedList<>();

        for (int i = 0; i < result_rows; i++) {
            for (int j = 0; j < result_cols; j++) {
                if (result.get(i, j)[0] >= 0.99)
                    matches.add(new Point(i, j));
            }
        }

       MinMaxLocResult mmr = Core.minMaxLoc(result);
       Point matchLoc = mmr.maxLoc;

       /* Show me what you got. */
       Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()), new Scalar(0, 255, 0), 2);



       /* Save the visualized detection. */
       File annotated = new File("/tmp/img.png");
       Imgcodecs.imwrite(annotated.getPath(), img);
        if (matches.size() == 0) {
            LOG.error("WARNING: No matches found!");
            return false;
        } else if (matches.size() > 1) {
            LOG.error("WARNING: Multiple matches!");
            return false;
        } else
            return true;

    }

    /*
     * Run the FAST and SIFT feature detector algorithms on the two input images and
     * try to match the features found in @object image into the @scene image
     *
     */
    private boolean runFeatureDetection(String templ, String img, Set<Point> allMatches) {
        boolean sift = siftDetector(templ, img, allMatches);
        boolean fast = fastDetector(templ, img, allMatches);

        boolean res = sift || fast;

        if (res) {
            System.out.println("[LOG]\tTemplate Present");
        }
        return res;
    }

    /*
     * Run the FAST feature detector algorithms on the two input images and try to
     * match the features found in @object image into the @scene image
     *
     */
    private boolean fastDetector(String object, String scene, Set<Point> allMatches) {

        // System.out.println("FAST Detector");
        Mat objectImage = Imgcodecs.imread(object, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Mat sceneImage = Imgcodecs.imread(scene, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.FAST);
        featureDetector.detect(objectImage, objectKeyPoints);
//		System.out.println("[LOG]\tFAST: Detecting key-points in templage image");

        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);
//		System.out.println("[LOG]\tFAST: Computing descriptors in templage image");

        /* Create output image. */
        Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Scalar newKeypointColor = new Scalar(255, 0, 0);

        Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

        /* Match object image with the scene image. */
        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
//		System.out.println("[LOG]\tFAST: Detecting key-points in reference image");
        featureDetector.detect(sceneImage, sceneKeyPoints);
//		System.out.println("[LOG]\tFAST: Computing descriptors in reference image");
        descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

        Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Scalar matchestColor = new Scalar(0, 255, 0);

        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
//		System.out.println("[LOG]\tFAST: Matching descriptors");
        descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

        // System.out.println("Calculating good match list...");
        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

        for (int i = 0; i < matches.size(); i++) {
            MatOfDMatch matofDMatch = matches.get(i);
            DMatch[] dmatcharray = matofDMatch.toArray();
            DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];

            if (m1.distance <= m2.distance * nndrRatio) {
                goodMatchesList.addLast(m1);
            }
        }

        if (goodMatchesList.size() == 0) {
            return false;
        }

        // System.out.println("Good matches (FAST): " + goodMatchesList.size());

        int min_accepted_matches = (int) (objectKeyPoints.toList().size() * 0.3);

        // System.out.println("Min matches (FAST): " + min_accepted_matches);

        if (goodMatchesList.size() > min_accepted_matches) {

            // System.out.println("Object Found!");

            List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
            List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

            LinkedList<Point> objectPoints = new LinkedList<Point>();
            LinkedList<Point> scenePoints = new LinkedList<Point>();

            for (int i = 0; i < goodMatchesList.size(); i++) {
                objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
                scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
            }

            // add the scenepoints to list of all matching points
            if (allMatches != null) {
                allMatches.addAll(scenePoints);
                // System.out.println(scenePoints);
            }

            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
            objMatOfPoint2f.fromList(objectPoints);
            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
            scnMatOfPoint2f.fromList(scenePoints);

            /* output filename. */
            String filename = object.toString();
            int i = filename.lastIndexOf("/");
            filename = filename.substring(i + 1, filename.length());
            filename = filename.replace(".png", "");

            /* visualize detected features. */
            Imgcodecs.imwrite("output/templateMatching/FAST-" + filename + "-outputImage.jpg", outputImage);

            /* Get the rectangle the the potential match is. */
            try {
                Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.LMEDS, 3);
                Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
                Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

                obj_corners.put(0, 0, new double[] { 0, 0 });
                obj_corners.put(1, 0, new double[] { objectImage.cols(), 0 });
                obj_corners.put(2, 0, new double[] { objectImage.cols(), objectImage.rows() });
                obj_corners.put(3, 0, new double[] { 0, objectImage.rows() });

                // System.out.println("Transforming object corners to scene corners...");
                Core.perspectiveTransform(obj_corners, scene_corners, homography);

                Mat img = Imgcodecs.imread(scene, Imgcodecs.CV_LOAD_IMAGE_COLOR);

                Imgproc.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(255, 0, 0), 2);
                Imgproc.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(255, 0, 0), 2);
                Imgproc.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(255, 0, 0), 2);
                Imgproc.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(255, 0, 0), 2);

                // System.out.println("Drawing matches image...");
                MatOfDMatch goodMatches = new MatOfDMatch();
                goodMatches.fromList(goodMatchesList);

                Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);

                /* visualize feature detection. */
                Imgcodecs.imwrite("output/templateMatching/FAST-" + filename + "-matchoutput.jpg", matchoutput);
                Imgcodecs.imwrite("output/templateMatching/FAST-" + filename + "-img.jpg", img);
            } catch (Exception e) {
               LOG.error("Homography not found");
            }

            return true;

        } else {
            // System.out.println("Object Not Found");
            return false;
        }

    }


    private  boolean siftDetector(String object, String scene, Set<Point> allMatches) {

        // System.out.println("SIFT Detector");
        Mat objectImage = Imgcodecs.imread(object, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Mat sceneImage = Imgcodecs.imread(scene, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
        featureDetector.detect(objectImage, objectKeyPoints);
        System.out.println("[LOG]\tDetecting key-points in templage image");

        MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);
        System.out.println("[LOG]\tComputing descriptors in template image");


        Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Scalar newKeypointColor = new Scalar(255, 0, 0);

        Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

        /* Match object image with the scene image. */
        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
        System.out.println("[LOG]\tDetecting key-points in reference image");
        featureDetector.detect(sceneImage, sceneKeyPoints);
        System.out.println("[LOG]\tComputing descriptors in reference image");
        descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

        Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Scalar matchestColor = new Scalar(0, 255, 0);

        List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        System.out.println("[LOG]\tMatching descriptors");
        descriptorMatcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);

        // System.out.println("Calculating good match list...");
        LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

        for (int i = 0; i < matches.size(); i++) {
            MatOfDMatch matofDMatch = matches.get(i);
            DMatch[] dmatcharray = matofDMatch.toArray();
            DMatch m1 = dmatcharray[0];
            DMatch m2 = dmatcharray[1];

            if (m1.distance <= m2.distance * nndrRatio) {
                goodMatchesList.addLast(m1);
            }
        }

        if (goodMatchesList.size() == 0) {
            return false;
        }



        int min_accepted_matches = (int) (objectKeyPoints.toList().size() * 0.3);



        if (goodMatchesList.size() > min_accepted_matches) {

            // System.out.println("[LOG]\tTemplate Present!");

            List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
            List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

            LinkedList<Point> objectPoints = new LinkedList<Point>();
            LinkedList<Point> scenePoints = new LinkedList<Point>();

            for (int i = 0; i < goodMatchesList.size(); i++) {
                objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
                scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
            }

            if (allMatches != null) {
                allMatches.addAll(scenePoints);

            }

            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
            objMatOfPoint2f.fromList(objectPoints);
            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
            scnMatOfPoint2f.fromList(scenePoints);

            /* output filename. */
            String filename = object.toString();
            int index = filename.lastIndexOf("/");
            filename = filename.substring(index + 1, filename.length());
            filename = filename.replace(".png", "");


            Imgcodecs.imwrite("output/templateMatching/SIFT-" + filename + "-outputImage.jpg", outputImage);

            try {

                Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);
                Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
                Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

                obj_corners.put(0, 0, new double[] { 0, 0 });
                obj_corners.put(1, 0, new double[] { objectImage.cols(), 0 });
                obj_corners.put(2, 0, new double[] { objectImage.cols(), objectImage.rows() });
                obj_corners.put(3, 0, new double[] { 0, objectImage.rows() });


                Core.perspectiveTransform(obj_corners, scene_corners, homography);

                Mat img = Imgcodecs.imread(scene, Imgcodecs.CV_LOAD_IMAGE_COLOR);

                Imgproc.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(255, 0, 0), 2);
                Imgproc.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(255, 0, 0), 2);
                Imgproc.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(255, 0, 0), 2);
                Imgproc.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(255, 0, 0), 2);


                MatOfDMatch goodMatches = new MatOfDMatch();
                goodMatches.fromList(goodMatchesList);

                Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);

                filename = object.toString();
                index = filename.lastIndexOf("/");
                filename = filename.substring(index + 1, filename.length());
                filename = filename.replace(".png", "");

                Imgcodecs.imwrite("output/templateMatching/SIFT-" + filename + "-matchoutput.jpg", matchoutput);
                Imgcodecs.imwrite("output/templateMatching/SIFT-" + filename + "-img.jpg", img);

            } catch (Exception e) {
                System.out.println("[LOG]\tHomography not found");
            }

            return true;

        } else {
            // System.out.println("Object Not Found");
            return false;
        }

    }
}
