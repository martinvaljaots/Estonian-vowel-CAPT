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


package vowelcapt.panels;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SoundDetector extends JPanel implements AudioProcessor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3501426880288136245L;

	public JTextArea textArea;
	ArrayList<Clip> clipList;
	int counter;
	public double threshold = -80;
	AudioDispatcher dispatcher;
	Mixer currentMixer;
	public final GaphPanel graphPanel;
	JVMAudioInputStream audioInputStream;
	public SilenceDetector silenceDetector;
	

	public SoundDetector(JSlider thresholdSlider) {
		this.setLayout(new BorderLayout());
		// this.threshold = SilenceDetector.DEFAULT_SILENCE_THRESHOLD;
		//this.silenceDetector = new SilenceDetector(threshold, false);
        setSilenceDetector(new SilenceDetector(threshold, false));
		
		// JPanel inputPanel = new InputPanel();
		// add(inputPanel);
		/*
		inputPanel.addPropertyChangeListener("mixer",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						try {
							setNewMixer((Mixer) arg0.getNewValue());
						} catch (LineUnavailableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnsupportedAudioFileException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		*/

		JPanel params = new JPanel(new BorderLayout());
		params.setBorder(new TitledBorder("2. Set the algorithm parameters"));
		
		JLabel label = new JLabel("Threshold");
		label.setToolTipText("Energy level when sound is counted (dB SPL).");
		params.add(label,BorderLayout.NORTH);
		params.add(thresholdSlider,BorderLayout.CENTER);
		
		JPanel inputAndParamsPanel = new JPanel(new BorderLayout());
		//inputAndParamsPanel.add(inputPanel,BorderLayout.NORTH);
		inputAndParamsPanel.add(params,BorderLayout.SOUTH);

		
		JPanel panelWithTextArea = new JPanel(new BorderLayout());
		textArea = new JTextArea(8,30);
		textArea.setEditable(false);
		panelWithTextArea.add(inputAndParamsPanel,BorderLayout.NORTH);
		panelWithTextArea.add(new JScrollPane(textArea),BorderLayout.CENTER);

		add(panelWithTextArea,BorderLayout.NORTH);
		
	
		graphPanel = new GaphPanel(threshold);
		graphPanel.setSize(80,100);
		add(graphPanel,BorderLayout.CENTER);
	}
	
	public static final class GaphPanel extends JPanel{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5969781241442094359L;
		public double threshold = -50;
		private double maxLevel = -1000;
		private long currentModulo = System.currentTimeMillis()/15000;
		private List<Double> levels;
		private List<Long> startTimes;
		
		public GaphPanel(double defaultThreshold){			
			setThresholdLevel(defaultThreshold);
			levels = new ArrayList<Double>();
			startTimes=new ArrayList<Long>();
			setMinimumSize(new Dimension(80,60)); 
		}
		
		private void setMaxLevel(double newMaxLevel){
			if(newMaxLevel> maxLevel){
				maxLevel=newMaxLevel;
			}
		}
		
		public void setThresholdLevel(double newThreshold){
			threshold=newThreshold;
			repaint();
		}
		
		public void addDataPoint(double level,long ms){
			levels.add(level);
			startTimes.add(ms);
			setMaxLevel(level);
			repaint();
		}
		
		public void paintComponent(Graphics g) {
	        super.paintComponent(g); //paint background
	        g.setColor(Color.BLACK);
			g.fillRect(0, 0,getWidth(), getHeight());
			
			if(System.currentTimeMillis()/15000 > currentModulo){
				currentModulo = System.currentTimeMillis()/15000;
				levels.clear();
				startTimes.clear();
			}
			
	
			for(int i =0 ; i < levels.size();i++){
				g.setColor( levels.get(i) > threshold ? Color.GREEN:Color.ORANGE ); 
				int x = msToXCoordinate(startTimes.get(i));
				int y = levelToYCoordinate(levels.get(i));
				g.drawLine(x, y, x+1, y);
			}
			
			int thresholdYCoordinate = levelToYCoordinate(threshold);
			g.setColor(Color.ORANGE);
			g.drawLine(0, thresholdYCoordinate, getWidth(),thresholdYCoordinate);
			g.drawString(String.valueOf((int)threshold), 0, thresholdYCoordinate + 15);
			
			
			int maxYCoordinate = levelToYCoordinate(maxLevel);
			g.setColor(Color.RED);
			g.drawLine(0, maxYCoordinate, getWidth(),maxYCoordinate);
			g.drawString(String.valueOf(((int)(maxLevel*100))/100.0), getWidth() - 40, maxYCoordinate + 15);
			
	    }
		
		private int levelToYCoordinate(double level){
			int inPixels = (int)((120 + level)  / 120 * (getHeight()-1));
			int yCoordinate =  getHeight() - inPixels;
			return yCoordinate;
		}
		
		private int msToXCoordinate(long ms){
			return (int) ((ms % 15000)/15000.0 * getWidth());
		}
				
	}
	
	
/*
	private JSlider initialzeThresholdSlider() {
		JSlider thresholdSlider = new JSlider(-120,0);
		thresholdSlider.setValue((int)threshold);
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setMajorTickSpacing(20);
		thresholdSlider.setMinorTickSpacing(10);
		thresholdSlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				threshold = source.getValue();
				graphPanel.setThresholdLevel(threshold);
			    if (!source.getValueIsAdjusting()) {			        
			        try {
						setNewMixer(currentMixer);
					} catch (LineUnavailableException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (UnsupportedAudioFileException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    }
			}
		});
		return thresholdSlider;
	}

	private void setNewMixer(Mixer mixer) throws LineUnavailableException,
			UnsupportedAudioFileException {
		
		if(dispatcher!= null){
			dispatcher.stop();
		}
		currentMixer = mixer;
		
		float sampleRate = 44100;
		int bufferSize = 512;
		int overlap = 0;
		
		textArea.append("Started listening with " + Shared.toLocalString(mixer.getMixerInfo().getName()) + "\n\tparams: " + threshold + "dB\n");

		AudioFormat format = new AudioFormat(sampleRate, 16, 1, true,
				true);
		DataLine.Info dataLineInfo = new DataLine.Info(
				TargetDataLine.class, format);
		TargetDataLine line;
		line = (TargetDataLine) mixer.getLine(dataLineInfo);
		int numberOfSamples = bufferSize;
		line.open(format, numberOfSamples);
		line.start();
		AudioInputStream stream = new AudioInputStream(line);

		JVMAudioInputStream audioStream = new JVMAudioInputStream(stream);
		// create a new dispatcher
		dispatcher = new AudioDispatcher(this.audioInputStream, bufferSize,
				overlap);

		// add a processor, handle percussion event.
		// silenceDetector = new SilenceDetector(threshold,false);
		dispatcher.addAudioProcessor(silenceDetector);
		dispatcher.addAudioProcessor(this);

		// run the dispatcher (on a new thread).
		new Thread(dispatcher,"Audio dispatching").start();
	}

	/*
	public static void main(String... strings) throws InterruptedException,
			InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new SoundDetector();
				frame.pack();
				frame.setSize(640,480);
				frame.setVisible(true);
			}
		});
	}
	*/

	@Override
	public boolean process(AudioEvent audioEvent) {
		handleSound();
		return true;
	}

	private void handleSound(){
		if(silenceDetector.currentSPL() > threshold){
			textArea.append("Sound detected at:" + System.currentTimeMillis() + ", " + (int)(silenceDetector.currentSPL()) + "dB SPL\n");
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
		graphPanel.addDataPoint(silenceDetector.currentSPL(), System.currentTimeMillis());		
	}
	@Override
	public void processingFinished() {		
		
	}

	public void setSilenceDetector(SilenceDetector silenceDetectorNew) {
	    silenceDetector = silenceDetectorNew;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
        this.graphPanel.setThresholdLevel(this.threshold);
    }
}
