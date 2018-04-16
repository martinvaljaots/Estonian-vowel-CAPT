package vowelcapt.utils.formants;

public class FormantResults {

    private final double firstFormantAverage;
    private final double secondFormantAverage;
    private final boolean withinStandardDeviation;

    public FormantResults(double firstFormantAverage, double secondFormantAverage, boolean withinStandardDeviation) {
        this.firstFormantAverage = firstFormantAverage;
        this.secondFormantAverage = secondFormantAverage;
        this.withinStandardDeviation = withinStandardDeviation;
    }

    public double getFirstFormantAverage() {
        return firstFormantAverage;
    }

    public double getSecondFormantAverage() {
        return secondFormantAverage;
    }

    public boolean isWithinStandardDeviation() {
        return withinStandardDeviation;
    }

    @Override
    public String toString() {
        return firstFormantAverage + " " + secondFormantAverage + " " + withinStandardDeviation;
    }
}
