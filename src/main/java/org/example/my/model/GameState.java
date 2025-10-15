package org.example.my.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public class GameState {
    /**
     * Карта машинок, участвующих в игре
     * Key: ID машинки
     * Value: Объект машинки
     */
    private Map<String, Car> cars = new ConcurrentHashMap<>();

    /**
     * Карта пуль, находящихся на поле
     * Key: ID пули
     * Value: Объект пули
     */
    private Map<String, Bullet> bullets = new ConcurrentHashMap<>();

    /**
     * Флаг, указывающий запущена ли игра
     */
    private boolean gameRunning = false;

    /**
     * Время начала игры в миллисекундах
     */
    private long gameStartTime;

    /**
     * Время окончания игры в миллисекундах
     */
    private long gameEndTime;

    /**
     * Длительность игры в миллисекундах
     */
    private long gameDuration;

    /**
     * ID победившей машинки
     * null - если игра еще не закончена или ничья
     */
    private String winner;

    /**
     * Сообщение о статусе игры (для UI)
     */
    private String statusMessage = "Game not started";

    /**
     * Конфигурация игрового поля
     */
    private GameConfig gameConfig = new GameConfig();

    /**
     * Статистика игры
     */
    private GameStats gameStats = new GameStats();

    /**
     * Конструктор с начальной инициализацией
     */
    public GameState(boolean initializeEmpty) {
        if (initializeEmpty) {
            this.cars = new ConcurrentHashMap<>();
            this.bullets = new ConcurrentHashMap<>();
            this.gameRunning = false;
            this.gameStartTime = 0;
            this.winner = null;
            this.statusMessage = "Ready to start";
        }
    }

    /**
     * Получение количества живых машинок
     */
    public int getAliveCarsCount() {
        return (int) cars.values().stream()
                .filter(Car::isAlive)
                .count();
    }

    /**
     * Получение количества всех пуль на поле
     */
    public int getBulletsCount() {
        return bullets.size();
    }

    /**
     * Проверка, закончена ли игра
     */
    public boolean isGameOver() {
        return !gameRunning && winner != null;
    }

    /**
     * Получение длительности игры в секундах
     */
    public double getGameDurationInSeconds() {
        if (gameRunning) {
            return (System.currentTimeMillis() - gameStartTime) / 1000.0;
        } else if (gameDuration > 0) {
            return gameDuration / 1000.0;
        } else {
            return 0;
        }
    }

    /**
     * Обновление статистики игры
     */
    public void updateStats() {
        if (gameStats == null) {
            gameStats = new GameStats();
        }

        gameStats.setTotalCars(cars.size());
        gameStats.setAliveCars(getAliveCarsCount());
        gameStats.setTotalBullets(getBulletsCount());
        gameStats.setGameDuration(getGameDurationInSeconds());

        // Обновляем статистику по машинкам
        cars.forEach((id, car) -> {
            CarStats carStats = gameStats.getCarStats().computeIfAbsent(id, k -> new CarStats());
            carStats.setHealth(car.getHealth());
            carStats.setAmmo(car.getAmmo());
            carStats.setAlive(car.isAlive());
            carStats.setPosition(car.getPosition());
        });
    }

    /**
     * Внутренний класс для конфигурации игры
     */
    @Data
    @NoArgsConstructor
    public static class GameConfig {
        private int fieldWidth = 800;
        private int fieldHeight = 600;
        private int maxGameTimeSeconds = 300; // 5 минут
        private boolean friendlyFire = false;
        private int initialHealth = 100;
        private int initialAmmo = 50;
        private double bulletDamage = 25.0;

        public GameConfig(int fieldWidth, int fieldHeight) {
            this.fieldWidth = fieldWidth;
            this.fieldHeight = fieldHeight;
        }
    }

    /**
     * Внутренний класс для статистики игры
     */
    @Data
    @NoArgsConstructor
    public static class GameStats {
        private int totalCars;
        private int aliveCars;
        private int totalBullets;
        private double gameDuration;
        private Map<String, CarStats> carStats = new ConcurrentHashMap<>();

        /**
         * Получение статистики для конкретной машинки
         */
        public CarStats getCarStats(String carId) {
            return carStats.get(carId);
        }

        /**
         * Получение ID победителя на основе статистики
         */
        public String getWinnerByStats() {
            return carStats.entrySet().stream()
                    .filter(entry -> entry.getValue().isAlive())
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Внутренний класс для статистики машинки
     */
    @Data
    @NoArgsConstructor
    public static class CarStats {
        private int health;
        private int ammo;
        private boolean isAlive;
        private Position position;
        private int shotsFired;
        private int hits;
        private int damageDealt;
        private int damageReceived;
        private double accuracy; // точность стрельбы

        /**
         * Расчет точности стрельбы
         */
        public double calculateAccuracy() {
            if (shotsFired == 0) return 0.0;
            return (double) hits / shotsFired * 100.0;
        }

        /**
         * Обновление статистики при выстреле
         */
        public void incrementShotsFired() {
            this.shotsFired++;
            this.accuracy = calculateAccuracy();
        }

        /**
         * Обновление статистики при попадании
         */
        public void incrementHits(int damage) {
            this.hits++;
            this.damageDealt += damage;
            this.accuracy = calculateAccuracy();
        }

        /**
         * Обновление статистики при получении урона
         */
        public void takeDamage(int damage) {
            this.damageReceived += damage;
        }
    }
}