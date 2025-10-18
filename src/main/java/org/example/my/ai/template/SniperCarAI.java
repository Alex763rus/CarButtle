package org.example.my.ai.template;

import org.example.my.ai.CarAI;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SniperCarAI implements CarAI {

    private static final double PREFERRED_DISTANCE = 300.0;
    private static final double AIM_THRESHOLD = 5.0;

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
        return 4;
    }

    @Override
    public CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
        double distance = calculateDistance(myCar.getPosition(), opponentCar.getPosition());
        double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
        double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

        // Держим дистанцию
        if (distance < PREFERRED_DISTANCE - 50) {
            return new CarAction(CarAction.ActionType.MOVE_BACKWARD, 0.6);
        } else if (distance > PREFERRED_DISTANCE + 50) {
            if (Math.abs(angleDiff) > 20) {
                return angleDiff > 0 ?
                        new CarAction(CarAction.ActionType.TURN_RIGHT, 0.5) :
                        new CarAction(CarAction.ActionType.TURN_LEFT, 0.5);
            }
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.4);
        }

        // Точное прицеливание и выстрел
        if (Math.abs(angleDiff) < AIM_THRESHOLD) {
            if (myCar.canShoot()) {
                return new CarAction(CarAction.ActionType.SHOOT);
            }
        } else {
            // Медленное точное поворачивание
            double turnPower = Math.min(Math.abs(angleDiff) / 90.0, 0.3);
            if (angleDiff > 0) {
                return new CarAction(CarAction.ActionType.TURN_RIGHT, turnPower);
            } else {
                return new CarAction(CarAction.ActionType.TURN_LEFT, turnPower);
            }
        }

        return new CarAction(CarAction.ActionType.IDLE);
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
        return "Sniper AI";
    }
}