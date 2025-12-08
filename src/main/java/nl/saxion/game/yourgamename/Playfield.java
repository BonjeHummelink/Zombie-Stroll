package nl.saxion.game.yourgamename;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

import java.util.ArrayList;

public class Playfield extends ScalableGameScreen {
    Player player;
    float size = 20;
    int cameraSpeed = 50;
    int score = 0;
    float height = 20;
    ArrayList<Zombie> zombies = new ArrayList<>();
    ArrayList<Powerup> powerups = new ArrayList<>();
    float gettingHarder = 1f;
    private float elapsedTime = 0f;
    private float spawnInterval = 0.5f;
    private float powerupSpawnTimer = 0f;
    private float powerupSpawnInterval = 5f;

    // Powerup effects
    private float speedBoostTimer = 0f;
    private float invincibilityTimer = 0f;

    public Playfield(int worldWidth, int worldHeight) {
        super(worldWidth, worldHeight);
    }

    @Override
    public void show() {
        player = new Player();
        player.x = getWorldWidth() / 2;
        player.y = 0;
        GameApp.addFont("basic", "fonts/basic.ttf", 60);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        elapsedTime += delta;
        powerupSpawnTimer += delta;

        // Update powerup timers
        if (speedBoostTimer > 0) speedBoostTimer -= delta;
        if (invincibilityTimer > 0) invincibilityTimer -= delta;

        // --- PLAYER MOVEMENT ---
        float playerSpeed = 200;
        if (speedBoostTimer > 0) playerSpeed = 350; // Speed boost active

        if (GameApp.isKeyPressed(Input.Keys.UP)) {
            player.y += playerSpeed * delta;
        }
        if (GameApp.isKeyPressed(Input.Keys.DOWN)) {
            player.y -= playerSpeed * delta;
        }
        if (GameApp.isKeyPressed(Input.Keys.LEFT)) {
            player.x -= playerSpeed * delta;
        }
        if (GameApp.isKeyPressed(Input.Keys.RIGHT)) {
            player.x += playerSpeed * delta;
        }
        player.y = GameApp.clamp(player.y, 0, getWorldHeight() - height);
        player.x = GameApp.clamp(player.x, 0, getWorldWidth() - size);

        // --- DRAW ---
        GameApp.clearScreen();
        GameApp.startShapeRenderingFilled();

        // Draw player (red normally, blue when invincible)
        if (invincibilityTimer > 0) {
            GameApp.setColor(100, 200, 255);
        } else {
            GameApp.setColor(255, 100, 100);
        }
        GameApp.drawRect(player.x, player.y, size, height);

        // Draw zombies (green)
        GameApp.setColor(0, 255, 0);
        for (Zombie zombie : zombies) {
            GameApp.drawCircle(zombie.x, zombie.y, 20);
            float playerCenterX = player.x + size / 2;
            float playerCenterY = player.y + height / 2;

            float dx = zombie.x - playerCenterX;
            float dy = zombie.y - playerCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if(distance > 0){
                zombie.x -= (dx / distance) * zombie.speed * delta;
                zombie.y -= (dy / distance) * zombie.speed * delta;
            }

            // Check collision (only if not invincible)
            if (distance < 20 + Math.max(size, height) / 2) {
                if (invincibilityTimer <= 0) {
                    player.isDead = true;
                }
            }
        }

        // Draw powerups
        for (Powerup powerup : powerups) {
            if (powerup.type == PowerupType.SPEED) {
                GameApp.setColor(255, 255, 0); // Yellow
            } else if (powerup.type == PowerupType.INVINCIBILITY) {
                GameApp.setColor(100, 200, 255); // Blue
            } else {
                GameApp.setColor(255, 100, 255); // Purple
            }
            GameApp.drawCircle(powerup.x, powerup.y, 15);
        }

        GameApp.endShapeRendering();

        // Move zombies with camera
        for (Zombie zombie : zombies) {
            zombie.y -= cameraSpeed * delta;
        }

        // Move powerups with camera
        for (Powerup powerup : powerups) {
            powerup.y -= cameraSpeed * delta;
        }

        // Check powerup collection
        float playerCenterX = player.x + size / 2;
        float playerCenterY = player.y + height / 2;

        for (int i = powerups.size() - 1; i >= 0; i--) {
            Powerup powerup = powerups.get(i);
            float dx = powerup.x - playerCenterX;
            float dy = powerup.y - playerCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 15 + Math.max(size, height) / 2) {
                activatePowerup(powerup.type);
                powerups.remove(i);
                score += 50;
            }
        }

        // Remove off-screen entities
        zombies.removeIf(zombie -> zombie.y < -50);
        powerups.removeIf(powerup -> powerup.y < -50);

        if (player.isDead) {
            GameApp.switchScreen("MainMenuScreen");
            return;
        }

        // Spawn zombies
        if (elapsedTime >= spawnInterval) {
            zombieSpawner();
            elapsedTime = 0f;
            if (gettingHarder==0)
                spawnInterval = spawnInterval - 0.3f;
        }


        // Spawn powerups
        if (powerupSpawnTimer >= powerupSpawnInterval) {
            spawnPowerup();
            powerupSpawnTimer = 0f;
            powerupSpawnInterval = GameApp.random(4f, 8f);
        }
    }

    public void zombieSpawner() {
        Zombie zombie = new Zombie();
        int side = (int) GameApp.random(0, 3);
        switch(side) {
            case 0:
                zombie.x = 0;
                zombie.y = GameApp.random(0, getWorldHeight());
                break;
            case 1:
                zombie.x = getWorldWidth();
                zombie.y = GameApp.random(0, getWorldHeight());
                break;
            case 2:
                zombie.x = GameApp.random(0, getWorldWidth());
                zombie.y = getWorldHeight();
                break;
        }
        zombies.add(zombie);
        score += 10;
    }

    public void spawnPowerup() {
        Powerup powerup = new Powerup();
        powerup.x = GameApp.random(50, getWorldWidth() - 50);
        powerup.y = GameApp.random(getWorldHeight() / 2, getWorldHeight());

        // Random powerup type
        int typeRoll = (int) GameApp.random(0, 3);
        if (typeRoll == 0) {
            powerup.type = PowerupType.SPEED;
        } else if (typeRoll == 1) {
            powerup.type = PowerupType.INVINCIBILITY;
        } else {
            powerup.type = PowerupType.SLOW_ZOMBIES;
        }

        powerups.add(powerup);
    }

    public void activatePowerup(PowerupType type) {
        if (type == PowerupType.SPEED) {
            speedBoostTimer = 5f;
        } else if (type == PowerupType.INVINCIBILITY) {
            invincibilityTimer = 5f;
        } else if (type == PowerupType.SLOW_ZOMBIES) {
            for (Zombie zombie : zombies) {
                zombie.speed *= 0.5f;
            }
        }
    }

    @Override
    public void hide() { }
}