package org.example.my.service;

import org.example.my.model.*;
import org.example.my.ai.CarAI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class GameService {

    @Autowired
    private ApplicationContext applicationContext;

    private GameState gameState = new GameState();
    private final Map<String, CarAI> aiInstances = new ConcurrentHashMap<>();
    private ScheduledExecutorService gameLoop;

    private static final double CAR_SPEED = 3.0;
    private static final double TURN_SPEED = 4.0;
    private static final double BULLET_SPEED = 8.0;
    private static final int FIELD_WIDTH = 800;
    private static final int FIELD_HEIGHT = 600;
    private static final double CAR_RADIUS = 20.0;
    private static final double MIN_DISTANCE_BETWEEN_CARS = CAR_RADIUS * 2.5;

    /**
     * Запуск новой игры
     */
    public void startGame(String aiClass1, String aiClass2) {
        if (aiClass1 == null || aiClass1.trim().isEmpty()) {
            throw new IllegalArgumentException("AI class 1 cannot be null or empty");
        }
        if (aiClass2 == null || aiClass2.trim().isEmpty()) {
            throw new IllegalArgumentException("AI class 2 cannot be null or empty");
        }

        System.out.println("Starting game with AI1: " + aiClass1 + ", AI2: " + aiClass2);

        resetGame();

        Car car1 = new Car("car1", 100, 100, aiClass1);
        Car car2 = new Car("car2", FIELD_WIDTH - 100, FIELD_HEIGHT - 100, aiClass2);

        adjustCarPositions(car1, car2);

        gameState.getCars().put(car1.getId(), car1);
        gameState.getCars().put(car2.getId(), car2);

        try {
            createAIInstance(car1);
            createAIInstance(car2);
        } catch (Exception e) {
            System.err.println("Error creating AI instances: " + e.getMessage());
            resetGame();
            throw new RuntimeException("Failed to create AI instances: " + e.getMessage(), e);
        }

        gameState.setGameRunning(true);
        gameState.setGameStartTime(System.currentTimeMillis());
        gameState.setWinner(null);

        startGameLoop();

        System.out.println("Game started successfully with cars: " + gameState.getCars().keySet());
    }

    /**
     * Корректировка позиций машинок при спавне
     */
    private void adjustCarPositions(Car car1, Car car2) {
        double distance = calculateDistance(car1.getPosition(), car2.getPosition());

        if (distance < MIN_DISTANCE_BETWEEN_CARS) {
            car2.getPosition().setX(FIELD_WIDTH - 150);
            car2.getPosition().setY(FIELD_HEIGHT - 150);
        }
    }

    /**
     * Запуск игрового цикла
     */
    private void startGameLoop() {
        gameLoop = Executors.newScheduledThreadPool(1);
        gameLoop.scheduleAtFixedRate(this::gameTick, 0, 50, TimeUnit.MILLISECONDS);
    }

    /**
     * Основной игровой цикл
     */
    private void gameTick() {
        if (!gameState.isGameRunning()) return;

        try {
            updateBullets();

            for (Car car : gameState.getCars().values()) {
                if (!car.isAlive()) continue;

                Car opponent = getOpponent(car);
                CarAI ai = aiInstances.get(car.getId());

                if (ai != null && opponent != null && opponent.isAlive()) {
                    CarAction action = ai.decideAction(car, opponent, gameState.getBullets().values());
                    applyActionWithCollisionCheck(car, action);
                }
            }

            checkGameEnd();

        } catch (Exception e) {
            System.err.println("Error in game tick: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Применение действия с проверкой столкновений
     */
    private void applyActionWithCollisionCheck(Car car, CarAction action) {
        Position originalPos = new Position(
                car.getPosition().getX(),
                car.getPosition().getY(),
                car.getPosition().getAngle()
        );

        applyAction(car, action);

        for (Car otherCar : gameState.getCars().values()) {
            if (!otherCar.getId().equals(car.getId()) && otherCar.isAlive()) {
                if (checkCarCollision(car, otherCar)) {
                    car.getPosition().setX(originalPos.getX());
                    car.getPosition().setY(originalPos.getY());
                    System.out.println("Collision detected between " + car.getId() + " and " + otherCar.getId());
                    break;
                }
            }
        }

        checkBoundaries(car);
    }

    /**
     * Проверка столкновения между двумя машинками
     */
    private boolean checkCarCollision(Car car1, Car car2) {
        double distance = calculateDistance(car1.getPosition(), car2.getPosition());
        return distance < MIN_DISTANCE_BETWEEN_CARS;
    }

    /**
     * Проверка и корректировка границ поля
     */
    private void checkBoundaries(Car car) {
        Position pos = car.getPosition();
        double radius = CAR_RADIUS;

        if (pos.getX() - radius < 0) {
            pos.setX(radius);
        }
        if (pos.getX() + radius > FIELD_WIDTH) {
            pos.setX(FIELD_WIDTH - radius);
        }
        if (pos.getY() - radius < 0) {
            pos.setY(radius);
        }
        if (pos.getY() + radius > FIELD_HEIGHT) {
            pos.setY(FIELD_HEIGHT - radius);
        }
    }

    /**
     * Применение действия к машинке
     */
    private void applyAction(Car car, CarAction action) {
        Position pos = car.getPosition();

        switch (action.getAction()) {
            case MOVE_FORWARD:
                moveCar(car, CAR_SPEED * action.getIntensity());
                break;
            case MOVE_BACKWARD:
                moveCar(car, -CAR_SPEED * action.getIntensity() * 0.5);
                break;
            case TURN_LEFT:
                pos.setAngle(pos.getAngle() - TURN_SPEED * action.getIntensity());
                break;
            case TURN_RIGHT:
                pos.setAngle(pos.getAngle() + TURN_SPEED * action.getIntensity());
                break;
            case SHOOT:
                if (car.canShoot()) {
                    shootBullet(car);
                }
                break;
            case IDLE:
                break;
        }
    }

    /**
     * Движение машинки
     */
    private void moveCar(Car car, double distance) {
        Position pos = car.getPosition();
        double angleRad = Math.toRadians(pos.getAngle());

        double newX = pos.getX() + Math.cos(angleRad) * distance;
        double newY = pos.getY() + Math.sin(angleRad) * distance;

        pos.setX(newX);
        pos.setY(newY);
    }

    /**
     * Выстрел пули
     */
    private void shootBullet(Car car) {
        Bullet bullet = new Bullet();
        bullet.setId(UUID.randomUUID().toString());
        bullet.setOwnerId(car.getId());

        Position carPos = car.getPosition();
        double bulletX = carPos.getX() + Math.cos(Math.toRadians(carPos.getAngle())) * 25;
        double bulletY = carPos.getY() + Math.sin(Math.toRadians(carPos.getAngle())) * 25;

        bullet.setPosition(new Position(bulletX, bulletY, carPos.getAngle()));
        bullet.setSpeed(BULLET_SPEED);
        bullet.setAngle(carPos.getAngle());

        gameState.getBullets().put(bullet.getId(), bullet);
        car.setAmmo(car.getAmmo() - 1);
        car.setLastShotTime(System.currentTimeMillis());

        System.out.println(car.getId() + " shot bullet. Ammo left: " + car.getAmmo());
    }

    /**
     * Обновление позиций пуль
     */
    private void updateBullets() {
        Iterator<Map.Entry<String, Bullet>> iterator = gameState.getBullets().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Bullet> entry = iterator.next();
            Bullet bullet = entry.getValue();

            double angleRad = Math.toRadians(bullet.getAngle());
            bullet.getPosition().setX(bullet.getPosition().getX() + Math.cos(angleRad) * bullet.getSpeed());
            bullet.getPosition().setY(bullet.getPosition().getY() + Math.sin(angleRad) * bullet.getSpeed());

            if (checkBulletCollisions(bullet)) {
                iterator.remove();
            }
            else if (isOutOfBounds(bullet.getPosition())) {
                iterator.remove();
            }
        }
    }

    /**
     * Проверка столкновений пуль с машинками
     */
    private boolean checkBulletCollisions(Bullet bullet) {
        for (Car car : gameState.getCars().values()) {
            if (!car.isAlive() || car.getId().equals(bullet.getOwnerId())) continue;

            double distance = calculateDistance(bullet.getPosition(), car.getPosition());
            if (distance < CAR_RADIUS) {
                car.setHealth(car.getHealth() - 25);
                System.out.println("Bullet hit " + car.getId() + ". Health: " + car.getHealth());

                if (car.getHealth() <= 0) {
                    car.setAlive(false);
                    car.setHealth(0);
                    System.out.println(car.getId() + " destroyed!");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Проверка выхода за границы поля
     */
    private boolean isOutOfBounds(Position position) {
        return position.getX() < 0 || position.getX() > FIELD_WIDTH ||
                position.getY() < 0 || position.getY() > FIELD_HEIGHT;
    }

    /**
     * Проверка условий окончания игры
     */
    private void checkGameEnd() {
        List<Car> aliveCars = gameState.getCars().values().stream()
                .filter(Car::isAlive)
                .toList();

        if (aliveCars.size() <= 1) {
            gameState.setGameRunning(false);
            gameState.setGameDuration(System.currentTimeMillis() - gameState.getGameStartTime());

            if (aliveCars.size() == 1) {
                gameState.setWinner(aliveCars.get(0).getId());
            } else {
                gameState.setWinner("draw");
            }

            if (gameLoop != null) {
                gameLoop.shutdown();
            }
        }
    }

    /**
     * Создание экземпляра ИИ для машинки
     */
    private void createAIInstance(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Car cannot be null");
        }

        String aiClassName = car.getAiClass();
        if (aiClassName == null || aiClassName.trim().isEmpty()) {
            throw new IllegalArgumentException("AI class name cannot be null or empty for car: " + car.getId());
        }

        try {
            System.out.println("Creating AI instance for car " + car.getId() + ": " + aiClassName);

            Class<?> aiClass = Class.forName(aiClassName);
            CarAI ai = (CarAI) applicationContext.getBean(aiClass);
            aiInstances.put(car.getId(), ai);

            car.setAiName(ai.getAIName());

            System.out.println("Successfully created AI: " + ai.getAIName() + " for car " + car.getId());

        } catch (ClassNotFoundException e) {
            System.err.println("AI class not found: " + aiClassName);
            try {
                CarAI fallbackAI = applicationContext.getBean(org.example.my.ai.SimpleCarAI.class);
                aiInstances.put(car.getId(), fallbackAI);
                car.setAiName(fallbackAI.getAIName() + " (Fallback)");
                System.out.println("Using fallback AI for car " + car.getId());
            } catch (Exception fallbackException) {
                throw new RuntimeException("Cannot create fallback AI instance for car: " + car.getId(), fallbackException);
            }
        } catch (Exception e) {
            System.err.println("Error creating AI instance: " + e.getMessage());
            throw new RuntimeException("Cannot create AI instance: " + aiClassName + " for car: " + car.getId(), e);
        }
    }

    /**
     * Получение противника для машинки
     */
    private Car getOpponent(Car car) {
        return gameState.getCars().values().stream()
                .filter(c -> !c.getId().equals(car.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Расчет расстояния между двумя позициями
     */
    private double calculateDistance(Position p1, Position p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Сброс игры в начальное состояние
     */
    public void resetGame() {
        stopGame();

        gameState.getCars().clear();
        gameState.getBullets().clear();
        gameState.setGameRunning(false);
        gameState.setGameStartTime(0);
        gameState.setWinner(null);
        gameState.setGameDuration(0);

        aiInstances.clear();
    }

    /**
     * Остановка текущей игры
     */
    public void stopGame() {
        gameState.setGameRunning(false);

        if (gameLoop != null && !gameLoop.isShutdown()) {
            gameLoop.shutdown();
            try {
                if (!gameLoop.awaitTermination(2, TimeUnit.SECONDS)) {
                    gameLoop.shutdownNow();
                }
            } catch (InterruptedException e) {
                gameLoop.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Обработка действия игрока
     */
    public void handlePlayerAction(String carId, Object action) {
        Car car = gameState.getCars().get(carId);
        if (car != null && car.isAlive()) {
            // Реализация для ручного управления
        }
    }

    /**
     * Проверка, запущена ли игра
     */
    public boolean isGameRunning() {
        return gameState.isGameRunning();
    }

    /**
     * Получение текущего состояния игры
     */
    public GameState getGameState() {
        return gameState;
    }
}