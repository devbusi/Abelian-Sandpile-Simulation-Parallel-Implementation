
package serialAbelianSandpile;

import java.util.concurrent.RecursiveTask;

public class UpdateTask extends RecursiveTask<Boolean>
{
	private int [][] grid; 
	private int [][] updateGrid;
	private int rowStart,rowEnd,columnStart,columnEnd;
	public static final int THRESHOLD = 110;

	public UpdateTask (int [][] grid,int [][] updateGrid,int rowStart,int rowEnd, int columnStart, int columnEnd)
	{
		this.grid = grid;
		this.updateGrid = updateGrid;
		this.rowStart = rowStart;
		this.rowEnd = rowEnd;
		this.columnStart = columnStart;
		this.columnEnd = columnEnd;
	}

	protected Boolean compute()
	{
		if ((rowEnd - rowStart <= THRESHOLD) && (columnEnd-columnStart <= THRESHOLD))
		{
			boolean change=false;
		//do not update border
			for( int i = rowStart; i<rowEnd; i++ ) {
				for( int j = columnStart; j<columnEnd; j++ ) {
					updateGrid[i][j] = (grid[i][j] % 4) + 
						(grid[i-1][j] / 4) +
						grid[i+1][j] / 4 +
						grid[i][j-1] / 4 + 
						grid[i][j+1] / 4;
				if (grid[i][j]!=updateGrid[i][j]) {  
					change=true;
				}
		}} //end nested for

		return change;
		}
		else
		{
			int rowMiddle = (rowStart + rowEnd)/2;
			int columnMiddle = (columnStart + columnEnd)/2;

			UpdateTask topLeft =  new UpdateTask(grid, updateGrid,rowStart,rowMiddle,columnStart,columnMiddle);

			UpdateTask topRight =  new UpdateTask(grid, updateGrid,rowMiddle,rowEnd,columnStart,columnMiddle);

			UpdateTask bottomLeft =  new UpdateTask(grid, updateGrid,rowStart,rowMiddle,columnMiddle,columnEnd);

			UpdateTask bottomRight =  new UpdateTask(grid, updateGrid,rowMiddle,rowEnd,columnMiddle,columnEnd);


			topLeft.fork();

			topRight.fork();

			bottomLeft.fork();

			boolean a = bottomRight.compute();

			boolean b = topLeft.join();

			boolean c = topRight.join();

			boolean d = bottomLeft.join();
			
			return a || b || c || d;

		}
	}
}