package vowelcapt.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
}
