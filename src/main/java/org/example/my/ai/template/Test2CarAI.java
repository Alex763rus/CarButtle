package org.example.my.ai.template;

import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class Test2CarAI extends BaseCarAI {

    @Override
    public int getShootingRange() {
        return 3;
    }

    @Override
    public int getMovementSpeed() {
        return 3;
    }

    @Override
    public int getFireRate() {
        return 3;
    }

    @Override
    protected CarAction decideTankAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
//        if (shouldShoot(myCar, opponentCar, 20)) {
            return shoot();
//        }
//        return moveForward(1.0);
        // Используем готовые методы из BaseCarAI
//        double distance = getDistanceToEnemy(myCar, opponentCar);
//        double angleDiff = getAngleDifference(myCar, opponentCar);
//
//        // Уворачиваемся от пуль
//        if (shouldEvadeBullet(myCar, bullets, 60)) {
//            Bullet nearestBullet = getNearestEnemyBullet(myCar, bullets);
//            return evadeBullet(myCar, nearestBullet);
//        }
//
//
//        // Двигаемся к противнику
//        if (Math.abs(angleDiff) > 15) {
//            return turnToEnemy(angleDiff);
//        }
//
//        return moveForward(0.7);
    }

    @Override
    public String getAIName() {
        return "Simple AI";
    }
}