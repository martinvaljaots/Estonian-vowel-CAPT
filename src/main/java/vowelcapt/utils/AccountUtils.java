package vowelcapt.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class AccountUtils {

    private List<Account> accounts = new ArrayList<>();

    public AccountUtils() {
        initializeAccountsList();
    }

    private void initializeAccountsList() {

        Path path = FileSystems.getDefault().getPath("resources/accounts/acc.csv");
        try {
            accounts = Files.lines(path)
                    .skip(1)
                    .map(mapToAccount)
                    .collect(toList());
            accounts.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Function<String, Account> mapToAccount = (line) -> {
        String[] accountInfoStringArray = line.split(";");
        String userName = accountInfoStringArray[0];
        String password = accountInfoStringArray[1];
        String sex = accountInfoStringArray[2];

        return new Account(userName, password, sex);
    };

    public Optional<Account> attemptLogin(String userName, String password) {
        return accounts.stream()
                .filter(e -> e.getUserName().equals(userName)
                        && e.getPassword().equals(password))
                .findFirst();
    }

    public void saveThreshold(String userName, double threshold) {
        Path path = FileSystems.getDefault().getPath("resources/accounts/" + userName + "/threshold.txt");

        String newThreshold = String.valueOf(threshold);
        System.out.println(threshold);
        System.out.println(path.toString());

        List<String> newThresholdAsLines = Arrays.asList(newThreshold);

        try {
            Files.write(path, newThresholdAsLines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveResult(String userName, char vowel, boolean best, double[] results) {
        Path path;
        if (best) {
            path = FileSystems.getDefault().getPath("resources/accounts/" + userName + "/" + vowel + "_best.txt");
        } else {
            path = FileSystems.getDefault().getPath("resources/accounts/" + userName + "/" + vowel + "_last.txt");
        }

        String newResult = results[0] + ";" + results[1];
        System.out.println(Arrays.toString(results));
        System.out.println(path.toString());

        List<String> newResultAsLines = Arrays.asList(newResult);

        try {
            Files.write(path, newResultAsLines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getThreshold(String userName) {
        Path path = FileSystems.getDefault().getPath("resources/accounts/" + userName + "/threshold.txt");
        try {
            return Files.lines(path)
                    .mapToDouble(Double::parseDouble).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0.0;
    }
}
