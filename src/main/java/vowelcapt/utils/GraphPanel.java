package vowelcapt.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


// Code taken from a SoundDetector example in TarsosDSP
public final class GraphPanel extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = 5969781241442094359L;
        public double threshold = -50;
        private double maxLevel = -1000;
        private long currentModulo = System.currentTimeMillis()/15000;
        private List<Double> levels;
        private List<Long> startTimes;

        public GraphPanel(double defaultThreshold){
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

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 300);
        }

    }
