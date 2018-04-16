package vowelcapt.utils;

import de.fau.cs.jstk.exceptions.MalformedParameterStringException;
import de.fau.cs.jstk.framed.*;
import de.fau.cs.jstk.sampled.AudioFileReader;
import de.fau.cs.jstk.sampled.AudioSource;
import de.fau.cs.jstk.sampled.RawAudioFormat;
import vowelcapt.utils.helpers.FormantResults;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class FormantUtils {

    private List<VowelInfo> maleVowels = new ArrayList<>();
    private List<VowelInfo> femaleVowels = new ArrayList<>();
    private static List<FormantResults> formantResultsStorage = new ArrayList<>();
    private final Object lock = new Object();

    public FormantUtils() {
        initializeVowelInfo();
    }

    private void initializeVowelInfo() {
        Path maleVowelsPath = FileSystems.getDefault().getPath("resources/formant_values/male.csv");
        Path femaleVowelsPath = FileSystems.getDefault().getPath("resources/formant_values/female.csv");

        Charset charset = Charset.forName("ISO-8859-1");

        try {
            maleVowels = Files.lines(maleVowelsPath, charset)
                    .skip(1)
                    .map(mapToVowelInfo)
                    .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            femaleVowels = Files.lines(femaleVowelsPath, charset)
                    .skip(1)
                    .map(mapToVowelInfo)
                    .collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Function<String, VowelInfo> mapToVowelInfo = (line) -> {
        String[] vowelInfoStringArray = line.split(";");
        char vowel = vowelInfoStringArray[0].charAt(0);
        double firstFormantMeanValue = Double.parseDouble(vowelInfoStringArray[1]);
        double firstFormantSdValue = Double.parseDouble(vowelInfoStringArray[2]);
        double secondFormantMeanValue = Double.parseDouble(vowelInfoStringArray[3]);
        double secondFormantSdValue = Double.parseDouble(vowelInfoStringArray[4]);
        return new VowelInfo(vowel, firstFormantMeanValue, firstFormantSdValue,
                secondFormantMeanValue, secondFormantSdValue);
    };

    public double[] findFormants(String userName, String userGender, char vowel) {
        String filePath = "resources/accounts/" + userName + "/" + vowel + "_justVowel.wav";
        try {
            AudioSource as = new AudioFileReader(filePath,
                    RawAudioFormat.create("f:" + filePath),
                    true);
            Window wnd = new HammingWindow(as, 25, 10, false);
            // AutoCorrelation acf = new FastACF(wnd);
            AutoCorrelation acf = new SimpleACF(wnd);
            LPCSpectrum lpc = new LPCSpectrum(acf, 22, true);
            Formants fs = new Formants(lpc, as.getSampleRate(), 3);
            System.out.println(as.getSampleRate());
            System.out.println(filePath);
            System.out.println(wnd.getFrameSize());
            System.out.println(acf);
            System.out.println(lpc.getFrameSize());
            System.out.println(fs);
            double[] buf = new double[fs.getFrameSize()];
            List<Double> firstFormantValues = new ArrayList<>();
            List<Double> secondFormantValues = new ArrayList<>();

            while (fs.read(buf)) {
                //TODO: don't sout this, log it
                //System.out.println(Arrays.toString(buf));
                firstFormantValues.add(buf[0]);
                secondFormantValues.add(buf[1]);
            }

            double firstFormantAverage = calculateAverageFormantValue(userGender, 1, firstFormantValues, vowel);
            double secondFormantAverage = calculateAverageFormantValue(userGender, 2, secondFormantValues, vowel);
            return new double[]{firstFormantAverage, secondFormantAverage};

        } catch (UnsupportedAudioFileException | IOException | MalformedParameterStringException e) {
            e.printStackTrace();
        }
        return new double[]{0, 0};
    }

    private double calculateAverageFormantValue(String gender, int formant, List<Double> formantValues, char vowel) {
        List<VowelInfo> vowels;
        if (gender.equals("male")) {
            vowels = maleVowels;
        } else vowels = femaleVowels;
        OptionalDouble averageFormantValue = OptionalDouble.empty();
        Optional<VowelInfo> vowelInfoOptional = vowels.stream()
                .filter(e -> e.getVowel() == vowel)
                .findAny();

        if (vowelInfoOptional.isPresent()) {
            double formantStatisticalMeanValue;
            if (formant == 1) {
                formantStatisticalMeanValue = vowelInfoOptional.get().getFirstFormantMean();
            } else formantStatisticalMeanValue = vowelInfoOptional.get().getSecondFormantMean();

            System.out.println("Vowel: " + vowel + " " + formant + ". formant mean value: " + formantStatisticalMeanValue);

            averageFormantValue = formantValues.stream()
                    .filter(e -> e >= formantStatisticalMeanValue - 200 * formant
                            && e <= formantStatisticalMeanValue + 200 * formant)
                    .mapToDouble(e -> e)
                    .average();
        }

        return averageFormantValue.isPresent() ? averageFormantValue.getAsDouble() : 0;
    }

    public boolean isWithinStandardDeviation(char vowel, String gender,
                                             double firstFormantAverageValue, double secondFormantAverageValue) {
        List<VowelInfo> vowels;
        VowelInfo vowelInfo;
        if (gender.equals("male")) {
            vowels = maleVowels;
        } else vowels = femaleVowels;

        Optional<VowelInfo> vowelInfoOptional = vowels.stream()
                .filter(e -> e.getVowel() == vowel)
                .findAny();

        if (vowelInfoOptional.isPresent()) {
            vowelInfo = vowelInfoOptional.get();
            boolean isFirstFormantWithinStandardDeviation =
                    firstFormantAverageValue >= vowelInfo.getFirstFormantMean() - vowelInfo.getFirstFormantSd()
                            && firstFormantAverageValue <= vowelInfo.getFirstFormantMean() + vowelInfo.getFirstFormantSd();

            boolean isSecondFormantWithinStandardDeviation =
                    secondFormantAverageValue >= vowelInfo.getSecondFormantMean() - vowelInfo.getSecondFormantSd()
                            && secondFormantAverageValue <= vowelInfo.getSecondFormantMean() + vowelInfo.getSecondFormantSd();

            System.out.println("F1 value: " + firstFormantAverageValue + " is within "
                    + vowelInfo.getFirstFormantMean() + " +- " + vowelInfo.getFirstFormantSd() + " : "
                    + isFirstFormantWithinStandardDeviation);

            System.out.println("F2 value: " + secondFormantAverageValue + " is within "
                    + vowelInfo.getSecondFormantMean() + " +- " + vowelInfo.getSecondFormantSd() + " : "
                    + isSecondFormantWithinStandardDeviation);

            return isFirstFormantWithinStandardDeviation && isSecondFormantWithinStandardDeviation;
        }

        return false;
    }

    public List<VowelInfo> getVowels(String gender) {
        if (gender.equals("male")) {
            return maleVowels;
        }
        return femaleVowels;
    }

    public synchronized Optional<FormantResults> getLastResults() {
        synchronized (lock) {
            while (formantResultsStorage.isEmpty()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return Optional.of(formantResultsStorage.remove(0));
        }
    }

    public void addLastResults(FormantResults formantResults) {
        synchronized (lock) {
            formantResultsStorage.add(formantResults);
            lock.notifyAll();
        }
    }
}
