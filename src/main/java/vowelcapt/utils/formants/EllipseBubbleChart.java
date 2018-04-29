package vowelcapt.utils.formants;

import javafx.scene.chart.Axis;
import javafx.scene.chart.BubbleChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Ellipse;

import java.util.Optional;


/**
 * Origin: https://stackoverflow.com/a/38614934
 * Modified to create accurate ellipse-shaped target areas for each vowel.
 * Renamed from CircularBubbleChart because the bubbles are ellipses.
 */
public class EllipseBubbleChart<X, Y> extends BubbleChart<X, Y> {

    private double xAxisConstantFemale = 3.4965034965034;
    private double xAxisConstantMale = 2.7972027972027;
    private double yAxisConstant = 2.5235288033922;
    private String userGender;

    public EllipseBubbleChart(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        final double xAxisConstant = userGender.equals("male") ? xAxisConstantMale : xAxisConstantFemale;
        getData().stream().flatMap(series -> series.getData().stream())
                .map(Data::getNode)
                .map(StackPane.class::cast)
                .map(StackPane::getShape)
                .map(Ellipse.class::cast)
                .forEach(ellipse -> {
                    FormantUtils formantUtils = new FormantUtils();
                    Optional<VowelInfo> vowelInfo = formantUtils.getVowels(userGender).stream()
                            .filter(vowel -> vowel.getFirstFormantSd() == Math.round((ellipse.getRadiusY()
                                    * 100 / 120 * yAxisConstant)))
                            .findFirst();
                    vowelInfo.ifPresent(vowel -> ellipse.setRadiusX((vowel.getSecondFormantSd()
                            + (vowel.getSecondFormantSd() / 5)) / xAxisConstant));
                });
    }
}
