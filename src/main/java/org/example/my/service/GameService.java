package org.example.my.service;

import org.example.my.ai.SimpleCarAI;
import org.example.my.controller.GameController;
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

    /**
     * Запуск новой игры
     */
    public void startGame(String aiClass1, String aiClass2) {
        // Проверка входных параметров
        if (aiClass1 == null || aiClass1.trim().isEmpty()) {
            throw new IllegalArgumentException("AI class 1 cannot be null or empty");
        }
        if (aiClass2 == null || aiClass2.trim().isEmpty()) {
            throw new IllegalArgumentException("AI class 2 cannot be null or empty");
        }

        System.out.println("Starting game with AI1: " + aiClass1 + ", AI2: " + aiClass2);

        resetGame();

        // Создаем машинки в разных углах поля
        Car car1 = new Car("car1", 100, 100, aiClass1);
        Car car2 = new Car("car2", 700, 500, aiClass2);

        gameState.getCars().put(car1.getId(), car1);
        gameState.getCars().put(car2.getId(), car2);

        // Создаем экземпляры ИИ
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

        // Запускаем игровой цикл
        startGameLoop();

        System.out.println("Game started successfully with cars: " + gameState.getCars().keySet());
    }
    /**
     * Сброс игры в начальное состояние
     */
    public void resetGame() {
        stopGame();

        // Очищаем состояние игры
        gameState.getCars().clear();
        gameState.getBullets().clear();
        gameState.setGameRunning(false);
        gameState.setGameStartTime(0);
        gameState.setWinner(null);
        gameState.setGameDuration(0);

        // Очищаем экземпляры ИИ
        aiInstances.clear();
    }

    /**
     * Остановка текущей игры
     */
    public void stopGame() {
        gameState.setGameRunning(false);

        // Останавливаем игровой цикл
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
     * Запуск игрового цикла
     */
    private void startGameLoop() {
        gameLoop = Executors.newScheduledThreadPool(1);
        gameLoop.scheduleAtFixedRate(this::gameTick, 0, 50, TimeUnit.MILLISECONDS); // 20 FPS
    }

    /**
     * Основной игровой цикл
     */
    private void gameTick() {
        if (!gameState.isGameRunning()) return;

        try {
            // Обновляем позиции пуль
            updateBullets();

            // Получаем решения ИИ и применяем их
            for (Car car : gameState.getCars().values()) {
                if (!car.isAlive()) continue;

                Car opponent = getOpponent(car);
                CarAI ai = aiInstances.get(car.getId());

                if (ai != null && opponent != null) {
                    CarAction action = ai.decideAction(car, opponent, gameState.getBullets().values());
                    applyAction(car, action);
                }
            }

            // Проверяем условия окончания игры
            checkGameEnd();

        } catch (Exception e) {
            // Логируем ошибку, но не прерываем игровой цикл
            System.err.println("Error in game tick: " + e.getMessage());
            e.printStackTrace();
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

            // Устанавливаем имя AI для отображения
            car.setAiName(ai.getAIName());

            System.out.println("Successfully created AI: " + ai.getAIName() + " for car " + car.getId());

        } catch (ClassNotFoundException e) {
            System.err.println("AI class not found: " + aiClassName);
            // Fallback на SimpleCarAI если указанный класс не найден
            try {
                CarAI fallbackAI = applicationContext.getBean(SimpleCarAI.class);
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
                // Ничего не делаем
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

        // Проверка границ поля
        if (newX >= 0 && newX <= FIELD_WIDTH) pos.setX(newX);
        if (newY >= 0 && newY <= FIELD_HEIGHT) pos.setY(newY);
    }

    /**
     * Выстрел пули
     */
    private void shootBullet(Car car) {
        Bullet bullet = new Bullet();
        bullet.setId(UUID.randomUUID().toString());
        bullet.setOwnerId(car.getId());

        Position carPos = car.getPosition();
        // Пуля появляется немного впереди машинки
        double bulletX = carPos.getX() + Math.cos(Math.toRadians(carPos.getAngle())) * 25;
        double bulletY = carPos.getY() + Math.sin(Math.toRadians(carPos.getAngle())) * 25;

        bullet.setPosition(new Position(bulletX, bulletY, carPos.getAngle()));
        bullet.setSpeed(BULLET_SPEED);
        bullet.setAngle(carPos.getAngle());

        gameState.getBullets().put(bullet.getId(), bullet);
        car.setAmmo(car.getAmmo() - 1);
        car.setLastShotTime(System.currentTimeMillis());
    }

    /**
     * Обновление позиций пуль
     */
    private void updateBullets() {
        Iterator<Map.Entry<String, Bullet>> iterator = gameState.getBullets().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Bullet> entry = iterator.next();
            Bullet bullet = entry.getValue();

            // Двигаем пулю
            double angleRad = Math.toRadians(bullet.getAngle());
            bullet.getPosition().setX(bullet.getPosition().getX() + Math.cos(angleRad) * bullet.getSpeed());
            bullet.getPosition().setY(bullet.getPosition().getY() + Math.sin(angleRad) * bullet.getSpeed());

            // Проверяем столкновения
            if (checkBulletCollisions(bullet)) {
                iterator.remove();
            }
            // Удаляем пули за пределами поля
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
            if (distance < 20) { // радиус столкновения
                car.setHealth(car.getHealth() - 25);
                if (car.getHealth() <= 0) {
                    car.setAlive(false);
                    car.setHealth(0);
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

            // Останавливаем игровой цикл
            if (gameLoop != null) {
                gameLoop.shutdown();
            }
        }
    }

    /**
     * Обработка действия игрока (для ручного управления)
     */
    public void handlePlayerAction(String carId, Object action) {
        Car car = gameState.getCars().get(carId);
        if (car != null && car.isAlive()) {
            // Пример обработки действий
            if (action instanceof GameController.PlayerAction) {
                GameController.PlayerAction playerAction = (GameController.PlayerAction) action;

                // Простая реализация обработки действий
                switch (playerAction.getActionType()) {
                    case "MOVE_FORWARD":
                        moveCar(car, CAR_SPEED);
                        break;
                    case "MOVE_BACKWARD":
                        moveCar(car, -CAR_SPEED * 0.5);
                        break;
                    case "TURN_LEFT":
                        car.getPosition().setAngle(car.getPosition().getAngle() - TURN_SPEED);
                        break;
                    case "TURN_RIGHT":
                        car.getPosition().setAngle(car.getPosition().getAngle() + TURN_SPEED);
                        break;
                    case "SHOOT":
                        if (car.canShoot()) {
                            shootBullet(car);
                        }
                        break;
                }
            }
        }
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