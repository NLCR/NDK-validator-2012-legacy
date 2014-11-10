package com.logica.ndk.tm.utilities.transformation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;


public class ImageIOConvertor {
    
  public static void jpg2tiff(File source, File target) throws Exception {
//    IIORegistry registry = IIORegistry.getDefaultInstance();  
//    registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi());  
//    registry.registerServiceProvider(new com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi()); 
    BufferedImage bi =loadJPEG(source);
    saveTiff(target, bi);
  }
  
  protected static BufferedImage loadJPEG(File f) throws Exception {
    return ImageIO.read(f);
  }
  
  protected static void saveTiff(File f, BufferedImage image) throws Exception {

      ImageOutputStream ios = null;
      
      ImageWriter writer = new TIFFImageWriterSpi().createWriterInstance();//getWriter("TIF");
      
      // setup writer
      ios = ImageIO.createImageOutputStream(f);
      writer.setOutput(ios);
      TIFFImageWriteParam writeParam = new TIFFImageWriteParam(Locale.ENGLISH);
      writeParam.setCompressionMode(ImageWriteParam.MODE_DISABLED);
      //writeParam.setCompressionType("PackBits");

      // convert to an IIOImage
      IIOImage iioImage = new IIOImage(image, null, null);

      // write it!
      writer.write(null, iioImage, writeParam);
  }  
  
  static ImageWriter getWriter(String formatName) throws Exception {
    Iterator it = ImageIO.getImageWritersByFormatName("TIF");
    if (it.hasNext()) {
      return (ImageWriter)it.next();
    } else {
      throw new Exception("No matching writer found for format " + formatName);
    }
    
  }
  
  public static void main(String[] args) {
    File source = new File("D:\\personal\\Martina\\", "sekvencak1.JPG");
    File target = new File("C:\\TEMP\\sekvencatk1.tiff");
    
    Iterator it = ImageIO.getImageWritersByFormatName("TIF");
    while(it.hasNext()) {
      System.out.println(it.next().getClass().getName());
    }
    
    System.out.println("Started");
    ImageIOConvertor c = new ImageIOConvertor();
    try {
      jpg2tiff(source, target);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Finished");
  }
  
}
