package org.example.my.service;

import org.apache.catalina.core.ApplicationContext;
import org.example.my.ai.CarAI;
import org.example.my.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    public void startGame(String aiClass1, String aiClass2) {
        resetGame();

        // Создаем машинки
        Car car1 = new Car("car1", 100, 100, aiClass1);
        Car car2 = new Car("car2", 700, 500, aiClass2);

        gameState.getCars().put(car1.getId(), car1);
        gameState.getCars().put(car2.getId(), car2);

        // Создаем экземпляры ИИ
        createAIInstance(car1);
        createAIInstance(car2);

        gameState.setGameRunning(true);
        gameState.setGameStartTime(System.currentTimeMillis());

        // Запускаем игровой цикл
        gameLoop = Executors.newScheduledThreadPool(1);
        gameLoop.scheduleAtFixedRate(this::gameTick, 0, 50, TimeUnit.MILLISECONDS); // 20 FPS
    }

    private void createAIInstance(Car car) {
        try {
            Class<?> aiClass = Class.forName(car.getAiClass());
            CarAI ai = (CarAI) applicationContext.getBean(aiClass);
            aiInstances.put(car.getId(), ai);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create AI instance: " + car.getAiClass(), e);
        }
    }

    private void gameTick() {
        if (!gameState.isGameRunning()) return;

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
    }

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
        }
    }

    private void moveCar(Car car, double distance) {
        Position pos = car.getPosition();
        double angleRad = Math.toRadians(pos.getAngle());

        double newX = pos.getX() + Math.cos(angleRad) * distance;
        double newY = pos.getY() + Math.sin(angleRad) * distance;

        // Проверка границ поля
        if (newX >= 0 && newX <= FIELD_WIDTH) pos.setX(newX);
        if (newY >= 0 && newY <= FIELD_HEIGHT) pos.setY(newY);
    }

    private void shootBullet(Car car) {
        Bullet bullet = new Bullet();
        bullet.setId(UUID.randomUUID().toString());
        bullet.setOwnerId(car.getId());

        Position carPos = car.getPosition();
        bullet.setPosition(new Position(carPos.getX(), carPos.getY(), carPos.getAngle()));
        bullet.setSpeed(BULLET_SPEED);
        bullet.setAngle(carPos.getAngle());

        gameState.getBullets().put(bullet.getId(), bullet);
        car.setAmmo(car.getAmmo() - 1);
        car.setLastShotTime(System.currentTimeMillis());
    }

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

    private boolean checkBulletCollisions(Bullet bullet) {
        for (Car car : gameState.getCars().values()) {
            if (!car.isAlive() || car.getId().equals(bullet.getOwnerId())) continue;

            double distance = calculateDistance(bullet.getPosition(), car.getPosition());
            if (distance < 20) { // радиус столкновения
                car.setHealth(car.getHealth() - 25);
                if (car.getHealth() <= 0) {
                    car.setAlive(false);
                }
                return true;
            }
        }
        return false;
    }

    private void checkGameEnd() {
        long aliveCount = gameState.getCars().values().stream()
                .filter(Car::isAlive)
                .count();

        if (aliveCount <= 1) {
            gameState.setGameRunning(false);
            if (gameLoop != null) {
                gameLoop.shutdown();
            }
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    // ... остальные вспомогательные методы
}