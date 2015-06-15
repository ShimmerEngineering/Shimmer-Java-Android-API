package com.shimmerresearch.exgConfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.shimmerresearch.driver.ShimmerObject;
import com.shimmerresearch.driver.Util;
import com.shimmerresearch.exgConfig.PanelExGBytes.DisplayModeOptions;

public class PanelExGBytes extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5792297768039226060L;

	private JRadioButton rdoBtnInteger;
	private JRadioButton rdoBtnHex;
//	private TextFieldDocumentListener docListener;
	
	private int i;
	private int txtBoxIndex;
	
	private boolean manuallySetInCode = false;
	private boolean filtering= false;
	public static Boolean setDisplayModeOptions = false;

	public static DisplayModeOptions DisplayModeOptions;
	
	ButtonGroup bg;
	
	protected JRadioButton[] choice;
	
	protected JLabel lblText;
	
	JTextField[] editableBox = new JTextField[20];
	
	protected String[] lblContent = new String[]{"Chip1","Chip2"};
	ShimmerObject mCurrentShimmer;
	
	public enum DisplayModeOptions{
		HEX,
		INT
	}
	private DisplayModeOptions displayMode = setDisplayModeOptions();
	
	public PanelExGBytes(ShimmerObject selectedShimmer) {
		try {
			setUpLayout();
			updateFromShimmer(selectedShimmer);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static com.shimmerresearch.exgConfig.PanelExGBytes.DisplayModeOptions setDisplayModeOptions()
	{
		if(setDisplayModeOptions)
		{
			PanelExGBytes.DisplayModeOptions = DisplayModeOptions.INT;
		}
		else if(!setDisplayModeOptions)
		{
			PanelExGBytes.DisplayModeOptions = DisplayModeOptions.HEX;
		}
		
		return DisplayModeOptions;
	}
	
	public void setUpLayout() throws ParseException {
		
		this.setLayout(new BorderLayout());
		//this.setLayout(new GridLayout(2,12,1,10));
		this.setBackground(null);
		this.setForeground(null);
		this.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
		//this.setOpaque(false);

		rdoBtnInteger = new JRadioButton("int");
		rdoBtnInteger.setBackground(null);
		rdoBtnInteger.setActionCommand("setToInt");
		rdoBtnInteger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("setToInt".equals(e.getActionCommand())) {
					if(displayMode!=DisplayModeOptions.INT){
						displayMode = DisplayModeOptions.INT;
						switchTextBoxView();
						setDisplayModeOptions = true;
						//PanelAdvancedExG.setDisplayModeOptions = false;
					}
				}
			}
	    });
	
		rdoBtnHex = new JRadioButton("hex");
		rdoBtnHex.setBackground(null);
		rdoBtnHex.setActionCommand("setToHex");
		rdoBtnHex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if("setToHex".equals(e.getActionCommand())) {	
					if(displayMode!=DisplayModeOptions.HEX){
						displayMode = DisplayModeOptions.HEX;
						setDisplayModeOptions = false;
						switchTextBoxView();
					}
				}
			}
	    });
		
		bg = new ButtonGroup();
		bg.add(rdoBtnInteger);
		bg.add(rdoBtnHex);
		
		if(displayMode == DisplayModeOptions.HEX){
//			hex.doClick();
			//instantiating a mask of type hex
//			mask = new MaskFormatter("HH");
			rdoBtnHex.setSelected(true);
		}
		else if(displayMode == DisplayModeOptions.INT){
//			integer.doClick();
			//instantiating a mask of type int
//			mask = new MaskFormatter("##");
			rdoBtnInteger.setSelected(true);
		}
		
		JPanel pnlGridCenter = new JPanel();
		pnlGridCenter.setLayout(new GridLayout(2,10,15,10));
		pnlGridCenter.setBackground(Color.white);
		
		JPanel pnlLabels = new JPanel();
		pnlLabels.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
		pnlLabels.setLayout(new GridLayout(2,1,5,10));
		pnlLabels.setBackground(Color.white);
		
		JPanel pnlRadioBtns = new JPanel();
		pnlRadioBtns.setLayout(new GridLayout(2,1,5,10));
		pnlRadioBtns.setBackground(Color.white);
		pnlRadioBtns.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));

		txtBoxIndex = 0;
		for(i = 0; i < 2 ; i++) {
			lblText = new JLabel();
			lblText.setText(lblContent[i]);
			lblText.setForeground(Color.GRAY);
			
			pnlLabels.add(lblText);
			
			if(i == 0) {
				pnlRadioBtns.add(rdoBtnHex);
			}
			else if(i == 1) {
				pnlRadioBtns.add(rdoBtnInteger);
			}
			
			for(int j = 0; j < 10; j++) {
				editableBox[txtBoxIndex] = new JTextField();	
				editableBox[txtBoxIndex].setBackground(Color.white);				
				editableBox[txtBoxIndex].setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
				editableBox[txtBoxIndex].getDocument().addDocumentListener(new TextFieldDocumentListener(editableBox[txtBoxIndex]));
				editableBox[txtBoxIndex].setForeground(Color.gray);
//				editableBox[txtBoxIndex].setPreferredSize(new Dimension(5,5));
//				editableBox[txtBoxIndex].setMaximumSize(new Dimension(10, 10));
//				editableBox[txtBoxIndex].setMinimumSize(new Dimension(10,10));
				pnlGridCenter.add(editableBox[txtBoxIndex]);	
				txtBoxIndex++;
			}
		}
		txtBoxIndex =0;
		
		this.add(pnlLabels,BorderLayout.WEST);
		this.add(pnlGridCenter,BorderLayout.CENTER);
		this.add(pnlRadioBtns, BorderLayout.EAST);
	}

	public void updateFromShimmer(ShimmerObject sO) {
		mCurrentShimmer = sO;
		updateTextBoxes();
	}
	
	public void switchTextBoxView(){
		for(int r = 0; r < 20; r++) {
			byte currentValue = 0;
			String txtToDisplay = editableBox[r].getText().replace(" ", "");
			if(txtToDisplay.isEmpty()){
				txtToDisplay = "00";
			}
			if(displayMode == DisplayModeOptions.HEX){
				currentValue = (byte)Integer.parseInt(txtToDisplay);
				txtToDisplay = Util.bytesToHexString(new byte[]{currentValue});
			}
			else if(displayMode == DisplayModeOptions.INT){
				currentValue = Util.hexStringToByteArray(txtToDisplay)[0];
				txtToDisplay = Util.convertByteToUnsignedIntegerString(currentValue);
			}
			
			manuallySetInCode = true;
			editableBox[r].setText(txtToDisplay);
			manuallySetInCode = false;
		}		
	}

	public void updateTextBoxes() {
		String txtToDisplay = "";
		
		byte[] mEXG1RegisterArray = mCurrentShimmer.getEXG1RegisterArray();
		byte[] mEXG2RegisterArray = mCurrentShimmer.getEXG2RegisterArray();
		
		for(int r = 0; r < 20; r++) {
			byte[] bytesToRef = mEXG1RegisterArray;
			int offset = 0;
			if(r>=10){
				bytesToRef = mEXG2RegisterArray;
				offset = 10;
			}
			
			if(displayMode == DisplayModeOptions.HEX){
				txtToDisplay = Util.bytesToHexString(new byte[]{bytesToRef[r-offset]});
			}
			else if(displayMode == DisplayModeOptions.INT){
				txtToDisplay = Util.convertByteToUnsignedIntegerString(bytesToRef[r-offset]);
			}

			manuallySetInCode = true;
			editableBox[r].setText(txtToDisplay);
			manuallySetInCode = false;
		}
		//updateToShimmer();
	}
	
	public void updateToShimmer() {
		//updateTextBoxes();
		byte[] mEXG1RegisterArray = new byte[]{(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0};
		byte[] mEXG2RegisterArray = new byte[]{(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0,(byte) 0};
		
		for(int r = 0; r < 20; r++) {
			byte[] bytesToRef = mEXG1RegisterArray;
			int offset = 0;
			if(r>=10){
				bytesToRef = mEXG2RegisterArray;
				offset = 10;
			}
			
			String currentText = "00";
			if(!editableBox[r].getText().isEmpty()){
				currentText = editableBox[r].getText().replace(" ", "");
			}

//			System.out.println(currentText);
			if(displayMode == DisplayModeOptions.HEX){
				try{
					bytesToRef[r-offset] = Util.hexStringToByteArray(currentText)[0];
				}catch(IndexOutOfBoundsException e){
					//System.out.println("hex value may only consist of 2 digits ie. 00 <-> FF");
				}
			}
			else if(displayMode == DisplayModeOptions.INT){
				bytesToRef[r-offset] = (byte)(Integer.parseInt(currentText)&0xFF); 
			}
		}
		
		mCurrentShimmer.exgBytesGetConfigFrom(1, mEXG1RegisterArray);
		mCurrentShimmer.exgBytesGetConfigFrom(2, mEXG2RegisterArray);
	}
		
	public class TextFieldDocumentListener implements DocumentListener {
	    private JTextField textField;
	    
	    public TextFieldDocumentListener(JTextField textField) {
			if(!manuallySetInCode){
				this.textField = textField;
			}
	    }
		
		public void changedUpdate(DocumentEvent e) {
			if(!manuallySetInCode){
				filterText();
				updateConfig(e);
				updateTextBoxes();
			}
		}
		public void removeUpdate(DocumentEvent e) {
			if(!manuallySetInCode){
				filterText();
				updateConfig(e);
			}
		}
		public void insertUpdate(DocumentEvent e) {
			if(!manuallySetInCode){
				filterText();
				updateConfig(e);
			}
		}
		
		public void updateConfig(DocumentEvent e) {
			
//			counter value is set to 20, however this should not be.
			SwingUtilities.invokeLater(new Runnable() {
			  public void run() {
//				  if((displayMode==DisplayModeOptions.INT)
//						  ||((displayMode==DisplayModeOptions.HEX)&&(textField.getText().toCharArray().length==2))){
				  	  filterText();
					  updateToShimmer();
//				  }
			  }
			});
		}
		
		
		private void filterText()
	    {
	        if(filtering)
	            return;
	        filtering= true;

	        EventQueue.invokeLater(new Runnable()
	        {
	            @Override
	            public void run()
	            {
	                String input= textField.getText().toUpperCase();
	                String filtered= "";
	                int index= 0;

					if(displayMode==DisplayModeOptions.HEX){
		                // filter
		                for(int i= 0; i < input.length(); i++)
		                {
		                    char c= input.charAt(i);
		                    if("0123456789ABCDEF".indexOf(c) >= 0)  // hex only
		                    {
		                        filtered+= c;
//		                        if(index++ % 2 == 1 && i != input.length() - 1)
//		                            filtered+= " "; // whitespace after each byte
		                    }
		                }

		                // limit size
		                int maxBytes= 1; //256;
		                if(filtered.length() > 2 * maxBytes)
		                {
		                	filtered= filtered.substring(0, 2 * maxBytes);
//		                    Toolkit.getDefaultToolkit().beep();
		                }
		                textField.setText(filtered);
					}
					else if(displayMode==DisplayModeOptions.INT){
		                // filter
		                for(int i= 0; i < input.length(); i++)
		                {
		                    char c= input.charAt(i);
		                    if("0123456789".indexOf(c) >= 0)  // hex only
		                    {
		                        filtered+= c;
		                    }
		                }
		                if(!filtered.isEmpty()){
							int value = Integer.parseInt(filtered);
							if(value>255){
								value = 255;
							}
		                    filtered = Integer.toString(value);
			                textField.setText(filtered);
		                }
					}
	                filtering= false;
	            }
	        });
	    }
	}
	
//	public static void main(String[] args) {
//	    SwingUtilities.invokeLater(new Runnable() {
//	    	
//	        public void run() {
//	        	
//	    		PanelExGBytes pnlExGBytes = new PanelExGBytes();
//	    		
//	    		SlotDetails sD = new SlotDetails("Temp", 1);
//	    		sD.setDefaultShimmerConfiguration();
//	    		
//	    		pnlExGBytes.updateFromShimmer(sD);
//	            JFrame f = new JFrame();
//	            f.getContentPane().add(pnlExGBytes);
//	            f.setSize(800, 200);
//	            f.setVisible(true);
//	            f.setResizable(false);
//	            f.addWindowListener(new WindowAdapter() {
//	    		    @Override
//	    		    public void windowClosing(WindowEvent e) {
//	    		        System.exit(1);
//	    		    }
//	    		});
//	        }
//	    });
//    }


}
