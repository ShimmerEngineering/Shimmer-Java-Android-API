package com.shimmerresearch.guiUtilities.plot;
//==========================================================\\
// BodeNyquist class, extends JApplet.
// Plots complex frequency response based on
// transfer function coefficients in entry fields.
// Will multiply lists of polynomials and collect terms.
// May be run in a browser, appletviewer, or standalone.
// Implements Java Swing user interface.
// Designed to be built with Java 1.3.1 or newer.
// Originated 7/1/11 by M. Williamsen
// Released version 2.0, 8/20/11
// http://www.williamsonic.com
//==========================================================\\

import javax.swing.*;

import com.shimmerresearch.algorithms.Filter;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

// main class for this applet
//==========================================================\\
public class BodeNyquist extends JApplet
{
    // information strings
    static final String versionStr   =
    "Bode/Nyquist Plot Applet v. 2.0;  by M. Williamsen 8/20/11";
    static final String infoStr[][]  =
    {
        {"numerator",   "String",
            "Polynomial coefficients or list of polynomials."},
        {"denominator", "String",
            "Polynomial coefficients or list of polynomials."},
        {"startFreq",   "Real number", "Frequency at left edge of plot."},
        {"decades",     "Integer",     "Number of decades to plot."},
        {"units",       "String",      "Hz or radians."},
    };
    
    // GUI components
    PlotCanvas  theCanvas;
    PolarCanvas thePolar;
    PlotPanel   thePanel;
    PlotData    theData;
    JTabbedPane thePane;
    
    // instance variables
    String numStr;      // numerator coefficients
    String denStr;      // denominator coefficients
    String startStr;    // plot start frequency
    String decadesStr;  // number of decades to plot
    String unitsStr;    // units of Hz or radians/second
    
    // Set look & feel to match local OS, before drawing anything.
    void setLAF()
    {
        String theLAF = UIManager.getSystemLookAndFeelClassName();
        // System.out.println("Local look & feel: " + theLAF);
        try{UIManager.setLookAndFeel(theLAF);}
        catch(UnsupportedLookAndFeelException e)
            {System.out.println("UnsupportedLookAndFeelException.");}
        catch(Exception e)
            {System.out.println("Failed to set Look And Feel.");}
    }
    
    // default constructor, needed for browsers and appletviewer
    public BodeNyquist()
    {
        // Set the local look & feel before drawing anything.
        setLAF();
    }

    // construct BodeNyquist instance for standalone execution
    public BodeNyquist(String args[])
    {
        // preset to default values (2nd order notch filter)
        numStr = "1,0,1";
        denStr = "1,1,1";
        startStr = "0.1";
        decadesStr = "2";
        unitsStr = "radians";

        // accept command line arguments if present
        switch (args.length)
        {
        case 5:
            unitsStr = args[4];
        case 4:
            decadesStr = args[3];
        case 3:
            startStr = args[2];
        case 2:
            denStr = args[1];
        case 1:
            numStr = args[0];
            break;
        default:
            System.out.println("usage: java -cp BodeNyquist.jar BodeNyquist "
                + "numCoeff denCoeff [startFreq [2|3|4 [Hz|rad]]]");
            break;
        }

        // Set look & feel to match local OS.
        setLAF();        
    }

    // main entry point for standalone execution
    public static void main(String args[])
    {
        // construct an instance of this applet
        BodeNyquist thePlot = new BodeNyquist(args);
        thePlot.init();
        
        try {
			Filter filter = new Filter(Filter.BAND_STOP, 256, new double[] {45.0, 55.0});
			String coeff = "";
			for(int i=0;i<filter.coefficients.length;i++) {
				double d = filter.coefficients[i];
				coeff+=d;
				if(i<filter.coefficients.length-1) {
					coeff+=",";
				}
			}
			System.out.println(coeff);
			thePlot.thePanel.numField.setText(coeff);
			thePlot.thePanel.denField.setText("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


        // construct window frame to run applet
        JFrame theFrame = new JFrame("Bode/Nyquist Plot Java Application");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.getContentPane().add(thePlot);
        theFrame.setSize(755, 580);
        theFrame.setVisible(true);
        thePlot.start();
    }
        
    // initialization code
    public void init()
    {
        // obtain applet parameters from HTML
        if (null == numStr)     {numStr = getParameter("numerator");}
        if (null == denStr)     {denStr = getParameter("denominator");}
        if (null == startStr)   {startStr = getParameter("startFreq");}
        if (null == decadesStr) {decadesStr = getParameter("decades");}
        if (null == unitsStr)   {unitsStr = getParameter("units");}

        // Show version info on console
        System.out.println(versionStr);
        
        // add GUI component for plot area
        getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER));
        theCanvas = new PlotCanvas(this);
        thePolar  = new PolarCanvas(this);
        
        // add GUI component for control panel
        thePanel = new PlotPanel(this);
        
        // set up tabbed pane
        thePane = new JTabbedPane();
        thePane.setOpaque(false);
        thePane.addTab("Bode Plot", theCanvas);
        thePane.addTab("Nyquist Plot", thePolar);
        getContentPane().add(thePane);
        getContentPane().add(thePanel);
    }
    
    // start the applet
    public void start()
    {
        // draw plot for the first time
        thePanel.doPlot();
    }

    // implement some applet methods    
    public String getAppletInfo(){return versionStr;}
    public String[][] getParameterInfo(){return infoStr;}
}

//==========================================================\\
// component subclass to hold Bode plot area
class PlotCanvas extends JPanel
{
    // instance data members
    BodeNyquist theApp;

    // constructor with one arg, a reference to the parent
    PlotCanvas(BodeNyquist anApp)
    {
        // keep reference to parent
        theApp = anApp;
        
        // required to allow look and feel to show through
        setOpaque(false);
        setPreferredSize(new Dimension(725, 325));
    }

    // draw the plot area as needed
    public void paint(Graphics g)
    {
        if (null != theApp.theData)
            {theApp.theData.paintPlot(g);}
    }    
}

//==========================================================\\
// component subclass to hold Nyquist (polar) plot
class PolarCanvas extends JPanel
{
    // instance data members
    BodeNyquist theApp;
    
    // constructor with one arg, a reference to the parent
    PolarCanvas(BodeNyquist anApp)
    {
        // keep reference to parent
        theApp = anApp;
        
        // required to allow look and feel to show through
        setOpaque(false);
        setPreferredSize(new Dimension(725, 325));
    }
    
    // draw the plot area as needed
    public void paint(Graphics g)
    {
        if (null != theApp.theData)
            {theApp.theData.paintPolar(g);}
    }    
}

//==========================================================\\
// component to hold GUI controls
class PlotPanel extends JPanel
implements ActionListener
{
    // reference to the applet
    BodeNyquist theApp;

    // Swing instance variables, to support platform-based look & feel.
    JButton theButton;
    JCheckBox theCheck;
    JLabel numLabel;
    JTextField numField;
    JLabel denLabel;
    JTextField denField;
    JLabel startLabel;
    JTextField startField;
    JTextArea resultsArea;
    JComboBox decadesCombo;
    JComboBox unitsCombo;

    // constructor with one argument, a reference to the parent
    PlotPanel(BodeNyquist anApp)
    {
        theApp = anApp;
        setPreferredSize(new Dimension(725, 160));
        setLayout(new GridLayout(1,2,8,8));

        // required to allow look and feel to show through
        setOpaque(false);
        
        // button control to plot new data
        theButton = new JButton("Plot Response");
        theButton.setOpaque(false);
        theButton.addActionListener(this); // register for events
        
        // check box control to inhibit phase plot
        theCheck = new JCheckBox("Hide Phase", false);
        theCheck.setOpaque(false);
        theCheck.addActionListener(this); // register for events
        
        // choice control for decades
        String decadesList[] = {"2 Decades", "3 Decades", "4 Decades"};
        decadesCombo = new JComboBox(decadesList);
        decadesCombo.setOpaque(false);
        int numDecades = 2;
        try{numDecades = Integer.parseInt(theApp.decadesStr);}
        catch(NumberFormatException e)
        {
            System.out.println("Couldn't parse number of decades: "
             + theApp.decadesStr);
            numDecades = 2;
        }
        
        // limit range for number of decades to (2...4) 
        numDecades = Math.min(numDecades, 4);
        numDecades = Math.max(numDecades, 2);
        decadesCombo.setSelectedIndex(numDecades-2);
        decadesCombo.addActionListener(this); // register for events

        // set up button controls
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(decadesCombo);
        buttonPanel.add(theButton);
        buttonPanel.add(theCheck);
        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        controlPanel.setLayout(new GridLayout(4,1));
        controlPanel.setBorder(BorderFactory.createEtchedBorder());
        add(controlPanel);
        controlPanel.add(buttonPanel);

        // text entry field for numerator coefficients
        numField = new JTextField(theApp.numStr, 20);
        // numField.setDragEnabled(true);
        numLabel = new JLabel("Numerator", Label.LEFT);
        numLabel.setOpaque(false);
        JPanel numPanel = new JPanel();
        numPanel.setOpaque(false);
        numPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        numPanel.add(numField);
        numPanel.add(numLabel);
        controlPanel.add(numPanel);

        // text entry field for denominator coefficients
        denField = new JTextField(theApp.denStr, 20);
        // denField.setDragEnabled(true);
        denLabel = new JLabel("Denominator", Label.LEFT);
        denLabel.setOpaque(false);
        JPanel denPanel = new JPanel();
        denPanel.setOpaque(false);
        denPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        denPanel.add(denField);
        denPanel.add(denLabel);
        controlPanel.add(denPanel);
        
        // text entry field for start frequency of plot
        startField = new JTextField(theApp.startStr, 12);
        // startField.setDragEnabled(true);
        startLabel = new JLabel("Start Freq.", Label.LEFT);
        startLabel.setOpaque(false);

        // choice control for units
        String unitsList[] = {"rad/sec", "Hz"};
        unitsCombo = new JComboBox(unitsList);
        unitsCombo.setOpaque(false);
        if ((null != theApp.unitsStr)
            && (theApp.unitsStr.equalsIgnoreCase("Hz")))
            {unitsCombo.setSelectedItem("Hz");}
        else
            {unitsCombo.setSelectedItem("rad/sec"); }
        unitsCombo.addActionListener(this); // register for events

        // panel to hold start frequency and decades combo
        JPanel startPanel = new JPanel();
        startPanel.setOpaque(false);
        startPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        startPanel.add(unitsCombo);
        startPanel.add(startField);
        startPanel.add(startLabel);
        controlPanel.add(startPanel);
        
        // static text field for transfer function results
        resultsArea = new JTextArea("Transfer function results appear here.");
        resultsArea.setEditable(false);
        // resultsArea.setDragEnabled(true);
        resultsArea.setBorder(BorderFactory.createEtchedBorder());
        add(new JScrollPane(resultsArea));
    }
    
    // handle button click events
    public void actionPerformed(ActionEvent e)
    {
        // checkbox doesn't need to recalculate
        if (e.getSource() == theCheck)
        {
            theApp.theCanvas.repaint();
            theApp.thePolar.repaint();
        }
        
        // other controls do need recalculate
        else {doPlot(); }
    }
    
    void doPlot()
    {
        // build polynomial objects for numerator and denominator
        double[] numCoeff;
        double[] denCoeff;
        double startFreq;
        
        // get user input from numerator text field
        theApp.numStr = numField.getText();
        try{numCoeff = Polynomial.convertPolyList(theApp.numStr);}
        catch(NumberFormatException e)
        {
            resultsArea.setText("Couldn't parse numerator: " + theApp.numStr);
            System.out.println("Couldn't parse numerator: " + theApp.numStr);
            return;
        }
        
        // get user input from denominator text field
        theApp.denStr = denField.getText();
        try{denCoeff = Polynomial.convertPolyList(theApp.denStr);}
        catch(NumberFormatException e)
        {
            resultsArea.setText("Couldn't parse denominator: "+theApp.denStr);
            System.out.println("Couldn't parse denominator: "+theApp.denStr);
            return;
        }
        
        // get user input from start freq text field
        theApp.startStr = startField.getText();
        try{startFreq = Double.parseDouble(theApp.startStr);}
        catch(NumberFormatException e)
        {
            resultsArea.setText("Couldn't parse start freq: "+theApp.startStr);
            System.out.println("Couldn't parse start freq: "+theApp.startStr);
            return;
        }
        
        // check units of frequency
        boolean unitsHz = false;
        Object theItem = theApp.thePanel.unitsCombo.getSelectedItem();
        if ("Hz" == theItem) {unitsHz = true;}
        
        // if no problems, show results on console
        System.out.print("  Numerator: ");
        Polynomial.showArray(numCoeff);
        System.out.print("Denominator: ");
        Polynomial.showArray(denCoeff);
        System.out.println("Start freq.: " + startFreq + ' ' + theItem);
        
        
        // calculate transfer function results
        theApp.theData =
            new PlotData(theApp, startFreq, unitsHz, numCoeff, denCoeff);
        StringBuffer theBuff = theApp.theData.getResult();
        if (null == theBuff)
        {
            System.out.println("Error: failed to plot data.");
            resultsArea.setText("Error: failed to plot data.");
            return;
        }
        resultsArea.setText(theBuff.toString());
                
        // ask plots to redraw themselves with new data
        theApp.theCanvas.repaint();
        theApp.thePolar.repaint();
    }    
}

//==========================================================\\
// container for complex frequency response
class PlotData
{
    Complex[] theResult;
    double[] theFreqs;
    double maxReal;
    double minReal;
    double maxImag;
    double minImag;
    double startFreq;
    boolean unitsHz;
    int theRange;
    int numDecades;
    BodeNyquist theApp;
    
    PlotData(BodeNyquist anApp, double aFreq, boolean wantHz,
             double[] numCoeff, double[] denCoeff)
    {
        // copy input parameters
        theApp = anApp;
        startFreq = aFreq;
        unitsHz = wantHz;

        // check number of decades from combo control
        Object theItem = theApp.thePanel.decadesCombo.getSelectedItem();
        theRange = 300; // number of pixels per decade
        if ("2 Decades" == theItem)
            {theRange = 300; numDecades = 2;}
        else if ("3 Decades" == theItem)
            {theRange = 200; numDecades = 3;}
        else if ("4 Decades" == theItem)
            {theRange = 150; numDecades = 4;}
        
        // check units for frequency
        double freqFactor = 1.;
        if (unitsHz) {freqFactor = 2. * Math.PI;}
        
        // initialize list of frequencies to plot
        int index = 0;
        theFreqs = new double[601];
        theFreqs[0] = startFreq * freqFactor;
        for (index = 1; index < 601; index++)
        {
            // find next frequency in rad/sec as a ratio
            theFreqs[index] = Math.pow(10.0, 1.0/theRange)*theFreqs[index-1]; 
        }
        
        // calculate complex response at each plot frequency
        plotAll(numCoeff, denCoeff);
    }
    
    StringBuffer getResult()
    {
        // copy results to text buffer
        int index = 0;
        StringBuffer theBuff;
        if (unitsHz) {theBuff = new StringBuffer("Frequency(Hz)");}
        else {theBuff = new StringBuffer("Frequency(r/s)");}
        theBuff.append("\tComplex Response\n");
        
        // always use scientific notation to 6 sig. figs.
        DecimalFormat theFormat = new DecimalFormat("#.######E0");
        for (index = 0; index < theResult.length; index++)
        {   
            // first column in frequency in chosen units
            double aFreq = theFreqs[index];
            if (unitsHz) {aFreq /= (2. * Math.PI);}
            theBuff.append(theFormat.format(aFreq));
            
            // second column is complex response
            theBuff.append('\t');
            theBuff.append(theResult[index].toString());
            theBuff.append('\n');
        }
        return theBuff;
    }
        
    // draw Bode plot in first tabbed pane
    void paintPlot(Graphics g)
    {
        boolean hidePhase = theApp.thePanel.theCheck.isSelected();
        
        // move origin to make room for text labels
        g.translate(50, 3);
        
        // draw white rectangle for plot area
        g.setColor(Color.white);
        g.fillRect(0, 0, 599, 299);
        
        // draw log frequency scale
        DecimalFormat theFormat = new DecimalFormat("#.####");
        double aFreq;
        int index = 0;
        int inner = 0;
        for (index = 1; index < 10; index++)
        {
            for (inner = 0 ; inner < numDecades; inner++)
            {   
                // draw vertical grid lines
                int gridx = (int) (Math.log(index) * theRange
                    / Math.log(10.0) + 0.5 + inner * theRange);
                aFreq = index * startFreq * Math.pow(10.,inner);
                g.setColor(Color.lightGray);
                g.drawLine(gridx, 0, gridx, 299);
                g.setColor(Color.black);
                
                // draw text labels as needed
                switch (numDecades)
                {
                    // handle 4 decades plot
                case 4:
                    switch (index)
                    {
                    case 1:
                    case 3:
                        g.drawLine(gridx, 299, gridx, 304);
                        g.drawString(theFormat.format(aFreq), gridx, 317);
                        break;
                    default:
                        break;
                    }
                    break;
                        
                    // handle 3 decades plot
                case 3:
                    switch (index)
                    {
                    case 1:
                    case 2:
                    case 5:
                        g.drawLine(gridx, 299, gridx, 304);
                        g.drawString(theFormat.format(aFreq), gridx, 317);
                        break;
                    default:
                        break;
                    }
                    break;
                            
                    // handle 2 decades plot
                case 2:
                    switch (index)
                    {
                    case 1:
                    case 2:
                    case 4:
                    case 6:
                        g.drawLine(gridx, 299, gridx, 304);
                        g.drawString(theFormat.format(aFreq), gridx, 317);
                        break;
                    default:
                        break;
                    }
                    break;
                          
                    // should never reach here
                default:
                    break;
                }
            }
        }
        
        // special case for last label on the right
        g.drawLine(599, 299, 599, 304);
        aFreq = startFreq * Math.pow(10.,inner);
        g.drawString(theFormat.format(aFreq), 599, 317);
        
        // draw linear dB scale
        for (index = 1; index < 10; index++)
        {
            // draw horizontal grid lines
            g.setColor(Color.lightGray);
            int gridy = index * 30;
            g.drawLine(0, gridy, 599, gridy);
            
            // draw phase labels if not hidden
            if (!hidePhase)
            {
                g.setColor(Color.black);
                g.drawString(String.valueOf(225-(index*45)), 610, gridy+4);
            }
        }
        
        // draw legend
        g.setColor(Color.black);
        if (unitsHz) {g.drawString("Freq.  (Hz)", 267, 290);}
        else {g.drawString("Freq.  (r/s)", 267, 290);}
        g.setColor(Color.blue);
        g.drawString("Gain (dB)", 10, 18);
        if (!hidePhase)
        {
            g.setColor(Color.magenta);
            g.drawString("Phase (deg)", 520, 18);
        }
        
        // draw outline rectangle on top of everything
        g.setColor(Color.black);
        g.drawRect(0, 0, 599, 299);
        
        // check that data exists before plotting magnitude
        if ((null == theResult) || (0 == theResult.length))
        {
            System.out.println("Error: nothing to plot.");
            return;
        }
        
        // convert all data points to dB
        double[] dBResult = new double[theResult.length];
        double maxDB = -100.0;
        for (index = 0; index < theResult.length; index++)
        {
            // convert magnitude to dB, scaled for 5 pixels/dB
            double theMag = theResult[index].mod();
            theMag = 20.0 * Math.log(theMag) / Math.log(10.0);
            dBResult[index] = theMag;
            
            // save maximum dB value
            maxDB = Math.max(maxDB, theMag);
        }
        
        // limit vertical display to the range (-90...90)
        maxDB = Math.max(Math.min(maxDB, 90), -90);
        
        // snap to vertical grid, draw text labels
        maxDB = 6.0 * Math.round(maxDB/6.0);
        for (index = 0; index < 9; index++)
        {
            g.drawString(String.valueOf((int)(maxDB - (index * 6.))),
                         -35, index*30 + 34);
        }
        
        // reduce clip area while plotting
        g.clipRect(1, 1, 599, 299);
        
        // plot phase response first if not hidden
        int oldy = 0;
        int newy = 0;
        int oldx = 0;
        if (!hidePhase)
        {
            g.setColor(Color.magenta);
            for (index = 0; index < theResult.length; index++)
            {
                // plot on canvas scaled to 300 pixel height
                newy = (int) (150.5-(theResult[index].arg()*120./Math.PI));
                if (0 == index){oldy = newy;}
                else {g.drawLine(oldx, oldy, (int)(index), newy);}
                oldy = newy;
                oldx = index;
            }
        }
        
        // plot magnitude response last
        oldy = 0;
        newy = 0;
        oldx = 0;
        g.setColor(Color.blue);
        for (index = 0; index < theResult.length; index++)
        {
            // plot on canvas scaled to 300 pixel height
            newy = (int) (5.0 * (maxDB - dBResult[index] + 6.) + 0.5);
            if (0 == index){oldy = newy;}
            else {g.drawLine(oldx, oldy, (int)(index), newy);}
            oldy = newy;
            oldx = index;
        }
    }
    
    // draw Nyquist plot in second tabbed pane
    void paintPolar(Graphics g)
    {
        boolean hidePhase = theApp.thePanel.theCheck.isSelected();

        // move origin to make room for text labels
        g.translate(40, 3);
        
        // scale plot to fit width and height
        double xfactor = Math.max(maxReal, -minReal);
        double yfactor = Math.max(maxImag, -minImag);
        double preFactor = Math.max(0.5 * xfactor, yfactor);
        preFactor = Math.max(1.e-6, preFactor);
        
        // find a 'nice' number to set scale factor
        double mult = Math.log(preFactor) / Math.log(10.);
        mult = Math.pow(10., Math.round(mult - 1.5));
        preFactor = mult * Math.round(preFactor / mult);
        final int scale = 13;
        double factor = 10. * scale / preFactor;
        
        // draw white rectangle for plot area
        g.setColor(Color.white);
        g.fillRect(0, 0, 599, 299);
        
        // draw horiz and vert axes
        g.setColor(Color.lightGray);
        g.drawLine(0, 150, 604, 150);
        g.drawLine(300, 0, 300, 304);
    
        // draw ticks and grid lines for vertical axis
        DecimalFormat theFormat = new DecimalFormat("#.######");
        int index;
        for (index = -10; index <= 10; index++)
        {
            if (0 == index % 10)
            {
                g.setColor(Color.gray);
                g.drawLine(294,150+index*scale,306,150+index*scale);
                g.setColor(Color.black);
                g.drawLine(600,150+index*scale,604,150+index*scale);
                String theStr = theFormat.format(index*preFactor/10.);
                g.drawString(theStr, 610, 154-index*scale);
            }
            else if (0 == index % 5)
            {
                g.setColor(Color.gray);
                g.drawLine(296,150+index*scale,304,150+index*scale);
                g.drawLine(600,150+index*scale,604,150+index*scale);
            }
            else
            {
                g.setColor(Color.lightGray);
                g.drawLine(298,150+index*scale,302,150+index*scale);
            }
        }
        
        // draw ticks and grid lines for horizontal axis
        theFormat.applyPattern("#.######");
        for (index = -21; index <= 21; index++)
        {
            g.setColor(Color.lightGray);
            if (0 == index % 10)
            {
                g.setColor(Color.gray);
                g.drawLine(300+index*scale,144,300+index*scale,156);
                g.setColor(Color.black);
                g.drawLine(300+index*scale,300,300+index*scale,304);
                String theStr = theFormat.format(index*preFactor/10.);
                g.drawString(theStr, 300+index*scale, 317);
            }
            else if (0 == index % 5)
            {
                g.setColor(Color.gray);
                g.drawLine(300+index*scale,146,300+index*scale,154);
            }
            else
            {
                g.setColor(Color.lightGray);
                g.drawLine(300+index*scale,148,300+index*scale,152);
            }
        }
        
        // draw outline rectangle on top
        g.setColor(Color.black);
        g.drawRect(0, 0, 599, 299);
        g.drawString("Imaginary Plane", 256, 15);

        // if data exists, plot magnitude
        if ((theResult == null) || (theResult.length == 0))
        {
            System.out.println("Error: nothing to plot.");
            return;
        }

        // reduce clip area while plotting
        g.clipRect(1, 1, 598, 298);

        // draw text label for first point
        int newx = 0;
        int newy = 0;
        int oldx = 0;
        int oldy = 0;
                        
        // plot negative frequencies if enabled
        if (!hidePhase)
        {
            // draw unit circle
            int tickx = (int)(300.5 - factor);
            int dia = (int)(2. * factor);
            g.setColor(Color.lightGray);
            g.drawOval(tickx, tickx-150, dia, dia);
            
            // plot response
            g.setColor(Color.magenta);
            for (index = 0; index < theResult.length; index++)
            {
                newx = (int)(300.5 + factor * theResult[index].real());
                newy = (int)(150.5 + factor * theResult[index].imag());
                if (0 == index)
                {   // draw tick mark at -1
                    oldx = newx;
                    oldy = newy;
                    g.drawLine(tickx, 142, tickx, 158);
                }
                else {g.drawLine(oldx, oldy, newx, newy);}
                oldx = newx;
                oldy = newy;
            }
        }

        // plot continuous curve
        g.setColor(Color.black);
        for (index = 0; index < theResult.length; index++)
        {
            newx = (int)(300.5 + factor * theResult[index].real());
            newy = (int)(150.5 - factor * theResult[index].imag());
            if (0 == index)
            {   // special handling for first point, text label
                oldx = newx;
                oldy = newy;
                g.drawString("a", newx-12, newy-3);
                g.setColor(Color.blue);
            }
            else {g.drawLine(oldx, oldy, newx, newy);}
            oldx = newx;
            oldy = newy;
        }

        // draw text label for last point
        g.setColor(Color.black);
        g.drawString("z", newx+3, newy-3);        
    }
    
    // calculate the complex reponses to all frequencies in input list
    void plotAll(double[] nums, double[] dens)
    {
        // initialize extremes
        maxReal = -1.e10;
        minReal =  1.e10;
        maxImag = -1.e10;
        minImag =  1.e10;
        
        // check for empty arrays
        theResult = null;
        if (nums.length  == 0){return;}
        if (dens.length  == 0){return;}
        if (theFreqs.length == 0){return;}
        
        // allocate memory for result array
        theResult = new Complex[theFreqs.length];
        
        // loop through frequencies in sequence
        int outer = 0;
        for (outer = 0; outer < theFreqs.length; outer++)
        {
            // create complex representation of frequency
            Complex cFreq = new Complex(0, theFreqs[outer]);
            
            // initialize multiplier to unity
            Complex cMult = new Complex(1, 0);
            
            // calculate complex numerator using all coefficients
            Complex cNum = new Complex(0, 0);
            int inner = 0;
            for (inner = 0; inner < nums.length; inner++)
            {
                // get next numerator coefficient
                Complex cCoef = new Complex(nums[inner], 0);
                
                // multiply complex frequency, add to result
                cCoef.mpy(cMult);
                cNum.add(cCoef);
                
                // increase order of complex frequency
                cMult.mpy(cFreq);
            }
            
            // reset multiplier to unity
            cMult = new Complex(1, 0);
            
            // calculate complex denominator using all coefficients
            Complex cDen = new Complex(0, 0);
            for (inner = 0; inner < dens.length; inner++)
            {
                // get next denominator coefficient
                Complex cCoef = new Complex(dens[inner], 0);
                
                // multiply complex frequency, add to result
                cCoef.mpy(cMult);
                cDen.add(cCoef);
                
                // increase order of complex frequency
                cMult.mpy(cFreq);
            }
            
            // divide numerator by denominator
            theResult[outer] = cNum.div(cDen);
            
            // check for extreme values at each frequency
            double realVal = theResult[outer].real();
            double imagVal = theResult[outer].imag();
            maxReal = Math.max(maxReal, realVal);
            minReal = Math.min(minReal, realVal);
            maxImag = Math.max(maxImag, imagVal);
            minImag = Math.min(minImag, imagVal);
        }
    }    
}

//==========================================================\\
// complex values for transfer functions
class Complex
{
    // real part of number
    private double x;

    // imaginary part of number
    private double y;

    // default constructor
    Complex()
    {
        x = 0.0;    // initialize real component
        y = 0.0;    // initialize imaginary component
    }

    // constructor with arguments
    Complex(double r, double i)
    {
        x = r;    // set real component
        y = i;    // set imaginary component
    }

    // return real part of complex number
    double real(){return x;}

    // return imaginary part of complex number
    double imag(){return y;}

    // return the modulus (magnitude) of complex number
    double mod(){return Math.sqrt(x * x + y * y);}

    // return the argument (phase angle in radians) of complex number
    double arg(){return Math.atan2(y, x);}

    // add c to this, return this
    Complex add(Complex c)
    {
        x += c.x;
        y += c.y;
        return this;
    }

    // multiply this by c, return this
    Complex mpy(Complex c)
    {
        double x_new = x*c.x - y*c.y;
        double y_new = x*c.y + y*c.x;

        // move result to this
        x = x_new; y = y_new;
        return this;
    }

    // divide this by c, return this
    Complex div(Complex c)
    {
        // rationalize to make denominator real
        double den = c.x*c.x + c.y*c.y;

        // apply result
        double x_new = (x*c.x + y*c.y)/den;
        double y_new = (c.x*y - c.y*x)/den;

        // move result to this
        x = x_new; y = y_new;
        return this;
    }

    // obtain string representation of complex number
    // designed to be compatible with MS Excel
    public String toString()
    {
        // use scientific notation to 6 sig. figs.
        DecimalFormat theFormat = new DecimalFormat("+#.######E0;-#");
        String result = new String(theFormat.format(x)
            + theFormat.format(y) + "i");
        return result;
    }
}

//==========================================================\\
// this class holds only static methods, no instance data
class Polynomial
{
    // convert list of coefficients from input string
    // to an array of doubles
    static double[] convertPoly(String theStr)
    {
        // obtain numerator coefficients from text field
        theStr = theStr.replace(',', ' ');
        theStr = theStr.trim();
        StringTokenizer theToken = new StringTokenizer(theStr);
        double[] theCoeff = new double[theToken.countTokens()];

        // load coefficients into array in reverse order
        int index = 0;
        for (index = theCoeff.length - 1; index >= 0 ; index--)
        {
            theCoeff[index] = Double.parseDouble(theToken.nextToken());
        }
        return theCoeff;
    }

    // convert list of polynomials from input string
    // to an array of doubles
    static double[] convertPolyList(String theStr)
    {
        // initialize return value to unity
        double[] theReply = {1};

        // polynomials separated by ']' characters
        theStr = theStr.replace('(', ' ');
        theStr = theStr.replace('[', ' ');
        theStr = theStr.replace(')', ';');
        theStr = theStr.replace(']', ';');
        theStr = theStr.trim();
        StringTokenizer theToken = new StringTokenizer(theStr, ";");
        int index = 0;
        for (index =  0; theToken.countTokens() > 0; index++)
        {
            double[] theArray = convertPoly(theToken.nextToken());
            theReply = multiply(theReply, theArray);
        }
        return theReply;
    }

    static double[] multiply(double[] array1, double[] array2)
    {
        double[] theReply = new double[array1.length + array2.length - 1];
        int outer = 0;
        for (outer = 0; outer < array1.length; outer++)
        {
            int inner = 0;
            for (inner = 0; inner < array2.length; inner++)
            {
                theReply[outer + inner] += (array1[outer] * array2[inner]);
            }
        }
        return theReply;
    }

    static void showArray(double[] theArray)
    {
        int index = 0;
        boolean first = true;
        DecimalFormat theFormat = new DecimalFormat("#.######");
        for (index = (theArray.length-1); index >= 0; index--)
        {
            if (!first){System.out.print(", ");}
            System.out.print(theFormat.format(theArray[index]));
            first = false;
        }
        System.out.println();
    }
}
