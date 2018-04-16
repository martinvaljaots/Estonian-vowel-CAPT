package vowelcapt.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class AccountUtils {

    private List<Account> accounts = new ArrayList<>();

    public AccountUtils() {
        initializeAccountsList();
    }

    private void initializeAccountsList() {
        Path path = FileSystems.getDefault().getPath("resources/accounts/acc.csv");
        Charset charset = Charset.forName("ISO-8859-1");
        try {
            accounts = Files.lines(path, charset)
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

        List<String> newThresholdAsLines = Collections.singletonList(newThreshold);

        try {
            Files.write(path, newThresholdAsLines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveToLog(String userName, List<String> logMessages) {
        Path path = FileSystems.getDefault().getPath("resources/accounts/" + userName + "/log.txt");

        try {
            Files.write(path, logMessages, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
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

    public boolean accountExists(String userName) {
        return accounts.stream()
                .anyMatch(e -> e.getUserName().equals(userName));
    }
}
