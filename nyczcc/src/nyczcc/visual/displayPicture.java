package nyczcc.visual;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import nyczcc.Trajectory;


public class displayPicture {

	
	public class XYSeriesDemo extends ApplicationFrame {

		private Color[] palette = new Color[]{Color.BLUE, Color.ORANGE, Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.PINK};
		
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
		    	plot.getRenderer().setSeriesPaint(n, clusterMap.get(n) == -1 ? Color.BLACK : palette[clusterMap.get(n)]);
		    }
		    
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

}
