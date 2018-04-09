package vowelcapt.helpers;

import de.fau.cs.jstk.exceptions.MalformedParameterStringException;
import de.fau.cs.jstk.framed.*;
import de.fau.cs.jstk.sampled.AudioFileReader;
import de.fau.cs.jstk.sampled.AudioSource;
import de.fau.cs.jstk.sampled.RawAudioFormat;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class FormantUtils {

    private List<VowelInfo> vowels = new ArrayList<>();

    public FormantUtils() {
        initializeVowelInfo();
    }

    private void initializeVowelInfo() {
        //TODO: add sex based vowel info initialization
        Path path = FileSystems.getDefault().getPath("resources/formant_values/male.csv");
        try {
            vowels = Files.lines(path)
                    .skip(1)
                    .map(mapToVowelInfo)
                    .collect(toList());
            vowels.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Function<String, VowelInfo> mapToVowelInfo = (line) -> {
        String[] vowelInfoStringArray = line.split(";");
        char vowel = vowelInfoStringArray[0].charAt(0);
        double firstFormantValue = Double.parseDouble(vowelInfoStringArray[1]);
        double secondFormantValue = Double.parseDouble(vowelInfoStringArray[2]);
        return new VowelInfo(vowel, new double[]{firstFormantValue, secondFormantValue});
    };

    public void findFormants(char vowel) {
        String[] trial = new String[]{"AK/VowelPartTrial.wav", "3"};
        try {
            AudioSource as = new AudioFileReader(trial[0],
                    RawAudioFormat.create(trial.length > 2 ? trial[1] : "f:" + trial[0]),
                    true);
            Window wnd = new HammingWindow(as, 25, 10, false);
            // AutoCorrelation acf = new FastACF(wnd);
            AutoCorrelation acf = new SimpleACF(wnd);
            LPCSpectrum lpc = new LPCSpectrum(acf, 22, true);
            Formants fs = new Formants(lpc, as.getSampleRate(), Integer.parseInt(trial[1]));
            System.out.println(as.getSampleRate());
            System.out.println(Arrays.toString(trial));
            System.out.println(wnd.getFrameSize());
            System.out.println(acf);
            System.out.println(lpc.getFrameSize());
            System.out.println(fs);
            double[] buf = new double[fs.getFrameSize()];
            List<Double> firstFormantValues = new ArrayList<>();
            List<Double> secondFormantValues = new ArrayList<>();

            while (fs.read(buf)) {
                System.out.println(Arrays.toString(buf));
                firstFormantValues.add(buf[0]);
                secondFormantValues.add(buf[1]);
            }

            System.out.println(calculateAverageFormantValue(1, firstFormantValues, vowel));
            System.out.println(calculateAverageFormantValue(2, secondFormantValues, vowel));

        } catch (UnsupportedAudioFileException | IOException | MalformedParameterStringException e) {
            e.printStackTrace();
        }
    }

    private double calculateAverageFormantValue(int formant, List<Double> formantValues, char vowel) {
        OptionalDouble averageFormantValue = OptionalDouble.empty();
        Optional<VowelInfo> vowelInfoOptional = vowels.stream()
                .filter(e -> e.getVowel() == vowel)
                .findAny();

        if (vowelInfoOptional.isPresent()) {
            double formantStatisticalAverageValue = vowelInfoOptional.get().getFormantValues()[formant - 1];

            averageFormantValue = formantValues.stream()
                    .filter(e -> e >= formantStatisticalAverageValue - 200 * formant && e <= formantStatisticalAverageValue + 200 * formant)
                    .mapToDouble(e -> e)
                    .average();
        }

        return averageFormantValue.isPresent() ? averageFormantValue.getAsDouble() : 0;
    }
}
