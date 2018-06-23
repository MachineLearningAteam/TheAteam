import java.awt.Color;
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

      //Color data1_cl=new Color(data1);
      //Color data2_cl=new Color(data2);
      //System.out.println((data1_cl.getRed()));  
      //System.out.println((data2_cl.getRed()));//.getElem(i) + ", "
            for(int i=0;i<100;i++){
                for(int j=0;j<100;j++){
                    int data =img.getRGB(j, i);
                    Color data1_cl=new Color(data);
                    
                    if (  data1_cl.getBlue()==255 && data1_cl.getRed()==255  &&  data1_cl.getGreen()==255){
                        System.out.print("1"); 
                        feald[j][i]  =1000;//壁
                    }else if (  data1_cl.getBlue()==255 ){
                         System.out.print("2"); 
                         feald[j][i]  = 2000;//巣
                    }else if (  data1_cl.getRed()>0 ){
                        System.out.print("3"); 
                        feald[j][i]  = data1_cl.getRed();//巣
                     }else{
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