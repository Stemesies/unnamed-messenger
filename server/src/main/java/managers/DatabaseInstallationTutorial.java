package managers;

import utils.Ansi;

import java.util.Scanner;

import static managers.DatabaseManager.PASSWORD;
import static managers.DatabaseManager.PORT;
import static managers.DatabaseManager.USERNAME;

public class DatabaseInstallationTutorial {

    static Scanner scanner = new Scanner(System.in);

    private static boolean userDeclined() {
        return !scanner.nextLine().equalsIgnoreCase("y");
    }

    private static void await() {
        System.out.println(
            Ansi.applyStyle("Введите что угодно для продолжения...",
                Ansi.Colors.Bright.BLACK)
        );
        scanner.nextLine();
    }

    public static void start() {
        System.out.println("Кажется у вас не установлена база данных.");
        System.out.println("Данный туториал поможет вам ее установить.");
        System.out.println("Продолжаем? (Y/n)");
        if (userDeclined()) {
            System.out.println(Ansi.applyStyle("Выходим...", Ansi.Colors.RED));
            System.exit(-1);
        }
        Ansi.clearLine();
        Ansi.clearLine();
        System.out.println("[1] Первым делом скачиваем отсюда pgAdmin4 и устанавливаем его.");
        System.out.println("https://ftp.postgresql.org/pub/pgadmin/pgadmin4/v9.11/windows/pgadmin4-9.11-x64.exe");
        await();
        System.out.println("[2] Далее скачиваем базу данных и тоже устанавливаем");
        System.out.println("https://repo.postgrespro.ru/win/64/PostgreSQL_18.1_64bit_Setup.exe");
        System.out.println("[3] Выберите все компоненты программы.");
        System.out.println("[4] В ходе установки вас попросят задать параметры сервера.");
        System.out.println("    Продолжите этот гайд на этом моменте.");
        await();
        System.out.println("[5] Убедитесь, что указан порт \"" + PORT + "\"");
        System.out.println("    Пароль выберите любой. Повторите в поле ниже. (И запомните его)");
        System.out.println("    Больше ничего менять не следует.");
        System.out.println("[6] Дождитесь конца установки");
        await();
        System.out.println("[7] Запустите pgAdmin4.");
        await();
        System.out.println("[8] В открывшемся окне находим боковое меню \"Object Explorer\"");
        System.out.println("    Снизу также должна быть надпись \"Servers (1)\"");
        System.out.println("    Если ничего нет, убедитесь, что слева выбрана вкладка \"Default Workspace\"");
        System.out.println("    (Кнопка под надписью \"File\")");
        await();
        System.out.println("[9] Открываем \"Servers\" и выбираем там \"PostgreSQL 18 (64bit)\"");
        System.out.println("    Если его нет, попробуйте перезагрузить компьютер.");
        System.out.println("    В открывшемся окне введите пароль, что вы вводили при установке.");
        await();
        System.out.println("[10] В дереве сервера найдите запись \"Login/Group Roles (17)\"");
        System.out.println("     Нажмите ПКМ, Create, Login/Group Role");
        await();
        System.out.println("[11] В окне, во вкладке General напишите имя \"" + USERNAME + "\"");
        System.out.println("     Во вкладке Definition в поле \"Password\" напишите \"" + PASSWORD + "\"");
        System.out.println("     Во вкладке Privileges включите \"Can login?\"");
        System.out.println("     Сохраните изменения.");
        await();
        System.out.println("[12] В дереве сервера найдите запись \"Databases (1)\"");
        System.out.println("     Нажмите ПКМ, Create, Database");
        await();
        System.out.println("[13] В окне во вкладке General напишите в поле \"Database\" \"" + USERNAME + "\"");
        System.out.println("     В качестве владельца выберите \"" +  USERNAME + "\"");
        System.out.println("     Сохраните изменения.");
        await();
        System.out.println("[14] Попробуйте заново запустить ServerMain.");
        System.out.println("     Желаю удачи. ovo");
    }

}
