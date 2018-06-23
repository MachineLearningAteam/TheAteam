


import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class imagefileloat{
   int[][] feald;
   feald = new int[100][100];
    String string = "map1.bmp";
    try {
		BufferedImage img = ImageIO.read(new File(string));
      //  FileInputStream fis = new FileInputStream("map1.bmp");
        System.out.println("Width:"  + img.getWidth());
        System.out.println("Height:" + img.getHeight());
        System.out.println(img);
        for(int i=0;i<100;i++){
            for(int j=0;j<100;j++){
                    feald  =img.getRGB(j, i);
                if (data==-1){
                    System.out.print("1");   
            }
            else{
                System.out.print("0"); 
            }

        }
        System.out.println();
    }
    } catch ( IOException e) { 
        
        System.err.println("no date");
        System.exit()
      }
       // fis.close();
    return feald;
    



}