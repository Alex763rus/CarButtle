// UserCarAI.java - безопасный шаблон
package org.example.my.ai.template;

import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;

import java.util.Collection;

public class UserCarAI implements org.example.my.ai.CarAI {

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
        // Всегда проверяем на null и валидность
        if (myCar == null || !myCar.isAlive() || opponentCar == null || !opponentCar.isAlive()) {
            return new CarAction(CarAction.ActionType.IDLE);
        }

        try {
            // ВАШ КОД ЗДЕСЬ
            // Пример: всегда двигаться вперед
            return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.5);

        } catch (Exception e) {
            // В случае ошибки возвращаем безопасное действие
            return new CarAction(CarAction.ActionType.IDLE);
        }
    }

    @Override
    public String getAIName() {
        return "My Custom AI"; // Всегда возвращаем строку, никогда null!
    }
}