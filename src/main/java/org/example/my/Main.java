package org.example.my;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.Collections;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        },
        scanBasePackages = "org.example"
)
@EnableScheduling
@EnableAsync
public class Main {

    public static void main(String[] args) {
        // Простой запуск
        // SpringApplication.run(CarBattleGameApplication.class, args);

        // Расширенный запуск с конфигурацией
        SpringApplication app = new SpringApplication(Main.class);

        // Настройки для продакшена
        app.setBannerMode(Banner.Mode.CONSOLE);
        app.setLogStartupInfo(true);
        app.setRegisterShutdownHook(true);

        // Устанавливаем свойства по умолчанию
        app.setDefaultProperties(Collections.singletonMap(
                "spring.profiles.default", "dev"
        ));

        // Запускаем приложение
        app.run(args);
    }
}