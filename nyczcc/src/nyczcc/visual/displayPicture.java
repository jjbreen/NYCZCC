package nyczcc.visual;

import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import nyczcc.Trajectory;


public class displayPicture {

	
	public class XYSeriesDemo extends ApplicationFrame {

		/**
		 * A demonstration application showing an XY series containing a null value.
		 *
		 * @param title  the frame title.
		 */
		public XYSeriesDemo(final String title, List<Trajectory> t) {

		    super(title);
		    
		    final XYSeriesCollection data = new XYSeriesCollection();
		    
		    for (Trajectory tra : t)
		    {
		    	if (tra.getDropOffLatitude() == 0 ||tra.getDropOffLongitude() == 0 ||
		    		tra.getPickUpLatitude() == 0 || tra.getPickUpLongitude() == 0)
				{
					continue;
				}
		    	System.out.println("Got Here!");
		    	final XYSeries series = new XYSeries(tra.getRowID());
		    	series.add(tra.getPickUpLatitude(), tra.getPickUpLongitude());
		    	series.add(tra.getDropOffLatitude(), tra.getDropOffLongitude());
		  
		    	data.addSeries(series);
		    }
		    
		    final JFreeChart chart = ChartFactory.createXYLineChart(
		        "XY Series Demo",
		        "X", 
		        "Y", 
		        data,
		        PlotOrientation.VERTICAL,
		        false,
		        false,
		        false
		    );

	        NumberAxis axis = (NumberAxis) chart.getXYPlot().getRangeAxis();
	        axis.setAutoRangeIncludesZero(false);

		    
		    final ChartPanel chartPanel = new ChartPanel(chart);
		    chartPanel.setPreferredSize(new java.awt.Dimension(1000, 1000));
		    setContentPane(chartPanel);

		}

		// ****************************************************************************
		// * JFREECHART DEVELOPER GUIDE                                               *
		// * The JFreeChart Developer Guide, written by David Gilbert, is available   *
		// * to purchase from Object Refinery Limited:                                *
		// *                                                                          *
		// * http://www.object-refinery.com/jfreechart/guide.html                     *
		// *                                                                          *
		// * Sales are used to provide funding for the JFreeChart project - please    * 
		// * support us so that we can continue developing free software.             *
		// ****************************************************************************

		

	}
	
	public void displayPicture(List<Trajectory> t){
		final XYSeriesDemo demo = new XYSeriesDemo("XY Series Demo", t);
	    demo.pack();
	    RefineryUtilities.centerFrameOnScreen(demo);
	    demo.setVisible(true);
	    System.out.println("Got Here 2");
	}

}
