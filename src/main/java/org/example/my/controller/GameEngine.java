package org.example.my.controller;

import org.example.my.ai.CarAI;
import org.example.my.ai.dynamic.CustomAIManager;
import org.example.my.ai.template.AggressiveCarAI;
import org.example.my.ai.template.DefensiveCarAI;
import org.example.my.ai.template.SimpleCarAI;
import org.example.my.ai.template.SniperCarAI;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameEngine {
    private Map<String, Car> cars = new ConcurrentHashMap<>();
    private Map<String, CarAI> carAIs = new ConcurrentHashMap<>();
    private List<Bullet> bullets = Collections.synchronizedList(new ArrayList<>());
    private boolean gameRunning = false;
    private static final double CAR_RADIUS = 20.0;
    @Autowired
    private CustomAIManager customAIManager; // Добавляем зависимость

    public void initializeGame() {
        // Создаем две машины
        Car player1 = Car.builder()
                .position(new Position(100, 100, 0))
                .name("Player 1")
                .build();

        Car player2 = Car.builder()
                .position(new Position(700, 500, 180))
                .name("Player 2")
                .build();

        cars.put("player1", player1);
        cars.put("player2", player2);

        // Назначаем AI
        carAIs.put("player1", new SimpleCarAI());
        carAIs.put("player2", new SimpleCarAI());

        bullets.clear();
        gameRunning = true;
        System.out.println("Game initialized with 2 cars");
    }

    public void updateGame() {
        if (!gameRunning) return;

        Car player1 = cars.get("player1");
        Car player2 = cars.get("player2");

        if (player1 == null || player2 == null) {
            System.out.println("Cars not initialized!");
            return;
        }

        // Сохраняем старые позиции для отката при коллизии
        Position oldPos1 = new Position(
                player1.getPosition().getX(),
                player1.getPosition().getY(),
                player1.getPosition().getAngle()
        );
        Position oldPos2 = new Position(
                player2.getPosition().getX(),
                player2.getPosition().getY(),
                player2.getPosition().getAngle()
        );

        // Получаем действия от AI
        CarAI ai1 = carAIs.get("player1");
        CarAI ai2 = carAIs.get("player2");

        CarAction action1 = ai1.decideAction(player1, player2, bullets);
        CarAction action2 = ai2.decideAction(player2, player1, bullets);

        // Обрабатываем стрельбу
        if (action1.getType() == CarAction.ActionType.SHOOT && player1.canShoot()) {
            Bullet bullet = player1.shoot();
            if (bullet != null) {
                bullets.add(bullet);
                System.out.println("Player 1 shot a bullet");
            }
        }

        if (action2.getType() == CarAction.ActionType.SHOOT && player2.canShoot()) {
            Bullet bullet = player2.shoot();
            if (bullet != null) {
                bullets.add(bullet);
                System.out.println("Player 2 shot a bullet");
            }
        }

        // Применяем остальные действия
        applyNonShootAction(player1, action1);
        applyNonShootAction(player2, action2);

        // Обновляем состояние машин
        player1.update();
        player2.update();

        // Обновляем пули
        updateBullets();

        // Проверяем коллизии машин
        if (checkCollision(player1, player2)) {
            player1.setPosition(oldPos1);
            player2.setPosition(oldPos2);
            player1.setSpeed(0);
            player2.setSpeed(0);
            System.out.println("Collision detected! Positions reverted.");
        }

        // Проверяем попадания пуль
        checkBulletHits();

        // Удаляем неактивные пули
        bullets.removeIf(bullet -> !bullet.isActive());

        System.out.printf("Game updated: P1(%.1f,%.1f) P2(%.1f,%.1f) Bullets: %d%n",
                player1.getPosition().getX(), player1.getPosition().getY(),
                player2.getPosition().getX(), player2.getPosition().getY(),
                bullets.size());
    }

    private void updateBullets() {
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                bullet.update();
            }
        }
    }

    private void checkBulletHits() {
        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            if (!bullet.isActive()) continue;

            for (Map.Entry<String, Car> entry : cars.entrySet()) {
                Car car = entry.getValue();
                // Пуля не должна попадать в своего владельца
                if (bullet.getOwner() == car) continue;

                if (checkBulletHit(bullet, car)) {
                    car.takeDamage((int) bullet.getDamage());
                    bullet.deactivate();
                    System.out.println(entry.getKey() + " hit! Health: " + car.getHealth());

                    if (!car.isAlive()) {
                        System.out.println(entry.getKey() + " destroyed!");
                    }
                    break;
                }
            }
        }
    }

    private boolean checkBulletHit(Bullet bullet, Car car) {
        double distance = calculateDistance(
                bullet.getPosition(),
                car.getPosition()
        );
        return distance < CAR_RADIUS;
    }

    private void applyNonShootAction(Car car, CarAction action) {
        if (car == null || action == null || action.getType() == CarAction.ActionType.SHOOT) {
            return;
        }

        switch (action.getType()) {
            case MOVE_FORWARD:
                car.moveForward(action.getPower());
                break;
            case MOVE_BACKWARD:
                car.moveBackward(action.getPower());
                break;
            case TURN_LEFT:
                car.turnLeft(action.getPower());
                break;
            case TURN_RIGHT:
                car.turnRight(action.getPower());
                break;
            case IDLE:
                // Ничего не делаем
                break;
        }
    }

    private boolean checkCollision(Car car1, Car car2) {
        if (car1 == null || car2 == null) return false;

        double distance = calculateDistance(
                car1.getPosition(),
                car2.getPosition()
        );

        return distance < CAR_RADIUS * 2;
    }

    private double calculateDistance(Position p1, Position p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Map<String, Object> getGameState() {
        Map<String, Object> state = new HashMap<>();

        Car player1 = cars.get("player1");
        Car player2 = cars.get("player2");

        // Всегда возвращаем данные о танках, даже убитых
        if (player1 != null) {
            state.put("player1", Map.of(
                    "x", player1.getPosition().getX(),
                    "y", player1.getPosition().getY(),
                    "angle", player1.getPosition().getAngle(),
                    "health", player1.getHealth(),
                    "alive", player1.isAlive(),
                    "name", player1.getName(),
                    "speed", player1.getSpeed(),
                    "canShoot", player1.canShoot()
            ));
        }

        if (player2 != null) {
            state.put("player2", Map.of(
                    "x", player2.getPosition().getX(),
                    "y", player2.getPosition().getY(),
                    "angle", player2.getPosition().getAngle(),
                    "health", player2.getHealth(),
                    "alive", player2.isAlive(),
                    "name", player2.getName(),
                    "speed", player2.getSpeed(),
                    "canShoot", player2.canShoot()
            ));
        }

        // Добавляем информацию о пулях
        List<Map<String, Object>> bulletData = new ArrayList<>();
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                bulletData.add(Map.of(
                        "x", bullet.getX(),
                        "y", bullet.getY(),
                        "angle", bullet.getAngle()
                ));
            }
        }
        state.put("bullets", bulletData);

        state.put("gameRunning", gameRunning);
        state.put("timestamp", System.currentTimeMillis());

        return state;
    }

    public void stopGame() {
        gameRunning = false;
        cars.clear();
        carAIs.clear();
        bullets.clear();
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public void setPlayerAI(int playerNumber, String aiName) {
        String playerKey = playerNumber == 1 ? "player1" : "player2";

        if (aiName.startsWith("custom_")) {
            // Это кастомный AI
            String customAIName = aiName.substring(7); // Убираем "custom_" префикс
            CarAI customAI = customAIManager.getCustomAI(customAIName);
            if (customAI != null) {
                carAIs.put(playerKey, customAI);
                System.out.println("Set " + playerKey + " AI to custom: " + customAIName);
            } else {
                System.out.println("Custom AI not found: " + customAIName + ", using default");
                carAIs.put(playerKey, new SimpleCarAI());
            }
        } else {
            // Это встроенный AI
            switch (aiName) {
                case "aggressive":
                    carAIs.put(playerKey, new AggressiveCarAI());
                    break;
                case "defensive":
                    carAIs.put(playerKey, new DefensiveCarAI());
                    break;
                case "sniper":
                    carAIs.put(playerKey, new SniperCarAI());
                    break;
                default:
                    carAIs.put(playerKey, new SimpleCarAI());
                    break;
            }
            System.out.println("Set " + playerKey + " AI to: " + aiName);
        }
    }

    // Альтернативный метод для установки AI по имени
    public void setPlayerAI(String playerKey, String aiName) {
        setPlayerAI(playerKey.equals("player1") ? 1 : 2, aiName);
    }
}