import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SeamCarvingDP{

    // Calculate pixel energy (same as before)
    public static int[][] calculatePixelEnergy(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] energy = new int[height][width];

        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                int a = getPixelBrightness(image, col - 1, row - 1);
                int b = getPixelBrightness(image, col, row - 1);
                int c = getPixelBrightness(image, col + 1, row - 1);
                int d = getPixelBrightness(image, col - 1, row);
                int f = getPixelBrightness(image, col + 1, row);
                int g = getPixelBrightness(image, col - 1, row + 1);
                int h = getPixelBrightness(image, col, row + 1);
                int i = getPixelBrightness(image, col + 1, row + 1);

                int xEnergy = a + 2 * d + g - c - 2 * f - i;
                int yEnergy = a + 2 * b + c - g - 2 * h - i;

                energy[row][col] = (int) Math.sqrt(xEnergy * xEnergy + yEnergy * yEnergy);
            }
        }

        return energy;
    }

    // Get pixel brightness (same as before)
    private static int getPixelBrightness(BufferedImage image, int col, int row){
        int width = image.getWidth();
        int height = image.getHeight();

        if(col < 0 || col >= width || row < 0 || row >= height){
            return 0;
        }

        Color color = new Color(image.getRGB(col, row));
        return color.getRed() + color.getGreen() + color.getBlue();
    }

    // Find the lowest energy seam using dynamic programming
    public static int[] findSeamDP(int[][] energy){
        int height = energy.length;
        int width = energy[0].length;

        // DP table to store minimum energy path to each pixel
        int[][] dp = new int[height][width];
        // Parent array to help reconstruct the seam
        int[][] parent = new int[height][width];

        // Copy first row as it is
        for(int col = 0; col < width; col++){
            dp[0][col] = energy[0][col];
        }

        // Fill DP table from top to bottom
        for(int row = 1; row < height; row++){
            for(int col = 0; col < width; col++){
                int minEnergy = dp[row - 1][col];
                int minCol = col;

                if(col > 0 && dp[row - 1][col - 1] < minEnergy){
                    minEnergy = dp[row - 1][col - 1];
                    minCol = col - 1;
                }
                if(col < width - 1 && dp[row - 1][col + 1] < minEnergy){
                    minEnergy = dp[row - 1][col + 1];
                    minCol = col + 1;
                }

                dp[row][col] = energy[row][col] + minEnergy;
                parent[row][col] = minCol; // store the path
            }
        }

        // Find the column in the last row with minimum total energy
        int minEndCol = 0;
        int minTotalEnergy = dp[height - 1][0];
        for(int col = 1; col < width; col++){
            if(dp[height - 1][col] < minTotalEnergy){
                minTotalEnergy = dp[height - 1][col];
                minEndCol = col;
            }
        }

        // Reconstruct seam path from bottom to top
        int[] seam = new int[height];
        int currentCol = minEndCol;
        for(int row = height - 1; row >= 0; row--){
            seam[row] = currentCol;
            currentCol = parent[row][currentCol];
        }

        return seam;
    }

    // Remove the seam from the image
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

    // Perform seam carving using DP until target width is reached
    public static BufferedImage seamCarvingDP(BufferedImage image, int targetWidth){
        BufferedImage carved = image;
        int currentWidth = image.getWidth();

        while(currentWidth > targetWidth){
            int[][] energy = calculatePixelEnergy(carved);
            int[] seam = findSeamDP(energy);
            carved = removeSeam(carved, seam);
            currentWidth--;
        }

        return carved;
    }

    public static void main(String[] args){
        try{
            // Load input image
            File inputFile = new File("cartoon.jpg");
            BufferedImage inputImage = ImageIO.read(inputFile);

            int targetWidth = 300;

            // Apply dynamic programming seam carving
            BufferedImage outputImage = seamCarvingDP(inputImage, targetWidth);

            // Save the result
            File outputFile = new File("cartoon_dp.jpg");
            ImageIO.write(outputImage, "jpg", outputFile);

            System.out.println("DP seam carving completed. Saved to: " + outputFile.getAbsolutePath());
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
