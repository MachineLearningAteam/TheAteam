
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Imagefileload{
    int[][] get_feald(){
        int[][] feald;
        feald = new int[100][100];
        String string = "map1.bmp";
        //System.out.println("load_start");
        try {
		    BufferedImage img = ImageIO.read(new File(string));
      //  FileInputStream fis = new FileInputStream("map1.bmp");
            System.out.println("Width:"  + img.getWidth());
            System.out.println("Height:" + img.getHeight());
      //  System.out.println(img);
            for(int i=0;i<100;i++){
                for(int j=0;j<100;j++){
                    int data =img.getRGB(j, i);
                    if (data==-1){
                        System.out.print("1"); 
                        feald[j][i]  =1;
                    }
                    else{
                        System.out.print("0"); 
                        feald[j][i]  =0;
                    }
                }
                System.out.println("");
        
            }
        } catch ( IOException e) { 
        
            System.err.println("no date");
        //return 0;
       // System.exit()
        }
       // fis.close();
  
	    return feald;
    }
}