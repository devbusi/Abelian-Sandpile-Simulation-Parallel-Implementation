package serialAbelianSandpile;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Grid2 extends RecursiveAction {

    private int rows, columns;
    private int[][] grid;
    private int[][] updateGrid;
    private int startRow, endRow, startCol, endCol;
    public static final int threshold = 50;  // Define threshold to decide when to split the task

    private static final ForkJoinPool fjpool = ForkJoinPool.commonPool();

    // Main constructor
    public Grid2(int w, int h) {
        this.rows = w + 2;  // for the "sink" border
        this.columns = h + 2;  // for the "sink" border
        grid = new int[this.rows][this.columns];
        updateGrid = new int[this.rows][this.columns];

        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                grid[i][j] = 0;
                updateGrid[i][j] = 0;
            }
        }
    }

    // Constructor for sub-tasks
    private Grid2(int[][] grid, int[][] updateGrid, int startRow, int endRow, int startCol, int endCol) {
        this.grid = grid;
        this.updateGrid = updateGrid;
        this.startRow = startRow;
        this.endRow = endRow;
        this.startCol = startCol;
        this.endCol = endCol;
    }

    // Getter methods
    public int getRows() {
        return rows - 2; // less the sink
    }

    public int getColumns() {
        return columns - 2; // less the sink
    }

    int get(int i, int j) {
        return this.grid[i][j];
    }

    void setAll(int value) {
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < columns - 1; j++) {
                grid[i][j] = value;
            }
        }
    }

    // For the next timestep - copy updateGrid into grid
    public void nextTimeStep() {
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < columns - 1; j++) {
                this.grid[i][j] = updateGrid[i][j];
            }
        }
    }

    // Update method to calculate the next update grid
    boolean update() {
        boolean change = false;
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < columns - 1; j++) {
                updateGrid[i][j] = (grid[i][j] % 4) +
                        (grid[i - 1][j] / 4) +
                        (grid[i + 1][j] / 4) +
                        (grid[i][j - 1] / 4) +
                        (grid[i][j + 1] / 4);
                if (grid[i][j] != updateGrid[i][j]) {
                    change = true;
                }
            }
        }
        if (change) {
            nextTimeStep();
        }
        return change;
    }

    // Display the grid in text format
    void printGrid() {
        System.out.printf("Grid:\n+");
        for (int j = 1; j < columns - 1; j++) System.out.printf("  --");
        System.out.printf("+\n");
        for (int i = 1; i < rows - 1; i++) {
            System.out.printf("|");
            for (int j = 1; j < columns - 1; j++) {
                if (grid[i][j] > 0)
                    System.out.printf("%4d", grid[i][j]);
                else
                    System.out.printf("    ");
            }
            System.out.printf("|\n");
        }
        System.out.printf("+");
        for (int j = 1; j < columns - 1; j++) System.out.printf("  --");
        System.out.printf("+\n\n");
    }

    // Write grid out as an image
    void gridToImage(String fileName) throws IOException {
        BufferedImage dstImage = new BufferedImage(rows, columns, BufferedImage.TYPE_INT_ARGB);
        int a = 0, g = 0, b = 0, r = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                g = 0;
                b = 0;
                r = 0;
                switch (grid[i][j]) {
                    case 0:
                        break;
                    case 1:
                        g = 255;
                        break;
                    case 2:
                        b = 255;
                        break;
                    case 3:
                        r = 255;
                        break;
                    default:
                        break;
                }
                int dpixel = (0xff000000) | (a << 24) | (r << 16) | (g << 8) | b;
                dstImage.setRGB(i, j, dpixel);
            }
        }
        File dstFile = new File(fileName);
        ImageIO.write(dstImage, "png", dstFile);
    }

    @Override
    protected void compute() {
        // If the task is small enough, compute directly
        if ((endRow - startRow) <= threshold && (endCol - startCol) <= threshold) {
            updateBlock(startRow, endRow, startCol, endCol);
        } else {
            // Split the task into 4 subtasks
            int midRow = (startRow + endRow) / 2;
            int midCol = (startCol + endCol) / 2;

			
            
                    Grid2 topLeft = new Grid2(grid, updateGrid, startRow, midRow, startCol, midCol);        // Top-Left
                    Grid2 topRight = new Grid2(grid, updateGrid, startRow, midRow, midCol + 1, endCol);     // Top-Right
                    Grid2 bottomLeft = new Grid2(grid, updateGrid, midRow + 1, endRow, startCol, midCol);     // Bottom-Left
                    Grid2 bottomRight = new Grid2(grid, updateGrid, midRow + 1, endRow, midCol + 1, endCol); 
					
					topLeft.fork();
					topRight.fork();
					bottomLeft.fork();
					bottomRight.compute();
					
					topLeft.join();
					topRight.join();
					bottomLeft.join();
            
        }
    }

    // Method to update a block of the grid
    private void updateBlock(int rowStart, int rowEnd, int colStart, int colEnd) {
        for (int i = rowStart; i <= rowEnd; i++) {
            for (int j = colStart; j <= colEnd; j++) {
                updateGrid[i][j] = (grid[i][j] % 4) +
                        (grid[i - 1][j] / 4) +
                        (grid[i + 1][j] / 4) +
                        (grid[i][j - 1] / 4) +
                        (grid[i][j + 1] / 4);
            }
        }
    }
}
