package org.example.my.ai.template;

import org.example.my.ai.CarAI;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SimpleCarAI implements CarAI {

    private static final double SAFE_DISTANCE = 60.0; // Увеличили безопасную дистанцию
    private static final double BULLET_DANGER_DISTANCE = 60.0;

    @Override
    public CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        // Если наш танк мертв - ничего не делаем
        if (!myCar.isAlive()) {
            return new CarAction(CarAction.ActionType.IDLE);
        }

        // Если противник мертв - ищем другую цель или останавливаемся
        if (opponentCar == null || !opponentCar.isAlive()) {
            return new CarAction(CarAction.ActionType.IDLE);
        }
        if (!myCar.isAlive() || opponentCar == null || !opponentCar.isAlive()) {
            return new CarAction(CarAction.ActionType.IDLE);
        }

        // Проверяем пули в первую очередь
        for (Bullet bullet : bullets) {
            if (bullet.getOwner() == myCar) continue;

            double distance = calculateDistance(myCar.getPosition(), bullet.getPosition());
            if (distance < BULLET_DANGER_DISTANCE) {
                return evadeBullet(myCar, bullet);
            }
        }

        // Расчет расстояния до противника
        double distanceToOpponent = calculateDistance(myCar.getPosition(), opponentCar.getPosition());

        // Проверяем риск столкновения - увеличили дистанцию
        if (distanceToOpponent < SAFE_DISTANCE) {
            // Слишком близко - отступаем и пытаемся обойти
            return evadeCollision(myCar, opponentCar);
        }

// Если на средней дистанции и можно стрелять - стреляем
        if (distanceToOpponent < 300 && myCar.canShoot()) {
            double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
            double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

            // Стреляем если достаточно хорошо прицелились
            if (Math.abs(angleDiff) < 25) {
                return new CarAction(CarAction.ActionType.SHOOT);
            }
        }
        // Расчет угла до противника
        double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
        double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

        // Поворачиваем в сторону противника
        if (Math.abs(angleDiff) > 15) {
            if (angleDiff > 0) {
                return new CarAction(CarAction.ActionType.TURN_RIGHT, 0.8);
            } else {
                return new CarAction(CarAction.ActionType.TURN_LEFT, 0.8);
            }
        }

        // Двигаемся вперед к противнику, но медленнее при приближении
        double movePower = distanceToOpponent > 150 ? 0.8 : 0.4;
        return new CarAction(CarAction.ActionType.MOVE_FORWARD, movePower);
    }

    private CarAction evadeBullet(Car myCar, Bullet bullet) {
        double angleToBullet = calculateAngleToTarget(myCar.getPosition(), bullet.getPosition());
        double angleDiff = normalizeAngle(angleToBullet - myCar.getPosition().getAngle());

        // Быстрое уклонение - резкий поворот и отступление
        if (angleDiff > 0) {
            return new CarAction(CarAction.ActionType.TURN_LEFT, 1.0);
        } else {
            return new CarAction(CarAction.ActionType.TURN_RIGHT, 1.0);
        }
    }

    private CarAction evadeCollision(Car myCar, Car opponentCar) {
        double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
        double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

        // Если противник прямо перед нами - отступаем
        if (Math.abs(angleDiff) < 60) {
            return new CarAction(CarAction.ActionType.MOVE_BACKWARD, 0.8);
        } else {
            // Если сбоку - поворачиваем чтобы обойти
            if (angleDiff > 0) {
                return new CarAction(CarAction.ActionType.TURN_LEFT, 1.0);
            } else {
                return new CarAction(CarAction.ActionType.TURN_RIGHT, 1.0);
            }
        }
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
        return "Simple AI";
    }
}