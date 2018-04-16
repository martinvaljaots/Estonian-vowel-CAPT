package vowelcapt.utils.formants;

public class VowelInfo {

    private char vowel;
    private double firstFormantMean;
    private double firstFormantSd;
    private double secondFormantMean;
    private double secondFormantSd;

    VowelInfo(char vowel, double firstFormantMean, double firstFormantSd,
              double secondFormantMean, double secondFormantSd) {
        this.vowel = vowel;
        this.firstFormantMean = firstFormantMean;
        this.firstFormantSd = firstFormantSd;
        this.secondFormantMean = secondFormantMean;
        this.secondFormantSd = secondFormantSd;
    }

    public char getVowel() {
        return vowel;
    }

    public double getFirstFormantMean() {
        return firstFormantMean;
    }

    public double getFirstFormantSd() {
        return firstFormantSd;
    }

    public double getSecondFormantMean() {
        return secondFormantMean;
    }

    public double getSecondFormantSd() {
        return secondFormantSd;
    }

    @Override
    public String toString() {
        return vowel + " first formant mean: " + firstFormantMean + " first formant SD: " + firstFormantSd
                + " second formant mean: " + secondFormantMean + " second formant SD: " + secondFormantSd;
    }
}
