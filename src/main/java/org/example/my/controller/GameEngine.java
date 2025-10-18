package org.example.my.controller;

import org.example.my.ai.CarAI;
import org.example.my.ai.template.*;
import org.example.my.model.Bullet;
import org.example.my.model.Car;
import org.example.my.model.CarAction;
import org.example.my.model.Position;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameEngine {
    private Map<String, Car> cars = new ConcurrentHashMap<>();
    private Map<String, CarAI> carAIs = new ConcurrentHashMap<>();
    private Map<String, Boolean> statsApplied = new ConcurrentHashMap<>();
    private List<Bullet> bullets = Collections.synchronizedList(new ArrayList<>());
    private boolean gameRunning = false;

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

        // НАПРЯМУЮ назначаем AI для теста
        carAIs.put("player1", new AggressiveCarAI());
        carAIs.put("player2", new DefensiveCarAI());

        // СРАЗУ применяем характеристики
        applyTankStatsIfNeeded("player1", player1);
        applyTankStatsIfNeeded("player2", player2);

        bullets.clear();
        statsApplied.clear();
        gameRunning = true;

        System.out.println("🎮 Game initialized with DIRECT AI assignment");
    }

    public void updateGame() {
        if (!gameRunning) return;

        Car player1 = cars.get("player1");
        Car player2 = cars.get("player2");
        CarAI ai1 = carAIs.get("player1");
        CarAI ai2 = carAIs.get("player2");

        if (player1 == null || player2 == null) {
            System.out.println("Cars not initialized!");
            return;
        }
        // ПРОВЕРКА AI КЛАССОВ
        if (System.currentTimeMillis() % 100 == 0) {
            System.out.println("\n🔍 AI CLASS CHECK:");
            System.out.println("   P1 AI: " + (ai1 != null ? ai1.getClass().getSimpleName() : "NULL"));
            System.out.println("   P2 AI: " + (ai2 != null ? ai2.getClass().getSimpleName() : "NULL"));

            if (ai1 != null) {
                System.out.printf("   P1 Stats: Range=%d, Speed=%d, FireRate=%d%n",
                        ai1.getShootingRange(), ai1.getMovementSpeed(), ai1.getFireRate());
            }
            if (ai2 != null) {
                System.out.printf("   P2 Stats: Range=%d, Speed=%d, FireRate=%d%n",
                        ai2.getShootingRange(), ai2.getMovementSpeed(), ai2.getFireRate());
            }
        }
        // Применяем характеристики если еще не применяли
        applyTankStatsIfNeeded("player1", player1);
        applyTankStatsIfNeeded("player2", player2);

        // Сохраняем старые позиции для отката при коллизии
        Position oldPos1 = player1.getPosition().copy();
        Position oldPos2 = player2.getPosition().copy();

        // Получаем действия от AI
        CarAction action1 = ai1.decideAction(player1, player2, bullets);
        CarAction action2 = ai2.decideAction(player2, player1, bullets);

        // Обрабатываем стрельбу
        if (action1.getType() == CarAction.ActionType.SHOOT && player1.canShoot()) {
            Bullet bullet = player1.shoot();
            if (bullet != null) {
                bullets.add(bullet);
                Position pos = bullet.getPosition();
                System.out.printf("🔫 %s shot bullet at (%.1f, %.1f)%n",
                        player1.getName(), pos.getX(), pos.getY());
            }
        }

        if (action2.getType() == CarAction.ActionType.SHOOT && player2.canShoot()) {
            Bullet bullet = player2.shoot();
            if (bullet != null) {
                bullets.add(bullet);
                Position pos = bullet.getPosition();
                System.out.printf("🔫 %s shot bullet at (%.1f, %.1f)%n",
                        player2.getName(), pos.getX(), pos.getY());
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
            System.out.println("🚗 Collision detected! Positions reverted.");
        }

        // Логируем статистику
        logTankStats();
    }

    private void applyTankStatsIfNeeded(String playerKey, Car car) {
//        if (!statsApplied.getOrDefault(playerKey, false)) {
            CarAI ai = carAIs.get(playerKey);
            if (ai != null) {
                try {
                    // ЖЕСТКАЯ проверка характеристик
                    int range = ai.getShootingRange();
                    int speed = ai.getMovementSpeed();
                    int fireRate = ai.getFireRate();
                    int total = range + speed + fireRate;

                    System.out.printf("🔍 Validating %s: Range=%d, Speed=%d, FireRate=%d, Total=%d%n",
                            playerKey, range, speed, fireRate, total);

                    if (range < 1 || range > 5 || speed < 1 || speed > 5 || fireRate < 1 || fireRate > 5) {
                        throw new IllegalStateException(
                                String.format("Stats must be between 1 and 5! Got: Range=%d, Speed=%d, FireRate=%d",
                                        range, speed, fireRate));
                    }

                    if (total > 10) {
                        throw new IllegalStateException(
                                String.format("Too many points! Max 10, got %d (Range=%d, Speed=%d, FireRate=%d)",
                                        total, range, speed, fireRate));
                    }

                    // Применяем характеристики к танку
                    applyAITankStats(ai, car);
                    statsApplied.put(playerKey, true);

                    System.out.printf("✅ SUCCESS: Applied stats for %s: %s%n",
                            playerKey, getStatsDescription(ai, car));

                } catch (Exception e) {
                    System.err.println("❌ CRITICAL ERROR for " + playerKey + ": " + e.getMessage());
                    System.err.println("🚨 Using DEFAULT stats due to invalid configuration");
                    applyDefaultStats(car);
                    statsApplied.put(playerKey, true);
                }
//            }
        }
    }

    // Добавьте в GameEngine.java
    public void setPlayerAI(int playerNumber, String aiName) {
        String playerKey = playerNumber == 1 ? "player1" : "player2";

        try {
            switch (aiName) {
                case "aggressive":
                    carAIs.put(playerKey, new AggressiveCarAI());
                    break;
                case "defensive":
                    carAIs.put(playerKey, new DefensiveCarAI());
                    break;
                case "simple":
                    carAIs.put(playerKey, new SimpleCarAI());
                    break;
                case "test":
                    carAIs.put(playerKey, new TestCarAI());
                    break;
                case "test2":
                    carAIs.put(playerKey, new Test2CarAI());
                    break;
                default:
                    carAIs.put(playerKey, new SimpleCarAI());
                    break;
            }
            System.out.println("Set " + playerKey + " AI to: " + aiName);

            // Сбрасываем примененные характеристики для этого игрока
            statsApplied.remove(playerKey);

        } catch (Exception e) {
            System.err.println("Error setting AI for " + playerKey + ": " + e.getMessage());
            carAIs.put(playerKey, new SimpleCarAI());
        }
    }

    private void applyAITankStats(CarAI ai, Car car) {
        try {
            System.out.println("🎯 ===== APPLYING STATS FOR " + car.getName() + " =====");

            int range = ai.getShootingRange();
            int speed = ai.getMovementSpeed();
            int fireRate = ai.getFireRate();

            System.out.printf("📊 RAW STATS: Range=%d, Speed=%d, FireRate=%d%n", range, speed, fireRate);

            // ПРИМЕНЯЕМ С ОГРОМНОЙ РАЗНИЦЕЙ (используем примитивные типы)
            double maxSpeed = 1.0 + (speed - 1) * 1.25;    // 1.0 - 6.0
            double acceleration = 0.05 + (speed - 1) * 0.2;
            int bulletLifetime = 500 + (range - 1) * 2000;
            long shootCooldown = 2500L - (fireRate - 1) * 600L;

            System.out.printf("⚡ APPLYING: Speed=%.1f, Accel=%.3f, Range=%dms, FireRate=%dms%n",
                    maxSpeed, acceleration, bulletLifetime, shootCooldown);

            // Применяем к танку
            car.setMaxSpeed(maxSpeed);
            car.setAcceleration(acceleration);
            car.setBulletLifetime(bulletLifetime);
            car.setShootCooldown(shootCooldown);

            System.out.printf("✅ CONFIRMED: %s now has Speed=%.1f, Range=%dms%n",
                    car.getName(), car.getMaxSpeed(), car.getBulletLifetime());

        } catch (Exception e) {
            System.err.println("❌ ERROR in applyAITankStats: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String getStatsDescription(CarAI ai, Car car) {
        return String.format("Range=%d→%dms, Speed=%d→%.1f, FireRate=%d→%dms",
                ai.getShootingRange(), car.getBulletLifetime(),
                ai.getMovementSpeed(), car.getMaxSpeed(),
                ai.getFireRate(), car.getShootCooldown());
    }

    private void applyDefaultStats(Car car) {
        car.setMaxSpeed(5.0);
        car.setAcceleration(0.2);
        car.setBulletLifetime(2000);
        car.setShootCooldown(1000);
    }

    private void updateBullets() {
        // Обновляем все активные пули
        for (Bullet bullet : bullets) {
            if (bullet.isActive()) {
                bullet.update();
            }
        }

        // Проверяем попадания пуль
        checkBulletHits();

        // Удаляем неактивные пули
        bullets.removeIf(bullet -> !bullet.isActive());
    }

    private void checkBulletHits() {
        Iterator<Bullet> iterator = bullets.iterator();
        int hitsChecked = 0;

        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            if (!bullet.isActive()) continue;

            hitsChecked++;

            for (Map.Entry<String, Car> entry : cars.entrySet()) {
                Car car = entry.getValue();

                // Пуля не должна попадать в своего владельца
                if (bullet.getOwner() == car) {
                    continue;
                }

                double distance = calculateDistance(bullet.getPosition(), car.getPosition());
                boolean isHit = distance < 25; // Радиус попадания

                // Детальное логирование для отладки
                if (distance < 50) {
                    System.out.printf("💥 Checking %s: distance=%.1f, hit=%s%n",
                            entry.getKey(), distance, isHit);
                }

                if (isHit) {
                    car.takeDamage((int) bullet.getDamage());
                    bullet.setActive(false);
                    System.out.printf("🔴 DIRECT HIT! %s took %d damage, health: %d%n",
                            entry.getKey(), (int) bullet.getDamage(), car.getHealth());

                    if (!car.isAlive()) {
                        System.out.printf("💀 %s DESTROYED!%n", entry.getKey());
                    }
                    break;
                }
            }
        }

        if (hitsChecked > 0) {
            System.out.printf("🎯 Checked %d bullets for hits%n", hitsChecked);
        }
    }

    private double calculateDistance(Position p1, Position p2) {
        if (p1 == null || p2 == null) return Double.MAX_VALUE;
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
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

        double distance = calculateDistance(car1.getPosition(), car2.getPosition());
        boolean collision = distance < 40;

        if (collision) {
            System.out.printf("🚗 Collision! Distance=%.1f%n", distance);
        }

        return collision;
    }

    private void logTankStats() {
        Car player1 = cars.get("player1");
        Car player2 = cars.get("player2");

        if (player1 != null && player2 != null) {
            // Логируем раз в 10 обновлений чтобы не спамить
            if (System.currentTimeMillis() % 10 == 0) {
                System.out.printf("🎯 STATS - P1: speed=%.1f/%.1f, health=%d | P2: speed=%.1f/%.1f, health=%d | Bullets: %d%n",
                        player1.getSpeed(), player1.getMaxSpeed(), player1.getHealth(),
                        player2.getSpeed(), player2.getMaxSpeed(), player2.getHealth(),
                        bullets.size());
            }
        }
    }

    public Map<String, Object> getGameState() {
        Map<String, Object> state = new HashMap<>();

        Car player1 = cars.get("player1");
        Car player2 = cars.get("player2");

        if (player1 != null) {
            state.put("player1", Map.of(
                    "x", player1.getPosition().getX(),
                    "y", player1.getPosition().getY(),
                    "angle", player1.getPosition().getAngle(),
                    "health", player1.getHealth(),
                    "alive", player1.isAlive(),
                    "name", player1.getName(),
                    "speed", player1.getSpeed(),
                    "maxSpeed", player1.getMaxSpeed(),
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
                    "maxSpeed", player2.getMaxSpeed(),
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
        statsApplied.clear();
    }

    public boolean isGameRunning() {
        return gameRunning;
    }
}