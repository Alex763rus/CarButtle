package org.example.my.service;

import org.example.my.ai.CarAI;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.stereotype.Component;

import java.util.Collection;

// SimpleCarAI.java
@Component
public class SimpleCarAI implements CarAI {

    @Override
    public CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        if (!myCar.isAlive()) {
            return new CarAction(CarAction.ActionType.IDLE, 0);
        }

        // Простая логика: едем к противнику и стреляем
        double distance = calculateDistance(myCar.getPosition(), opponentCar.getPosition());

        if (distance < 200 && myCar.canShoot()) {
            return new CarAction(CarAction.ActionType.SHOOT, 1.0);
        }

        // Поворачиваем в сторону противника
        double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
        double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

        if (Math.abs(angleDiff) > 10) {
            if (angleDiff > 0) {
                return new CarAction(CarAction.ActionType.TURN_RIGHT, 0.7);
            } else {
                return new CarAction(CarAction.ActionType.TURN_LEFT, 0.7);
            }
        }

        // Двигаемся вперед
        return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.8);
    }

    private double calculateDistance(Position p1, Position p2) {
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

    private double calculateAngleToTarget(Position from, Position to) {
        return Math.toDegrees(Math.atan2(to.getY() - from.getY(), to.getX() - from.getX()));
    }

    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    @Override
    public String getAIName() {
        return "SimpleAI";
    }
}