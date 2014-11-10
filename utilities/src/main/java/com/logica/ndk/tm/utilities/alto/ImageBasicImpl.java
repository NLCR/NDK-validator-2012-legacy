package com.logica.ndk.tm.utilities.alto;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.utilities.alto.exception.ImageProcessingException;

/**
 * Helps to gather important information about the image, that is inserted into the pdf.
 * 
 * @author rasekl
 */
public class ImageBasicImpl implements Image {

  protected static final Logger defaultLogger = Logger.getLogger(Image.class.getName());

  protected XPath _xpath;
  protected DOMImplementationRegistry _domImplRegistry;
  protected String _imagePath;
  protected String _imageFileSuffix;
  protected String _imageIoFormatName;
  protected Logger _logger;
  protected Document _metaDoc;
  protected Point2D _sizeMetric;
  protected Point2D _sizeInch;
  protected Point2D _sizePoints;
  protected Point _sizePixel;
  protected Point2D _dpi;
  protected Point2D _ppmm;
  protected BufferedImage _image;

  /**
   * Helps to gather important information about an image (<b>DPI</b>, <b>px</b> size, <b>pt</b> size, ...), that is
   * inserted into the pdf.
   * 
   * @param imagePath
   *          - String
   * @return void
   * @throws ImageProcessingException
   */
  public ImageBasicImpl(String imagePath)
      throws ImageProcessingException
  {
    try {
      init(XPathFactory.newInstance().newXPath(), DOMImplementationRegistry.newInstance(), "javax_imageio_1.0", defaultLogger, imagePath);
    }
    catch (ClassCastException e) {
      throw new ImageProcessingException("Problem while using defaults for Image instance", e);
    }
    catch (ClassNotFoundException e) {
      throw new ImageProcessingException("Problem while using defaults for Image instance", e);
    }
    catch (IllegalAccessException e) {
      throw new ImageProcessingException("Problem while using defaults for Image instance", e);
    }
    catch (InstantiationException e) {
      throw new ImageProcessingException("Problem while using defaults for Image instance", e);
    }
  }

  public ImageBasicImpl(XPath xpath, DOMImplementationRegistry domImplRegistry, String imageIoFormatName, Logger logger, String imagePath)
      throws ImageProcessingException
  {
    init(_xpath, _domImplRegistry, _imageIoFormatName, _logger, _imagePath);
  }

  public void init(XPath xpath, DOMImplementationRegistry domImplRegistry, String imageIoFormatName, Logger logger, String imagePath)
      throws ImageProcessingException
  {
    _xpath = xpath;
    _imagePath = imagePath;
    _domImplRegistry = domImplRegistry;
    _imageFileSuffix = _imagePath.substring(_imagePath.lastIndexOf('.') + 1).toUpperCase();
    _imageIoFormatName = imageIoFormatName;
    _logger = logger;
    _metaDoc = null;
    open();
  }

  protected void open() throws ImageProcessingException {
    try {
      int imageIndex = 0;
      RandomAccessFile raf = new RandomAccessFile(_imagePath, "r");
      ImageReader rdr = ImageIO.getImageReadersBySuffix(_imageFileSuffix).next();
      rdr.setInput(new FileImageInputStream(raf), false, false);
      IIOMetadata meta = rdr.getImageMetadata(imageIndex);
      Node iioNode = meta.getAsTree(_imageIoFormatName);
      DOMImplementationLS lsImpl = (DOMImplementationLS) _domImplRegistry.getDOMImplementation("LS");
      LSSerializer ser = lsImpl.createLSSerializer();
      String docStr = ser.writeToString(iioNode);
      System.out.println(docStr);
      DocumentBuilderFactory dbf = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
      dbf.setNamespaceAware(true);
      DocumentBuilder bldr = dbf.newDocumentBuilder();
      _metaDoc = bldr.parse(new InputSource(new StringReader(docStr)));

      //ppmm
      String ppmmH = _xpath.evaluate("/javax_imageio_1.0/Dimension/HorizontalPixelSize/@value", _metaDoc);
      String ppmmV = _xpath.evaluate("/javax_imageio_1.0/Dimension/VerticalPixelSize/@value", _metaDoc);
      assert ppmmH != null : "missing HorizontalPixelSize";
      assert ppmmV != null : "missing VerticalPixelSize";
      _ppmm = new Point2D.Double(1.0 / Double.parseDouble(ppmmH), 1.0 / Double.parseDouble(ppmmV));

      //pixels
      _sizePixel = new Point(rdr.getWidth(imageIndex), rdr.getHeight(imageIndex));

      //DPI
      _dpi = new Point2D.Double(_ppmm.getX() * 25.4, _ppmm.getY() * 25.4);

      //size mm
      _sizeMetric = new Point2D.Double(_sizePixel.getX() / _ppmm.getX(), _sizePixel.getY() / _ppmm.getY());

      //size pt
      _sizePoints = new Point2D.Double(
          UnitConverter.mm2pt(_sizeMetric.getX()),
          UnitConverter.mm2pt(_sizeMetric.getY()));

      //size inch
      _sizeInch = new Point2D.Double(_sizePixel.getX() / _dpi.getX(), _sizePixel.getY() / _dpi.getY());

      //image
      _image = rdr.read(imageIndex);
    }
    catch (Exception e) {
      throw new ImageProcessingException(String.format("Problem while opening image %s", _imagePath), e);
    }
  }

  @Override
  public Point2D getSizeMetric() {
    return _sizeMetric;
  }

  @Override
  public Point2D getSizeInches() {
    return _sizeInch;
  }

  @Override
  public Point getSizePixel() {
    return _sizePixel;
  }

  @Override
  public Point2D getDPI() {
    return _dpi;
  }

  @Override
  public Point2D getPPMM() {
    return _ppmm;
  }

  @Override
  public BufferedImage getImage() {
    return _image;
  }

  @Override
  public Point2D getSizePoints() throws ImageProcessingException {
    return _sizePoints;
  }
}
