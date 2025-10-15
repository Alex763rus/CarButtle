package org.example.my.ai.template;

import org.example.my.ai.CarAI;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AggressiveCarAI implements CarAI {

    private static final double SHOOTING_DISTANCE = 250.0;
    private static final double CLOSE_DISTANCE = 80.0;

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

        double distance = calculateDistance(myCar.getPosition(), opponentCar.getPosition());
        double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
        double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

        // Всегда стреляем если можем и противник в зоне поражения
        if (myCar.canShoot() && Math.abs(angleDiff) < 30 && distance < SHOOTING_DISTANCE) {
            return new CarAction(CarAction.ActionType.SHOOT);
        }

        // Быстро сближаемся с противником
        if (distance > CLOSE_DISTANCE) {
            if (Math.abs(angleDiff) > 10) {
                return angleDiff > 0 ?
                        new CarAction(CarAction.ActionType.TURN_RIGHT, 1.0) :
                        new CarAction(CarAction.ActionType.TURN_LEFT, 1.0);
            }
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 1.0);
        } else {
            // На близкой дистанции делаем резкие маневры
            if (Math.abs(angleDiff) > 45) {
                return angleDiff > 0 ?
                        new CarAction(CarAction.ActionType.TURN_RIGHT, 0.5) :
                        new CarAction(CarAction.ActionType.TURN_LEFT, 0.5);
            }
            return new CarAction(CarAction.ActionType.MOVE_BACKWARD, 0.7);
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
        return "Aggressive AI";
    }
}