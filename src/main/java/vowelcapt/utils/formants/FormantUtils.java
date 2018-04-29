package vowelcapt.utils.formants;

import de.fau.cs.jstk.exceptions.MalformedParameterStringException;
import de.fau.cs.jstk.framed.*;
import de.fau.cs.jstk.sampled.AudioFileReader;
import de.fau.cs.jstk.sampled.AudioSource;
import de.fau.cs.jstk.sampled.RawAudioFormat;
import vowelcapt.utils.account.Account;
import vowelcapt.utils.account.AccountUtils;

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
    private AccountUtils accountUtils = new AccountUtils();
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

    //example from jstk https://github.com/sikoried/jstk
    public double[] findFormants(String userName, String userGender, char vowel) {
        String filePath = "resources/accounts/" + userName + "/" + vowel + "_justVowel.wav";
        try {
            AudioSource as = new AudioFileReader(filePath,
                    RawAudioFormat.create("f:" + filePath),
                    true);
            Window wnd = new HammingWindow(as, 25, 10, false);
            AutoCorrelation acf = new SimpleACF(wnd);
            LPCSpectrum lpc = new LPCSpectrum(acf, 22, true);
            Formants fs = new Formants(lpc, as.getSampleRate(), 3);
            double[] buf = new double[fs.getFrameSize()];
            List<Double> firstFormantValues = new ArrayList<>();
            List<Double> secondFormantValues = new ArrayList<>();
            List<String> logMessages = new ArrayList<>();

            logMessages.add("\nFirst, second and third formant raw values");
            while (fs.read(buf)) {
                logMessages.add(Arrays.toString(buf));
                firstFormantValues.add(buf[0]);
                secondFormantValues.add(buf[1]);
            }

            accountUtils.saveToLog(userName, logMessages);

            double firstFormantAverage = calculateAverageFormantValue(userName, userGender, 1, firstFormantValues, vowel);
            double secondFormantAverage = calculateAverageFormantValue(userName, userGender, 2, secondFormantValues, vowel);
            return new double[]{firstFormantAverage, secondFormantAverage};

        } catch (UnsupportedAudioFileException | IOException | MalformedParameterStringException e) {
            e.printStackTrace();
        }
        return new double[]{0, 0};
    }

    private double calculateAverageFormantValue(String userName, String gender, int formant, List<Double> formantValues, char vowel) {
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

            List<String> logMessages = new ArrayList<>();
            logMessages.add(userName + " " + gender + ": Vowel: " + vowel + " " + formant +
                    ". formant mean value: " + formantStatisticalMeanValue);

            List<Double> suitableFormantValues = formantValues.stream()
                    .filter(e -> e >= formantStatisticalMeanValue - 200 * formant
                            && e <= formantStatisticalMeanValue + 200 * formant)
                    .collect(toList());

            logMessages.add("Suitable values:");
            logMessages.add(suitableFormantValues.toString());

            averageFormantValue = suitableFormantValues.stream()
                    .mapToDouble(e -> e)
                    .average();

            logMessages.add("average formant value: " + averageFormantValue.toString());
            accountUtils.saveToLog(userName, logMessages);
        }

        return averageFormantValue.isPresent() ? averageFormantValue.getAsDouble() : 0;
    }

    public boolean isWithinStandardDeviation(char vowel, Account account,
                                             double firstFormantAverageValue, double secondFormantAverageValue) {
        List<VowelInfo> vowels;
        VowelInfo vowelInfo;
        if (account.getGender().equals("male")) {
            vowels = maleVowels;
        } else vowels = femaleVowels;

        Optional<VowelInfo> vowelInfoOptional = vowels.stream()
                .filter(e -> e.getVowel() == vowel)
                .findAny();

        if (vowelInfoOptional.isPresent()) {
            vowelInfo = vowelInfoOptional.get();
            boolean isFirstFormantWithinStandardDeviation =
                    firstFormantAverageValue >= vowelInfo.getFirstFormantMean()
                            - (vowelInfo.getFirstFormantSd() + vowelInfo.getFirstFormantSd() / 5)
                            && firstFormantAverageValue <= vowelInfo.getFirstFormantMean()
                            + (vowelInfo.getFirstFormantSd() + vowelInfo.getFirstFormantSd() / 5);

            boolean isSecondFormantWithinStandardDeviation =
                    secondFormantAverageValue >= vowelInfo.getSecondFormantMean()
                            - (vowelInfo.getSecondFormantSd() + vowelInfo.getSecondFormantSd() / 5)
                            && secondFormantAverageValue <= vowelInfo.getSecondFormantMean()
                            + (vowelInfo.getSecondFormantSd() + vowelInfo.getSecondFormantSd() / 5);

            List<String> logMessages = new ArrayList<>();
            logMessages.add(account.getUserName() + " " + account.getGender() + ": F1 value: "
                    + firstFormantAverageValue + " is within "
                    + vowelInfo.getFirstFormantMean() + " +- " + vowelInfo.getFirstFormantSd() + " : "
                    + isFirstFormantWithinStandardDeviation);

            logMessages.add(account.getUserName() + " " + account.getGender() + ": F2 value: "
                    + secondFormantAverageValue + " is within "
                    + vowelInfo.getSecondFormantMean() + " +- " + vowelInfo.getSecondFormantSd() + " : "
                    + isSecondFormantWithinStandardDeviation);

            logMessages.add("is result within standard deviation? " + (isFirstFormantWithinStandardDeviation
                    && isSecondFormantWithinStandardDeviation));

            accountUtils.saveToLog(account.getUserName(), logMessages);
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
