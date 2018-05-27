# Estonian-vowel-CAPT

A prototype CAPT tool for learning Estonian vowel pronunciation.

The tool has a pronunciation exercise for each vowel in the Estonian language where the user can record themselves and get feedback on their pronunciation.
The tool also features a listening exercise to learn to differentiate between Estonian vowel quantity degrees.


### Running

The easiest way to run the application is to run the pre-compiled JAR file (IAPM_main.jar). Java is required to do so.
Another way is to use an IDE. This requires Java, an IDE of your choosing (I used IntelliJ) and Gradle for managing dependencies.


### Acknowledgements

The application uses:
* jstk (https://github.com/sikoried/jstk) for finding formant values, which are used to evaluate pronunciation 
* TarsosDSP (https://github.com/JorenSix/TarsosDSP) for setting a volume threshold for recording the vowel segment, as well as for visualizing the volume level when the user adjusts it

The thesis was supervised by Einar Meister, PhD.
