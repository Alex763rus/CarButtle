package org.example.my.ai.template;

import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;

import java.util.Collection;

/**
 * ШАБЛОН для создания своего AI
 * Наследуйтесь от BaseCarAI и используйте готовые методы!
 */
public class UserCarAI extends BaseCarAI {

    // === ХАРАКТЕРИСТИКИ ===

    @Override
    public int getShootingRange() {
        return 4; // 1-5 очков
    }

    @Override
    public int getMovementSpeed() {
        return 3; // 1-5 очков
    }

    @Override
    public int getFireRate() {
        return 3; // 1-5 очков
    }

    // === ВАША ТАКТИКА ===

    @Override
    protected CarAction decideTankAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        try {
            // Используем готовые методы из BaseCarAI!
            double distance = getDistanceToEnemy(myCar, opponentCar);
            double angleDiff = getAngleDifference(myCar, opponentCar);

            // Ваша логика здесь...
            if (shouldEvadeBullet(myCar, bullets, 50)) {
                Bullet nearestBullet = getNearestEnemyBullet(myCar, bullets);
                return evadeBullet(myCar, nearestBullet);
            }

            if (shouldShoot(myCar, opponentCar, 25)) {
                return shoot();
            }

            if (distance > getOptimalShootingDistance()) {
                return moveToEnemy(myCar, opponentCar);
            }

            return maneuver();

        } catch (Exception e) {
            return idle();
        }
    }

    // Ваш собственный метод
    private CarAction maneuver() {
        // Случайное маневрирование
        double rand = Math.random();
        if (rand < 0.3) {
            return turnLeft(0.4);
        } else if (rand < 0.6) {
            return turnRight(0.4);
        } else {
            return moveForward(0.3);
        }
    }

    @Override
    public String getAIName() {
        return "My Custom Tank";
    }
}