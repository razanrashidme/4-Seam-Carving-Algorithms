import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SeamCarvingOptimized{

    // Calculate the energy for each pixel
    public static int[][] calculatePixelEnergy(BufferedImage image){
    
        int width =image.getWidth();
        int height =image.getHeight();
        int[][] energy =new int[height][width];

        for(int row=0; row<height ;row++){
            for(int col=0; col<width ;col++){
               // Get brightness for nearby pixels
               int a= getPixelBrightness(image, col-1 , row-1);  // Top-left pixel
               int b= getPixelBrightness(image, col , row-1);    // Top pixel
               int c= getPixelBrightness(image, col+1 , row-1);  // Top-right pixel
               int d= getPixelBrightness(image, col-1 , row);    // Left pixel
               int e= getPixelBrightness(image, col , row);      // Current pixel
               int f= getPixelBrightness(image, col+1 , row);    // Right pixel
               int g= getPixelBrightness(image, col-1, row+1);   // Bottom-left pixel
               int h= getPixelBrightness(image, col , row+1);    // Bottom pixel
               int i= getPixelBrightness(image, col+1 , row+1);  // Bottom-right pixel
               

               // Calculate horizontal and vertical energy
               int xEnergy= a + 2 * d + g - c - 2 * f - i;
               int yEnergy= a + 2 * b + c - g - 2 * h - i;

               // Calculate total energy
               energy[row][col]= (int) Math.sqrt(xEnergy * xEnergy + yEnergy * yEnergy);
          }
        }

         return energy;
      }

    // Get the brightness of a pixel
    private static int getPixelBrightness(BufferedImage image, int col, int row){
       int width= image.getWidth();
       int height= image.getHeight();

        // If the pixel is out of bounds, consider it as black(brightness 0)
        if( col < 0 || col >= width || row < 0 || row >= height){
            return 0;
        }

         // Get the color and return brightness(sum of the red, blue, and green values(RGB))
        Color color =new Color(image.getRGB(col, row));
        return color.getRed() + color.getGreen() + color.getBlue();
   }
    
    public static int[] findLowestEnergySeam(int[][] energy){
        int height = energy.length;
        int width = energy[0].length;
        double minEnergy = Double.POSITIVE_INFINITY;
        int[] bestSeam = new int[height];

        // Loop through each column in the first row
        for(int col = 0; col < width; col++){
            int[] seamPath = new int[height];
            int totalEnergy = energy[0][col];
            int currentCol = col;
            seamPath[0] = col;

            // Iterate through each row from 1 to height - 1
            for(int row = 1; row < height; row++){
                // Find the lowest energy among:
                // - Same column
                // - Left diagonal (currentCol - 1)
                // - Right diagonal (currentCol + 1)
                int left = (currentCol > 0) ? energy[row][currentCol - 1] : Integer.MAX_VALUE;
                int up = energy[row][currentCol];
                int right = (currentCol < width - 1) ? energy[row][currentCol + 1] : Integer.MAX_VALUE;

                // Choose the lowest energy pixel
                if(left <= up && left <= right){
                    currentCol -= 1;
                    totalEnergy += left;
                }else if (right <= up && right <= left){
                    currentCol += 1;
                    totalEnergy += right;
                }else{
                    totalEnergy += up;
                }

                seamPath[row] = currentCol;
            }

            // If the current seam has lower energy, update bestSeam
            if(totalEnergy < minEnergy){
                minEnergy = totalEnergy;
                bestSeam = seamPath;
            }
        }

        return bestSeam;
    }

    

    public static BufferedImage removeSeam(BufferedImage image, int[] seam){
        int height = image.getHeight();
        int width = image.getWidth();
        BufferedImage result = new BufferedImage(width - 1, height, image.getType());

        for(int row = 0; row < height; row++){
            int seamCol = seam[row];
            for(int col = 0; col < seamCol; col++){
                result.setRGB(col, row, image.getRGB(col, row));
            }
            for(int col = seamCol + 1; col < width; col++){
                result.setRGB(col - 1, row, image.getRGB(col, row));
            }
        }
        return result;
    }
    
    public static BufferedImage seamCarving(BufferedImage image, int targetWidth){
        int currentWidth = image.getWidth();
        BufferedImage carvedImage = image;

        // Repeat until the target width is reached
        while(currentWidth > targetWidth){
            // Compute the energy map (seam energy)
            int[][] energyMap = calculatePixelEnergy(carvedImage);

            // Find the lowest energy seam
            int[] seam = findLowestEnergySeam(energyMap);

            // Remove the seam from the image
            carvedImage = removeSeam(carvedImage, seam);
            currentWidth--;
        }

        return carvedImage;
    }
    
    public static void main(String[] args){
        try{
            // Load the input image
            File inputFile = new File("dancers.jpg");
            BufferedImage inputImage = ImageIO.read(inputFile);

            // Define the target width for the image
            int targetWidth = 300; // Adjust to your desired width

            // Apply seam carving
            BufferedImage outputImage = seamCarving(inputImage, targetWidth);

            // Save the result to a new file
            File outputFile = new File("dancers1_carved.jpg");
            ImageIO.write(outputImage, "jpg", outputFile);

            System.out.println("Seam carving completed and saved to: " + outputFile.getAbsolutePath());
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}