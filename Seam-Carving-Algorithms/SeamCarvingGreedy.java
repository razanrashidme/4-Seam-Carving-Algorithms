import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SeamCarvingGreedy{

    // Calculate the energy of each pixel in the image
    public static int[][] calculatePixelEnergy(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] energy = new int[height][width];

        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                // Get brightness values for surrounding pixels
                int a = getPixelBrightness(image, col - 1, row - 1);
                int b = getPixelBrightness(image, col, row - 1);
                int c = getPixelBrightness(image, col + 1, row - 1);
                int d = getPixelBrightness(image, col - 1, row);
                int f = getPixelBrightness(image, col + 1, row);
                int g = getPixelBrightness(image, col - 1, row + 1);
                int h = getPixelBrightness(image, col, row + 1);
                int i = getPixelBrightness(image, col + 1, row + 1);

                // Calculate energy in X and Y directions
                int xEnergy = a + 2 * d + g - c - 2 * f - i;
                int yEnergy = a + 2 * b + c - g - 2 * h - i;

                // Final energy of pixel (magnitude of gradient)
                energy[row][col] = (int) Math.sqrt(xEnergy * xEnergy + yEnergy * yEnergy);
            }
        }

        return energy;
    }

    // Get the brightness of a specific pixel (used for energy calculation)
    private static int getPixelBrightness(BufferedImage image, int col, int row){
        int width = image.getWidth();
        int height = image.getHeight();

        // If the pixel is out of bounds, return 0 (black)
        if(col < 0 || col >= width || row < 0 || row >= height){
            return 0;
        }

        // Get RGB color and sum up brightness
        Color color = new Color(image.getRGB(col, row));
        return color.getRed() + color.getGreen() + color.getBlue();
    }

    // Find the lowest-energy vertical seam using greedy approach
    public static int[] findGreedySeam(int[][] energy){
        int height = energy.length;
        int width = energy[0].length;
        int[] seam = new int[height];

        // Start from the top row: pick pixel with minimum energy
        int minCol = 0;
        int minVal = Integer.MAX_VALUE;
        for(int col = 0; col < width; col++){
            if(energy[0][col] < minVal){
                minVal = energy[0][col];
                minCol = col;
            }
        }

        seam[0] = minCol;

        // For each row below, pick the lowest-energy neighbor (left, middle, right)
        for(int row = 1; row < height; row++){
            int prevCol = seam[row - 1];
            int bestCol = prevCol;
            int bestEnergy = energy[row][prevCol];

            // Check left
            if(prevCol > 0 && energy[row][prevCol - 1] < bestEnergy){
                bestCol = prevCol - 1;
                bestEnergy = energy[row][prevCol - 1];
            }
            // Check right
            if(prevCol < width - 1 && energy[row][prevCol + 1] < bestEnergy){
                bestCol = prevCol + 1;
            }

            seam[row] = bestCol;
        }

        return seam;
    }

    // Remove the selected seam from the image
    public static BufferedImage removeSeam(BufferedImage image, int[] seam){
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width - 1, height, image.getType());

        for(int row = 0; row < height; row++){
            int colToRemove = seam[row];
            for(int col = 0; col < colToRemove; col++){
                result.setRGB(col, row, image.getRGB(col, row));
            }
            for(int col = colToRemove + 1; col < width; col++){
                result.setRGB(col - 1, row, image.getRGB(col, row));
            }
        }

        return result;
    }

    // Run the greedy seam carving process until reaching the target width
    public static BufferedImage seamCarvingGreedy(BufferedImage image, int targetWidth){
        BufferedImage carved = image;
        int currentWidth = image.getWidth();

        while(currentWidth > targetWidth){
            int[][] energy = calculatePixelEnergy(carved);
            int[] seam = findGreedySeam(energy);
            carved = removeSeam(carved, seam);
            currentWidth--;
        }

        return carved;
    }

    public static void main(String[] args){
        try{
            // Load the input image
            File inputFile = new File("cartoon.jpg");
            BufferedImage inputImage = ImageIO.read(inputFile);

            // Define target width
            int targetWidth = 300;

            // Apply greedy seam carving
            BufferedImage outputImage = seamCarvingGreedy(inputImage, targetWidth);

            // Save the result
            File outputFile = new File("cartoon_greedy.jpg");
            ImageIO.write(outputImage, "jpg", outputFile);

            System.out.println("Greedy seam carving completed. Saved to: " + outputFile.getAbsolutePath());
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
