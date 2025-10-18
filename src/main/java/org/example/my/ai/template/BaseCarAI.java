package org.example.my.ai.template;

import org.example.my.ai.CarAI;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;

import java.util.Collection;

/**
 * Базовый класс для всех AI с готовыми методами
 * Все боты должны наследоваться от этого класса
 */
public abstract class BaseCarAI implements CarAI {

    // === АБСТРАКТНЫЕ МЕТОДЫ ДЛЯ РЕАЛИЗАЦИИ ===

    /**
     * Основная логика AI - должен быть реализован в дочернем классе
     */
    protected abstract CarAction decideTankAction(Car myCar, Car opponentCar, Collection<Bullet> bullets);

    // === РЕАЛИЗАЦИЯ ИНТЕРФЕЙСА CarAI ===

    @Override
    public final CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        // Можно добавить общую логику здесь (например, применение характеристик)
        return decideTankAction(myCar, opponentCar, bullets);
    }

    // === ГОТОВЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    protected final double getDistanceToEnemy(Car myCar, Car enemyCar) {
        Position myPos = myCar.getPosition();
        Position enemyPos = enemyCar.getPosition();
        return Math.sqrt(
                Math.pow(myPos.getX() - enemyPos.getX(), 2) +
                        Math.pow(myPos.getY() - enemyPos.getY(), 2)
        );
    }

    protected final double getAngleToEnemy(Car myCar, Car enemyCar) {
        Position myPos = myCar.getPosition();
        Position enemyPos = enemyCar.getPosition();
        double dx = enemyPos.getX() - myPos.getX();
        double dy = enemyPos.getY() - myPos.getY();
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    protected final double getAngleDifference(Car myCar, Car enemyCar) {
        return normalizeAngle(getAngleToEnemy(myCar, enemyCar) - myCar.getPosition().getAngle());
    }

    protected final boolean isAimingAtEnemy(Car myCar, Car enemyCar, double tolerance) {
        return Math.abs(getAngleDifference(myCar, enemyCar)) < tolerance;
    }

// В класс BaseCarAI добавьте эти методы если их нет:

    protected final Bullet getNearestEnemyBullet(Car myCar, Collection<Bullet> bullets) {
        Bullet nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Bullet bullet : bullets) {
            // ИСПОЛЬЗУЕМ isActive() вместо getActive()
            if (bullet.getOwner() != myCar && bullet.isActive()) {
                double distance = getDistanceToBullet(myCar, bullet);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = bullet;
                }
            }
        }
        return nearest;
    }

    protected final boolean shouldEvadeBullet(Car myCar, Collection<Bullet> bullets, double safeDistance) {
        Bullet nearestBullet = getNearestEnemyBullet(myCar, bullets);
        return nearestBullet != null && getDistanceToBullet(myCar, nearestBullet) < safeDistance;
    }

    protected final boolean shouldShoot(Car myCar, Car enemyCar, double aimTolerance) {
        double distance = getDistanceToEnemy(myCar, enemyCar);
        double optimalDistance = getOptimalShootingDistance();
        // ИСПОЛЬЗУЕМ isAlive() вместо getAlive() и canShoot()
        return distance < optimalDistance &&
                myCar.isAlive() && enemyCar.isAlive() &&
                myCar.canShoot() &&
                isAimingAtEnemy(myCar, enemyCar, aimTolerance);
    }

    protected final double getDistanceToPosition(Position pos, double x, double y) {
        return Math.sqrt(Math.pow(pos.getX() - x, 2) + Math.pow(pos.getY() - y, 2));
    }

    protected final double getDistanceToBullet(Car myCar, Bullet bullet) {
        return getDistanceToPosition(myCar.getPosition(), bullet.getX(), bullet.getY());
    }

    protected final double getOptimalShootingDistance() {
        return 150 + (getShootingRange() - 1) * 50;
    }

    protected final double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    // === ГОТОВЫЕ ТАКТИЧЕСКИЕ МЕТОДЫ ===
    protected final CarAction evadeBullet(Car myCar, Bullet bullet) {
        double angleToBullet = getAngleToPosition(myCar.getPosition(), bullet.getX(), bullet.getY());
        double angleDiff = normalizeAngle(angleToBullet - myCar.getPosition().getAngle());

        // Уворачиваемся в противоположную сторону
        if (angleDiff > 0) {
            return turnLeft(1.0);
        } else {
            return turnRight(1.0);
        }
    }

    protected final CarAction moveToEnemy(Car myCar, Car enemyCar) {
        double angleDiff = getAngleDifference(myCar, enemyCar);

        if (Math.abs(angleDiff) > 30) {
            return turnToEnemy(angleDiff);
        }
        return moveForward(0.8);
    }

    protected final CarAction turnToEnemy(double angleDiff) {
        if (angleDiff > 0) {
            return turnRight(0.9);
        } else {
            return turnLeft(0.9);
        }
    }

    // === БАЗОВЫЕ ДЕЙСТВИЯ ===

    protected final CarAction moveForward(double power) {
        return new CarAction(CarAction.ActionType.MOVE_FORWARD, Math.min(power, 1.0));
    }

    protected final CarAction moveBackward(double power) {
        return new CarAction(CarAction.ActionType.MOVE_BACKWARD, Math.min(power, 1.0));
    }

    protected final CarAction turnLeft(double power) {
        return new CarAction(CarAction.ActionType.TURN_LEFT, Math.min(power, 1.0));
    }

    protected final CarAction turnRight(double power) {
        return new CarAction(CarAction.ActionType.TURN_RIGHT, Math.min(power, 1.0));
    }

    protected final CarAction shoot() {
        return new CarAction(CarAction.ActionType.SHOOT);
    }

    protected final CarAction idle() {
        return new CarAction(CarAction.ActionType.IDLE);
    }

    // === СЛУЖЕБНЫЕ МЕТОДЫ ===

    private double getAngleToPosition(Position from, double toX, double toY) {
        double dx = toX - from.getX();
        double dy = toY - from.getY();
        return Math.toDegrees(Math.atan2(dy, dx));
    }
}