/* ###################################################################
 * 
 *  FILE: "ImageUtils.java"
 *                                    created: 2006-02-07 16:25:00 
 *                                last update: 2008-03-21 14:41:19 
 *  Author: Frédéric Boulanger
 *  E-mail: Frederic.Boulanger@supelec.fr
 *    mail: Supélec - Département Informatique
 *          3 rue Joliot-Curie, 91192 Gif-sur-Yvette, France
 *     www: http://wwwsi.supelec.fr/fb/
 *  
 *  Description: 
 * 
 *  History
 * 
 *  modified   by  rev reason
 *  ---------- --- --- -----------
 *  2006-02-08 FBO 1.0 original
 * ###################################################################
 */

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Iterator;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.imageio.metadata.IIOMetadata;

/**
 * Utility class whose static methods allow reading and writing
 * of image files as supported by the javax.imageio package.
 */
public class ImageUtils {
  /********************/
  /*  Reading images  */
  /********************/

  /** Read an image from a file */
  public static BufferedImage read(File f) throws IOException, FileNotFoundException {
    return getImage(readAll(f).getRenderedImage());
  }
  
  /** Read an image, its thumbnails and its metadata from a file */
  public static IIOImage readAll(File f) throws IOException, FileNotFoundException {
    return read_(f);
  }

  /** Read an image from an input stream */
  public static BufferedImage read(InputStream in) throws IOException {
    return getImage(readAll(in).getRenderedImage());
  }
  
  /** Read an image, its thumbnails and its metadata from an input stream */
  public static IIOImage readAll(InputStream in) throws IOException {
    return read_(in);
  }

  /** Get an ImageReader that can read an image from the file */
  public static ImageReader getReader(File f) throws IOException {
    return get_reader_(f);
  }

  /** Get an ImageReader that can read an image from the input stream */
  public static ImageReader getReader(InputStream in) throws IOException {
    return get_reader_(in);
  }
  
  /** Get a BufferedImage from a RenderedImage */
  public static BufferedImage getImage(RenderedImage img) {
    try {
      if (Class.forName("java.awt.image.BufferedImage").isInstance(img)) {
        return (BufferedImage)img;
      }
    } catch (ClassNotFoundException cnfe) {
      throw new Error("Class java.awt.image.BufferedImage not found.");
    }
    
    Hashtable<String, Object> properties = null;
    String [] propnames = img.getPropertyNames();
    if ((propnames != null) && (propnames.length > 0)) {
      properties = new Hashtable<String, Object>();
      for (int i = 0; i < propnames.length; i++) {
        properties.put(propnames[i], img.getProperty(propnames[i]));
      }
    }
    BufferedImage buf = new BufferedImage(
                                          img.getColorModel(),
                                          img.copyData(null),
                                          false,
                                          properties);
    return buf;
  }
  
  private static ImageReader get_reader_(Object src) throws IOException {
    ImageInputStream in = ImageIO.createImageInputStream(src);
    Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
    if (!readers.hasNext()) {
      return null;
    }
    ImageReader reader = (ImageReader)(readers.next());
    reader.setInput(in);
    return reader;
  }
  
  private static IIOImage read_(Object src) throws IOException {
    ImageReader reader = get_reader_(src);
    if (reader == null) {
      throw new IllegalArgumentException("Unrecognized image format");
    }
    return reader.readAll(0, null);
  }
  
  /********************/
  /*  Writing images  */
  /********************/
  
  /** Get an estimate of the size in bytes of the image file obtained
   *  by saving <code>img</code> in format <code>format</code> with
   *  quality <code>quality</code> in progressive mode or not and with
   *  or without thumbnail.
   */
  public static int estimateSize(
                                 RenderedImage img, String format,
                                 float quality, boolean progressive)
  throws IllegalArgumentException {
    CounterImageOutputStream out = new CounterImageOutputStream();
    try {
      saveImage(img, out, format, quality, progressive, null);
      out.close();
    } catch (IOException ioe) {
      throw new Error("Impossible error: IO exception on CounterStream");
    }
    return out.size();
  }

  /** Save image <code>img</code> in file <code>f</code> using
   * format <code>format</code> with the given quality, 
   * progressive mode and thumbnail.
   */
  public static void saveImage(
                               RenderedImage img, File f, String format,
                               float quality, boolean progressive,
                               IIOMetadata meta)
  throws IOException, IllegalArgumentException {
    ImageOutputStream out = ImageIO.createImageOutputStream(
                                        new FileOutputStream(f));
    saveImage(img, out, format, quality, progressive, meta);
    out.close();
  }
  
  /** Save image <code>img</code> in stream <code>s</code> using
   * format <code>format</code> with the given quality, 
   * progressive mode and thumbnail.
   */
  public static void saveImage(
                               RenderedImage img, OutputStream s,
                               String format, float quality,
                               boolean progressive, IIOMetadata meta)
  throws IOException, IllegalArgumentException {
    ImageOutputStream out = ImageIO.createImageOutputStream(s);
    saveImage(img, out, format, quality, progressive, meta);
    out.close();
  }
  
  /** Save image <code>img</code> in image output stream <code>out</code>
   * using format <code>format</code> with the given quality, 
   * progressive mode and thumbnail.
   */
  public static void saveImage(
                               RenderedImage img, ImageOutputStream out,
                               String format, float quality,
                               boolean progressive, IIOMetadata meta)
  throws IOException, IllegalArgumentException {
    ImageWriter writer = get_writer_(out, format);
    if (writer == null) {
      throw new IllegalArgumentException("Unsupported image write format: '" + format + "'");
    }
    ImageWriteParam param = getWriteParam(writer, quality, progressive);
    writer.write(null, new IIOImage(img, null, meta), param);
  }

  /** Get an image writer ready to write an image to file <code>f</code>in 
   *  the given format, quality and progressive mode.
   */
  public ImageWriter getWriter(File f, String format)
  throws IOException, FileNotFoundException {
    ImageOutputStream out = ImageIO.createImageOutputStream(
                                       new FileOutputStream(f));
    return get_writer_(out, format);
  }
  
  /** Get an image writer ready to write an image to stream <code>s</code>in 
   *  the given format, quality and progressive mode.
   */
  public static ImageWriter getWriter(OutputStream s, String format)
  throws IOException {
    return get_writer_(ImageIO.createImageOutputStream(s), format);
  }
  
  public static ImageWriteParam getWriteParam(
                                              ImageWriter writer,
                                              float quality,
                                              boolean progressive) {
    ImageWriteParam param = writer.getDefaultWriteParam();
    if ((quality >= 0.0) && (quality <= 1.0)) {
      try {
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
      }
      catch (UnsupportedOperationException uoe) {
        // don't set quality if MODE_EXPLICIT is not supported
      }
    }
    if (param.canWriteProgressive()) {
      if (progressive) {
        param.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
      } else {
        param.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
      }
    }
    return param;
  }

  private static ImageWriter get_writer_(ImageOutputStream out, String format) {
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
    if (! writers.hasNext()) {
      return null;
    }
    ImageWriter writer = (ImageWriter)(writers.next());
    writer.setOutput(out);
    return writer;
  }

  public static void main(String [] args) {
    File in = new File(args[0]);
    File out = new File(args[1]);
    float qual = Float.parseFloat(args[3]);
    boolean prog = false;
    if (args.length > 4) {
      if (args[4].equals("1") || args[4].charAt(0) == 't') {
        prog = true;
      }
    }
    
    IIOImage img = null;
    try {
      img = readAll(in);
      saveImage(img.getRenderedImage(), out, args[2], qual, prog, img.getMetadata());
    } catch (IOException ioe) {
      System.err.println(ioe);
      System.exit(1);
    }
  }
}

/** An image output stream that just counts bytes written to it.
 *  Useful to estimate the size of an image file.
 */
class CounterImageOutputStream extends ImageOutputStreamImpl {
  private int size_;
  private int rpos_;
  
  public CounterImageOutputStream() {
    size_ = 0;
    rpos_ = 0;
  }
  
  public void write(int b) throws IOException {
    flushBits();
    size_ ++;
  }

  public void write(byte [] b, int off, int len) throws IOException {
    flushBits();
    size_ += len;
  }
  
  public int read() {
    if (rpos_ < size_) {
      rpos_++;
      return 0;
    } else {
      return -1;
    }
  }
  
  public int read(byte [] b, int off, int len) {
    if (rpos_ > size_) {
      return -1;
    } else if (rpos_ + len > size_) {
      return size_ - rpos_;
    } else {
      return len;
    }
  }
  
  public int size() {
    return size_;
  }
}
