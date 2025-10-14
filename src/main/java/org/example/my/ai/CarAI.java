package org.example.my.ai;

import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;

import java.util.Collection;

public interface CarAI {
    /**
     * Основной метод ИИ, вызывается каждый игровой тик
     * @param myCar наша машинка
     * @param opponentCar машинка противника
     * @param bullets список всех пуль на поле
     * @return действие, которое должна выполнить машинка
     */
    CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets);

    String getAIName();
}