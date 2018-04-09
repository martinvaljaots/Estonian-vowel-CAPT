package vowelcapt.utils;

public class VowelInfo {

    private char vowel;
    private double[] formantValues;

    public VowelInfo(char vowel, double[] formantValues) {
        this.vowel = vowel;
        this.formantValues = formantValues;
    }

    public char getVowel() {
        return vowel;
    }

    public double[] getFormantValues() {
        return formantValues;
    }

    @Override
    public String toString() {
        return vowel + " " + formantValues[0] + " " + formantValues[1];
    }
}
