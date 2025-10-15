package org.example.my.ai;


import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SimpleCarAI implements CarAI {

    private static final double SAFE_DISTANCE = 50.0;

    @Override
    public CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        if (!myCar.isAlive() || opponentCar == null || !opponentCar.isAlive()) {
            return new CarAction(CarAction.ActionType.IDLE);
        }

        // Расчет расстояния до противника
        double distanceToOpponent = calculateDistance(myCar.getPosition(), opponentCar.getPosition());

        // Проверяем риск столкновения
        if (distanceToOpponent < SAFE_DISTANCE) {
            // Слишком близко - отступаем
            return evadeCollision(myCar, opponentCar);
        }

        // Если близко и можно стрелять - стреляем
        if (distanceToOpponent < 200 && myCar.canShoot()) {
            return new CarAction(CarAction.ActionType.SHOOT);
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

        // Двигаемся вперед к противнику
        return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.7);
    }

    /**
     * Действие для избегания столкновения
     */
    private CarAction evadeCollision(Car myCar, Car opponentCar) {
        double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
        double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

        // Двигаемся в противоположную сторону от противника
        if (Math.abs(angleDiff) < 90) {
            return new CarAction(CarAction.ActionType.MOVE_BACKWARD, 0.8);
        } else {
            // Резко поворачиваем чтобы обойти
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