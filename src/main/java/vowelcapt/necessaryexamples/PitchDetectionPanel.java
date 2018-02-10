/*
*      _______                       _____   _____ _____  
*     |__   __|                     |  __ \ / ____|  __ \ 
*        | | __ _ _ __ ___  ___  ___| |  | | (___ | |__) |
*        | |/ _` | '__/ __|/ _ \/ __| |  | |\___ \|  ___/ 
*        | | (_| | |  \__ \ (_) \__ \ |__| |____) | |     
*        |_|\__,_|_|  |___/\___/|___/_____/|_____/|_|     
*                                                         
* -------------------------------------------------------------
*
* TarsosDSP is developed by Joren Six at IPEM, University Ghent
*  
* -------------------------------------------------------------
*
*  Info: http://0110.be/tag/TarsosDSP
*  Github: https://github.com/JorenSix/TarsosDSP
*  Releases: http://0110.be/releases/TarsosDSP/
*  
*  TarsosDSP includes modified source code by various authors,
*  for credits and info, see README.
* 
*/


package vowelcapt.necessaryexamples;

import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class PitchDetectionPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5107785666165487335L;

	private PitchEstimationAlgorithm algo;
	
	public PitchDetectionPanel(ActionListener algoChangedListener){
		super(new GridLayout(0,1));
		setBorder(new TitledBorder("2. Choose a pitch detection algorithm"));
		ButtonGroup group = new ButtonGroup();
		algo = PitchEstimationAlgorithm.YIN;
		for (PitchEstimationAlgorithm value : PitchEstimationAlgorithm.values()) {
			JRadioButton button = new JRadioButton();
			button.setText(value.toString());
			add(button);
			group.add(button);
			button.setSelected(value == algo);
			button.setActionCommand(value.name());
			button.addActionListener(algoChangedListener);
		}
	}
}
