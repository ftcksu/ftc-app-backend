package com.ftcksu.app.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {

    // Source: https://www.codejava.net/java-se/graphics/how-to-resize-images-in-java
    public static BufferedImage resize(BufferedImage inputImage, int scaledWidth,
                                       int scaledHeight) {
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return outputImage;
    }

    public static BufferedImage resize(BufferedImage inputImage, double percent) {
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        return resize(inputImage, scaledWidth, scaledHeight);
    }

    // Resizes the image to width 1920 while keeping the aspect-ratio.
    public static BufferedImage resize(BufferedImage inputImage) {
        double percent = (double) inputImage.getHeight() / inputImage.getWidth();
        int scaledHeight = (int) (1920 * percent);
        return resize(inputImage, 1920, scaledHeight);
    }

}
