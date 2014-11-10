package com.logica.ndk.tm.utilities.alto;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import com.logica.ndk.tm.utilities.alto.exception.ImageProcessingException;

/**
 * <p>
 * Methods used for gathering information about the image, which is inserted into a pdf document.
 * </p>
 * </p>X = horizontal, Y = vertical</p>
 * 
 * @author rasekl
 */
public interface Image {
  Point2D getSizeMetric() throws ImageProcessingException;

  Point2D getSizeInches() throws ImageProcessingException;

  Point getSizePixel() throws ImageProcessingException;

  Point2D getSizePoints() throws ImageProcessingException;

  Point2D getDPI() throws ImageProcessingException;

  Point2D getPPMM() throws ImageProcessingException;

  BufferedImage getImage() throws ImageProcessingException;
}
