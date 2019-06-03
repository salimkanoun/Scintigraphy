import ij.ImagePlus;
import ij.io.Opener;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SalimTryingInjectImage extends JPanel {
	private static final long serialVersionUID = 1L;
	
	Image image2;
	
	public SalimTryingInjectImage() {
		
	}
	private void testprint() throws IOException {
		@SuppressWarnings("unused")
		byte[] bytesPixel=extractBytes(null);
		
		 // open image
		Opener opener = new Opener();
		opener.setSilentMode(true);
		ImagePlus imp=opener.openImage("C:\\Users\\kanoun_s\\ownCloud2\\IUT-Informatique\\Images scinti2\\Exemples Dicoms\\DPD\\ANT3h001_DS.dcm");
		imp.show();
		@SuppressWarnings("unused")
		int depth= imp.getProcessor().getBitDepth();
		
		short[] pixels;
		 pixels =(short[]) imp.getStack().getProcessor(1).getPixels();
		
//		if(depth==8) {
//			byte[] pixels =(byte[]) imp.getProcessor().getPixels();
//		}else if(depth==16) {
//			short[] pixels =(short[]) imp.getProcessor().getPixels();
//		}else if(depth==32) {
//			float[] pixels =(float[]) imp.getProcessor().getPixels();
//		}
		
	
		
		MemoryImageSource image = new MemoryImageSource(256, 1024, makeColorModel(), ShortToByte_ByteBuffer_Method(pixels), 0, 0);
		//Image img = createImage(new MemoryImageSource(w, h, pixels, 0, w));
	
		image2=createImage(image);
		
		
	}
	
	byte [] ShortToByte_ByteBuffer_Method(short [] input)
	{
	  int index;
	  int iterations = input.length;

	  ByteBuffer bb = ByteBuffer.allocate(input.length * 2);

	  for(index = 0; index != iterations; ++index)
	  {
	    bb.putShort(input[index]);    
	  }

	  return bb.array();       
	}
	
	
	public void paint(Graphics g) {
        g.drawImage(image2, 0, 0, this);

    }
	
	
	public byte[] extractBytes ( BufferedImage bufferedImage) throws IOException {
		 // open image
		 File imgPath = new File("C:\\Users\\kanoun_s\\Pictures\\diego\\3.png");
		 BufferedImage bufferedImage2 = ImageIO.read(imgPath);

		 // get DataBufferBytes from Raster
		 WritableRaster raster = bufferedImage2.getRaster();
		 DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();
		 System.out.println(data.getDataType());
		 System.out.println(data.getNumBanks());
		 return ( data.getData() );
		}

	private ColorModel makeColorModel() {
		ColorModel cm1;
		byte[] rLUT = new byte[256];
		byte[] gLUT = new byte[256];
		byte[] bLUT = new byte[256];

		for (int i = 0; i < 256; i++) {
			rLUT[i] = (byte) i;
			gLUT[i] = (byte) i;
			bLUT[i] = (byte) i;
		}
		
		cm1 = new IndexColorModel(8, 256, rLUT, gLUT, bLUT);
		return cm1;

	}
	
	public static void main(String[] arg) throws IOException {
		SalimTryingInjectImage tab=new SalimTryingInjectImage();
		JFrame frame=new JFrame();
		frame.add(tab);
		frame.setVisible(true);
		tab.testprint();
	}

}
