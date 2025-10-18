package org.example.my.ai.template;

import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AggressiveCarAI extends BaseCarAI {

    @Override
    public int getShootingRange() {
        return 5; // Короткая дистанция
    }

    @Override
    public int getMovementSpeed() {
        return 1; // Максимальная скорость
    }

    @Override
    public int getFireRate() {
        return 4; // Средняя скорострельность
    }

    @Override
    protected CarAction decideTankAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        if (!myCar.isAlive() || opponentCar == null || !opponentCar.isAlive()) {
            return idle();
        }

        // Агрессивная тактика - всегда двигаться к противнику и стрелять
        double distance = getDistanceToEnemy(myCar, opponentCar);

        // Всегда стреляем если можем
        if (myCar.canShoot() && isAimingAtEnemy(myCar, opponentCar, 30)) {
            return shoot();
        }

        // Быстро сближаемся
        return moveToEnemy(myCar, opponentCar);
    }

    @Override
    public String getAIName() {
        return "Aggressive AI";
    }
}