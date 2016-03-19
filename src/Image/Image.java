package Image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class Image {

	int height, width;         //Size of the image
	HashMap<String, RGB> colors;
	String filePath;
	String fileName;
	String url;


	public class RGB
	{
		
		int red, blue, green;
			
		public RGB(int red, int green, int blue)
		{
			this.red = red;
			this.blue = blue;
			this.green = green;
		
		}
		
		public int getRed()
		{
			return red;
		}
		
		public int getGreen()
		{
			return green;
		}
		
		public int getBlue()
		{
			return blue;
		}
		public String toString()
		{
			return getRed() + " - " + getGreen() + " - " + getBlue();
		}
	}
	
	public Image()
	{
		this.width = 0;
		this.height = 0;
		this.fileName = "";
		this.filePath = "";
		this.colors = null;
	};
	
	
	//Regular image loading
	public Image(File file)
	{
		BufferedImage img = null;
		try
		{
			String path = file.getAbsolutePath().replace("\\", "/");
			img = ImageIO.read(new FileInputStream(path));
			this.width = img.getWidth();
			this.height = img.getHeight();
			this.filePath = file.getPath();
			this.fileName = file.getName();
			this.colors = new HashMap<String, RGB>();
			for(int w = 0; w < this.width; w ++)
			{
				for(int h = 0; h < this.height; h ++)
				{
					int rgb = img.getRGB(w, h);
					this.colors.put(w + ":" + h, 
							new RGB((rgb >> 16) & 0x000000FF,
									(rgb >> 8) & 0x000000FF,
									(rgb) & 0x000000FF));
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Image(int height, int width, String filePath, String fileName, String url) {
		super();
		this.height = height;
		this.width = width;
		this.filePath = filePath;
		this.fileName = fileName;
		this.url = url;
	}


	//This also uploads to database
	public void uploadImage(File file, String url, DBCollection table)
	{
		/* This method uploads image metadata to the database
		 * 
		 */
		BufferedImage img = null;
		int width, height;
		String filePath, fileName;
		try
		{
			String path = file.getAbsolutePath().replace("\\", "/");
			img = ImageIO.read(new FileInputStream(path));
			width = img.getWidth();
			height = img.getHeight();
			filePath = file.getPath();
			fileName = file.getName();
			
			System.out.println("Name: " + fileName + 
					"\nPath: " + filePath +
					"\nWidth x Height: " + width + " x " + height);
			
			DBObject image = new BasicDBObject()
					.append("name", fileName)
					.append("url", url)
					.append("path", filePath)
					.append("width", width)
					.append("height", height);
			table.insert(image);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getHeight()
	{
		return height;
	}
	public int getWidth()
	{
		return width;
	}
	public HashMap<String, RGB> getColors()
	{
		return colors;
	}
	
	public String getFilePath()
	{
		return filePath;
	}

	public String getUrl() {
		return url;
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public String toString()
	{
		return "File name: " + getFileName() +
				"\nPath: " + getFilePath() + 
				"\nResolution: " + getHeight() + " x " + getWidth();
	}
	
}