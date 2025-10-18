package org.example.my.ai;

import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;

import java.util.Collection;

/**
 * Основной интерфейс для всех AI с системой характеристик
 */
public interface CarAI {

    // === МЕТОДЫ КОНФИГУРАЦИИ ===

    /**
     * Дальность выстрела (1-5 очков)
     * Влияет на дальность и скорость пуль
     */
    int getShootingRange();

    /**
     * Скорость перемещения (1-5 очков)
     * Влияет на максимальную скорость и ускорение
     */
    int getMovementSpeed();

    /**
     * Скорострельность (1-5 очков)
     * Влияет на время перезарядки
     */
    int getFireRate();

    // === ОСНОВНЫЕ МЕТОДЫ ===

    /**
     * Основной метод принятия решений
     */
    CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets);

    /**
     * Получение имени AI
     */
    String getAIName();

}