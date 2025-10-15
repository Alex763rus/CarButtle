package org.example.my.ai;


import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AdvancedCarAI implements CarAI {

    private static final double SAFE_DISTANCE = 150;
    private static final double ATTACK_DISTANCE = 250;

    @Override
    public CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        if (!myCar.isAlive() || opponentCar == null || !opponentCar.isAlive()) {
            return new CarAction(CarAction.ActionType.IDLE);
        }

        double distance = calculateDistance(myCar.getPosition(), opponentCar.getPosition());
        double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
        double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

        // Избегание пуль
        Bullet nearestBullet = findNearestBullet(myCar, bullets);
        if (nearestBullet != null) {
            double bulletDistance = calculateDistance(myCar.getPosition(), nearestBullet.getPosition());
            if (bulletDistance < 100) {
                // Уклоняемся от пули
                return evadeBullet(myCar, nearestBullet);
            }
        }

        // Тактика в зависимости от здоровья
        if (myCar.getHealth() < 30) {
            // Мало здоровья - отступаем и стреляем издалека
            return defensiveBehavior(myCar, opponentCar, distance, angleDiff);
        } else if (myCar.getHealth() > 70 && distance > SAFE_DISTANCE) {
            // Много здоровья - агрессивная тактика
            return aggressiveBehavior(myCar, opponentCar, distance, angleDiff);
        } else {
            // Нейтральная тактика
            return neutralBehavior(myCar, opponentCar, distance, angleDiff);
        }
    }

    private CarAction evadeBullet(Car myCar, Bullet bullet) {
        double angleToBullet = calculateAngleToTarget(myCar.getPosition(), bullet.getPosition());
        double angleDiff = normalizeAngle(angleToBullet - myCar.getPosition().getAngle());

        // Двигаемся в противоположную сторону от пули
        if (Math.abs(angleDiff) < 90) {
            return new CarAction(CarAction.ActionType.MOVE_BACKWARD, 0.9);
        } else {
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.7);
        }
    }

    private CarAction defensiveBehavior(Car myCar, Car opponent, double distance, double angleDiff) {
        // Держим дистанцию и стреляем
        if (distance > ATTACK_DISTANCE && Math.abs(angleDiff) < 30 && myCar.canShoot()) {
            return new CarAction(CarAction.ActionType.SHOOT);
        }

        if (distance < SAFE_DISTANCE) {
            // Слишком близко - отступаем
            return new CarAction(CarAction.ActionType.MOVE_BACKWARD, 0.8);
        } else {
            // Держим дистанцию и поворачиваем к противнику
            if (Math.abs(angleDiff) > 20) {
                return turnTowardsTarget(angleDiff);
            }
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.5);
        }
    }

    private CarAction aggressiveBehavior(Car myCar, Car opponent, double distance, double angleDiff) {
        // Атакуем активно
        if (Math.abs(angleDiff) < 20 && myCar.canShoot()) {
            return new CarAction(CarAction.ActionType.SHOOT);
        }

        if (distance > SAFE_DISTANCE) {
            // Подходим ближе
            if (Math.abs(angleDiff) > 15) {
                return turnTowardsTarget(angleDiff);
            }
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.9);
        } else {
            // Близко - стреляем и маневрируем
            if (myCar.canShoot()) {
                return new CarAction(CarAction.ActionType.SHOOT);
            }
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.3);
        }
    }

    private CarAction neutralBehavior(Car myCar, Car opponent, double distance, double angleDiff) {
        // Баланс между атакой и защитой
        if (Math.abs(angleDiff) < 25 && myCar.canShoot() && distance < ATTACK_DISTANCE) {
            return new CarAction(CarAction.ActionType.SHOOT);
        }

        if (distance > ATTACK_DISTANCE) {
            // Подходим ближе
            if (Math.abs(angleDiff) > 20) {
                return turnTowardsTarget(angleDiff);
            }
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.7);
        } else if (distance < SAFE_DISTANCE) {
            // Слишком близко - отходим
            return new CarAction(CarAction.ActionType.MOVE_BACKWARD, 0.6);
        } else {
            // Маневрируем
            if (Math.abs(angleDiff) > 10) {
                return turnTowardsTarget(angleDiff);
            }
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.4);
        }
    }

    private CarAction turnTowardsTarget(double angleDiff) {
        if (angleDiff > 0) {
            return new CarAction(CarAction.ActionType.TURN_RIGHT, 0.7);
        } else {
            return new CarAction(CarAction.ActionType.TURN_LEFT, 0.7);
        }
    }

    private Bullet findNearestBullet(Car myCar, Collection<Bullet> bullets) {
        Bullet nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Bullet bullet : bullets) {
            // Игнорируем свои пули
            if (bullet.getOwnerId().equals(myCar.getId())) continue;

            double distance = calculateDistance(myCar.getPosition(), bullet.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = bullet;
            }
        }

        return nearest;
    }

    private double calculateDistance(Position p1, Position p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private double calculateAngleToTarget(Position from, Position to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    @Override
    public String getAIName() {
        return "Advanced AI";
    }
}