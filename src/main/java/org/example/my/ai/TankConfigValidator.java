package org.example.my.ai;

import org.example.my.model.Car;

/**
 * Утилитарный класс для работы с характеристиками танков
 */
public class TankConfigValidator {

    private static final int MAX_POINTS = 10;
    private static final int MIN_STAT = 1;
    private static final int MAX_STAT = 5;

    /**
     * Проверяет валидность конфигурации характеристик
     */
    public static void validateConfiguration(CarAI ai) {
        int shootingRange = ai.getShootingRange();
        int movementSpeed = ai.getMovementSpeed();
        int fireRate = ai.getFireRate();

        // Проверяем границы
        if (shootingRange < MIN_STAT || shootingRange > MAX_STAT ||
                movementSpeed < MIN_STAT || movementSpeed > MAX_STAT ||
                fireRate < MIN_STAT || fireRate > MAX_STAT) {
            throw new IllegalStateException(
                    String.format("Invalid tank stats for AI '%s'! Stats must be between %d and %d. Got: Range=%d, Speed=%d, FireRate=%d",
                            ai.getAIName(), MIN_STAT, MAX_STAT, shootingRange, movementSpeed, fireRate));
        }

        // Проверяем сумму очков
        int totalPoints = shootingRange + movementSpeed + fireRate;
        if (totalPoints > MAX_POINTS) {
            throw new IllegalStateException(
                    String.format("Too many points allocated for AI '%s'! Max %d, got %d (Range=%d, Speed=%d, FireRate=%d)",
                            ai.getAIName(), MAX_POINTS, totalPoints, shootingRange, movementSpeed, fireRate));
        }

        System.out.printf("✅ Tank AI '%s' validated: Range=%d, Speed=%d, FireRate=%d (%d/%d points)%n",
                ai.getAIName(), shootingRange, movementSpeed, fireRate, totalPoints, MAX_POINTS);
    }

    /**
     * Применяет характеристики к танку
     */
    public static void applyTankStats(CarAI ai, Car car) {
        // Скорость движения: 3.0 - 9.0
        car.setMaxSpeed(3.0 + (ai.getMovementSpeed() - 1) * 1.5);
        car.setAcceleration(0.15 + (ai.getMovementSpeed() - 1) * 0.1);

        // Дальность выстрела (время жизни пули): 2000 - 5000 ms
        car.setBulletLifetime(2000 + (ai.getShootingRange() - 1) * 750);

        // Скорострельность (кулдаун): 1000 - 200 ms
        car.setShootCooldown(1000 - (ai.getFireRate() - 1) * 200);

        System.out.printf("⚙️ Tank '%s' stats applied: Speed=%.1f, Range=%dms, FireRate=%dms%n",
                ai.getAIName(), car.getMaxSpeed(), car.getBulletLifetime(), car.getShootCooldown());
    }

    /**
     * Получает информацию о конфигурации
     */
    public static String getConfigurationInfo(CarAI ai) {
        int totalPoints = ai.getShootingRange() + ai.getMovementSpeed() + ai.getFireRate();
        return String.format("Range: %d/5, Speed: %d/5, FireRate: %d/5 (%d/10 points)",
                ai.getShootingRange(), ai.getMovementSpeed(), ai.getFireRate(), totalPoints);
    }
}