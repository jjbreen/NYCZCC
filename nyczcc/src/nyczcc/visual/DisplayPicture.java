package nyczcc.visual;

import java.awt.Color;
import java.awt.Shape;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Renderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

import nyczcc.Trajectory;
import nyczcc.optimization.SetCovering.SetPartition;


public class DisplayPicture {

	
	public class XYSeriesDemo extends ApplicationFrame {

		private Color[] palette = new Color[]{Color.BLUE, Color.ORANGE, Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN, Color.PINK};
		
		public XYSeriesDemo(final String title, List<Trajectory> t) {

		    super(title);
		    
		    final XYSeriesCollection data = new XYSeriesCollection();
		    
		    Map<Integer, Integer> clusterMap = new HashMap<>();
		    int snum = 0;
		    
		    for (Trajectory tra : t)
		    {
		    	if (tra.getDropOffLatitude() == 0 ||tra.getDropOffLongitude() == 0 ||
		    		tra.getPickUpLatitude() == 0 || tra.getPickUpLongitude() == 0)
				{
					continue;
				}
		    	final XYSeries series = new XYSeries(tra.getRowID());
		    	series.add(tra.getPickUpLatitude(), tra.getPickUpLongitude());
		    	series.add(tra.getDropOffLatitude(), tra.getDropOffLongitude());
		  
		    	clusterMap.put(snum, tra.getCluster());
		    	snum++;
		    	data.addSeries(series);
		    }
		    
		    final JFreeChart chart = ChartFactory.createXYLineChart(
		        title,
		        "X", 
		        "Y", 
		        data,
		        PlotOrientation.VERTICAL,
		        false,
		        false,
		        false
		    );

		    
		    XYPlot plot = (XYPlot) chart.getPlot();
		    for (Integer n : clusterMap.keySet())
		    {
		    	plot.getRenderer().setSeriesPaint(n, clusterMap.get(n) == -1 ? Color.BLACK : palette[clusterMap.get(n) % palette.length]);
		    }
		    
		    // Sets the Trajectories to not include Zero
	        NumberAxis axis = (NumberAxis) chart.getXYPlot().getRangeAxis();
	        axis.setAutoRangeIncludesZero(false);
		    
		    final ChartPanel chartPanel = new ChartPanel(chart);
		    chartPanel.setPreferredSize(new java.awt.Dimension(1000, 1000));
		    setContentPane(chartPanel);

		}
		
		public XYSeriesDemo(final String title, List<Trajectory> t, List<SetPartition> p) {

		    super(title);
		    
		    final XYSeriesCollection data = new XYSeriesCollection();
		    
		    Map<Integer, Integer> clusterMap = new HashMap<>();
		    int snum = 0;
		    int rid = 0;
		    
		    for (Trajectory tra : t)
		    {
		    	if (tra.getDropOffLatitude() == 0 ||tra.getDropOffLongitude() == 0 ||
		    		tra.getPickUpLatitude() == 0 || tra.getPickUpLongitude() == 0)
				{
					continue;
				}
		    	final XYSeries series = new XYSeries(tra.getRowID());
		    	series.add(tra.getPickUpLatitude(), tra.getPickUpLongitude());
		    	series.add(tra.getDropOffLatitude(), tra.getDropOffLongitude());
		  
		    	clusterMap.put(snum, tra.getCluster());
		    	snum++;
		    	data.addSeries(series);
		    	
		    	rid = tra.getRowID();
		    }
		    
		    for (SetPartition part : p){
		    	rid++;
		    	final XYSeries series = new XYSeries(rid);
		    	series.add((part.minLat + part.maxLat)/2, (part.minLon + part.maxLon) / 2);
		    	
		    	data.addSeries(series);
		    }
		    
		    final JFreeChart chart = ChartFactory.createXYLineChart(
		        title,
		        "X", 
		        "Y", 
		        data,
		        PlotOrientation.VERTICAL,
		        false,
		        false,
		        false
		    );

		    
		    
		    
		    XYPlot plot = (XYPlot) chart.getPlot();
		    for (Integer n : clusterMap.keySet())
		    {
		    	plot.getRenderer().setSeriesPaint(n, clusterMap.get(n) == -1 ? Color.BLACK : palette[clusterMap.get(n) % palette.length]);
		    }
		    
		    Shape cross = ShapeUtilities.createDiagonalCross(3, 1);
		    plot = (XYPlot) chart.getPlot();
		    XYItemRenderer renderer = plot.getRenderer();
		    for (int x = 0; x < p.size(); x++){
		    	renderer.setSeriesShape(rid - x - 1, cross);
		    	
		    }
		    
		    XYLineAndShapeRenderer r2 =
		    	    (XYLineAndShapeRenderer) plot.getRenderer();
		    r2.setBaseShapesVisible(true);
		    
		    // Sets the Trajectories to not include Zero
	        NumberAxis axis = (NumberAxis) chart.getXYPlot().getRangeAxis();
	        axis.setAutoRangeIncludesZero(false);
		    
		    final ChartPanel chartPanel = new ChartPanel(chart);
		    chartPanel.setPreferredSize(new java.awt.Dimension(1000, 1000));
		    setContentPane(chartPanel);

		}
	}
	
	public void displayPicture(String title, List<Trajectory> t){
		final XYSeriesDemo demo = new XYSeriesDemo(title, t);
	    demo.pack();
	    RefineryUtilities.centerFrameOnScreen(demo);
	    demo.setVisible(true);
	}
	
	public void displayOptimization(String title, List<Trajectory> t, List<SetPartition> p){
		final XYSeriesDemo demo = new XYSeriesDemo(title, t, p);
	    demo.pack();
	    RefineryUtilities.centerFrameOnScreen(demo);
	    demo.setVisible(true);
	}

}