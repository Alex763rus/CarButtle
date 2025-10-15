package org.example.my.ai.template;

import org.example.my.ai.CarAI;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class DefensiveCarAI implements CarAI {

    private static final double OPTIMAL_DISTANCE = 150.0;
    private static final double DANGER_DISTANCE = 100.0;
    private static final double BULLET_SAFE_DISTANCE = 120.0;

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
        Bullet nearestBullet = findNearestBullet(myCar, bullets);
        if (nearestBullet != null) {
            double bulletDistance = calculateDistance(myCar.getPosition(), nearestBullet.getPosition());
            if (bulletDistance < BULLET_SAFE_DISTANCE) {
                return evadeBullet(myCar, nearestBullet);
            }
        }

        double distance = calculateDistance(myCar.getPosition(), opponentCar.getPosition());
        double angleToOpponent = calculateAngleToTarget(myCar.getPosition(), opponentCar.getPosition());
        double angleDiff = normalizeAngle(angleToOpponent - myCar.getPosition().getAngle());

        // Держим оптимальную дистанцию
        if (distance < DANGER_DISTANCE) {
            // Слишком близко - отступаем
            return new CarAction(CarAction.ActionType.MOVE_BACKWARD, 0.8);
        } else if (distance > OPTIMAL_DISTANCE + 50) {
            // Слишком далеко - приближаемся
            if (Math.abs(angleDiff) > 15) {
                return angleDiff > 0 ?
                        new CarAction(CarAction.ActionType.TURN_RIGHT, 0.6) :
                        new CarAction(CarAction.ActionType.TURN_LEFT, 0.6);
            }
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.5);
        } else {
            // На оптимальной дистанции - стреляем и маневрируем
            if (myCar.canShoot() && Math.abs(angleDiff) < 20) {
                return new CarAction(CarAction.ActionType.SHOOT);
            }
            // Легкое маневрирование
            return new CarAction(CarAction.ActionType.TURN_RIGHT, 0.3);
        }
    }

    private CarAction evadeBullet(Car myCar, Bullet bullet) {
        double angleToBullet = calculateAngleToTarget(myCar.getPosition(), bullet.getPosition());
        double angleDiff = normalizeAngle(angleToBullet - myCar.getPosition().getAngle());

        // Уворачиваемся в противоположную сторону
        if (angleDiff > 0) {
            return new CarAction(CarAction.ActionType.TURN_LEFT, 1.0);
        } else {
            return new CarAction(CarAction.ActionType.TURN_RIGHT, 1.0);
        }
    }

    private Bullet findNearestBullet(Car myCar, Collection<Bullet> bullets) {
        if (bullets == null || bullets.isEmpty()) return null;

        Bullet nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Bullet bullet : bullets) {
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
        return "Defensive AI";
    }
}