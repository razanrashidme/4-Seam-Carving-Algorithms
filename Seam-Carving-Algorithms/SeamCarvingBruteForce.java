
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class SeamCarvingBruteForce{

    // Calculate the energy for each pixel
    public static int[][] calculatePixelEnergy(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] energy = new int[height][width];

        for(int row = 0; row < height; row++){
            for (int col = 0; col < width; col++){
                // Get brightness for nearby pixels
                int a = getPixelBrightness(image, col - 1, row - 1);
                int b = getPixelBrightness(image, col, row - 1);
                int c = getPixelBrightness(image, col + 1, row - 1);
                int d = getPixelBrightness(image, col - 1, row);
                int e = getPixelBrightness(image, col, row);
                int f = getPixelBrightness(image, col + 1, row);
                int g = getPixelBrightness(image, col - 1, row + 1);
                int h = getPixelBrightness(image, col, row + 1);
                int i = getPixelBrightness(image, col + 1, row + 1);

                // Calculate horizontal and vertical energy
                int xEnergy = a + 2 * d + g - c - 2 * f - i;
                int yEnergy = a + 2 * b + c - g - 2 * h - i;

                // Calculate total energy
                energy[row][col] = (int) Math.sqrt(xEnergy * xEnergy + yEnergy * yEnergy);
            }
        }

        return energy;
    }

    // Get the brightness of a pixel
    private static int getPixelBrightness(BufferedImage image, int col, int row){
        int width = image.getWidth();
        int height = image.getHeight();

        // If the pixel is out of bounds, consider it as black (brightness 0)
        if(col < 0 || col >= width || row < 0 || row >= height){
            return 0;
        }

        // Get the color and return brightness (sum of the red, green, and blue values)
        Color color = new Color(image.getRGB(col, row));
        return color.getRed() + color.getGreen() + color.getBlue();
    }

    public static List<Integer> findBruteForceSeam(int[][] energy, int row, int col, List<Integer> path, int totalEnergy){
        int height = energy.length;
        int width = energy[0].length;

        // Base case: reached the last row, return the path
        if(row == height - 1){
            List<Integer> result = new ArrayList<>(path);
            result.add(col);
            return result;
        }

        List<Integer> bestSeam = null;
        int minEnergy = Integer.MAX_VALUE;

        // Try moving Left, Middle, and Right
        for(int offset = -1; offset <= 1; offset++){
            int newCol = col + offset;
            if(newCol >= 0 && newCol < width){ // Check bounds
                List<Integer> newPath = new ArrayList<>(path);
                newPath.add(col);
                List<Integer> candidateSeam = findBruteForceSeam(energy, row + 1, newCol, newPath, totalEnergy + energy[row][col]);

                // Compute energy for this seam
                int candidateEnergy = totalEnergy + energy[row][col];

                if(candidateEnergy < minEnergy){
                    minEnergy = candidateEnergy;
                    bestSeam = candidateSeam;
                }
            }
        }

        return bestSeam;
    }

    public static int[] findLowestEnergySeam(int[][] energy){
        int width = energy[0].length;
        int height = energy.length;
        int minTotalEnergy = Integer.MAX_VALUE;
        List<Integer> bestSeam = null;

        for(int col = 0; col < width; col++){
            System.out.println("Trying seam starting from column: " + col);  // Debugging

            List<Integer> seam = findBruteForceSeam(energy, 0, col, new ArrayList<>(), 0);

            if(seam == null || seam.isEmpty()){
                continue; // Skip invalid seams
            }

            // Fixed energy calculation
            int totalEnergy = 0;
            for(int row = 0; row < seam.size(); row++){
                int colIndex = seam.get(row);
                totalEnergy += energy[row][colIndex];
            }

            if(totalEnergy < minTotalEnergy){
                minTotalEnergy = totalEnergy;
                bestSeam = seam;
            }
        }

        if(bestSeam == null){
            throw new RuntimeException("No valid seam found!");
        }

        return bestSeam.stream().mapToInt(i -> i).toArray();
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

    public static BufferedImage seamCarving(BufferedImage image, int targetWidth) {
        int currentWidth = image.getWidth();
        BufferedImage carvedImage = image;

        System.out.println("Starting seam carving...");
        System.out.println("Input image size: " + currentWidth + "x" + image.getHeight());
        System.out.println("Target width: " + targetWidth);

        while(currentWidth > targetWidth){
            // Compute the energy map
            int[][] energyMap = calculatePixelEnergy(carvedImage);

            // Find the lowest energy seam
            int[] seam = findLowestEnergySeam(energyMap);

            // Remove the seam from the image
            carvedImage = removeSeam(carvedImage, seam);
            currentWidth--;

            System.out.println("Removed a seam, new width: " + currentWidth);
        }

        System.out.println("Seam carving completed.");
        return carvedImage;
    }

    public static void main(String[] args){
        try{
            // Load the input image
        	File inputFile = new File("cartoon.jpg");
            BufferedImage inputImage = ImageIO.read(inputFile);

            // Define the target width
            int targetWidth = 300;

            // Apply seam carving
            BufferedImage outputImage = seamCarving(inputImage, targetWidth);

            // Save the result
            File outputFile = new File("cartoon_carved.jpg");
            ImageIO.write(outputImage, "jpg", outputFile);

            System.out.println("Seam carving completed and saved to: " + outputFile.getAbsolutePath());
        } catch(IOException e){
            System.out.println("Error loading or saving the image.");
            e.printStackTrace();
        }
    }
}
