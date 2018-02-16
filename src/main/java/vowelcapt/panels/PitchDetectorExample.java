package vowelcapt.panels;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;

public class PitchDetectorExample extends JPanel implements PitchDetectionHandler {

	private static final long serialVersionUID = 3501426880288136245L;

	private final JTextArea textArea;
	
	public PitchEstimationAlgorithm algo;
	public PitchDetectorExample() {
		this.setLayout(new GridLayout(0, 1));
		algo = PitchEstimationAlgorithm.YIN;
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		add(new JScrollPane(textArea));
	}

	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult,AudioEvent audioEvent) {
		if(pitchDetectionResult.getPitch() != -1){
			double timeStamp = audioEvent.getTimeStamp();
			float pitch = pitchDetectionResult.getPitch();
			float probability = pitchDetectionResult.getProbability();
			double rms = audioEvent.getRMS() * 100;
			String message = String.format("Pitch detected at %.2fs: %.2fHz ( %.2f probability, RMS: %.5f )\n", timeStamp,pitch,probability,rms);
			textArea.append(message);
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}
}
