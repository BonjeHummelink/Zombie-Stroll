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

    // Scoreboard variable
    private float survivalTime = 0f;

    public Playfield(int worldWidth, int worldHeight) {
        super(worldWidth, worldHeight);
    }

    @Override
    public void show() {
        player = new Player();
        player.x = getWorldWidth() / 2;
        player.y = 0;
        survivalTime = 0f;
        GameApp.addFont("basic", "fonts/basic.ttf", 60);
        GameApp.addFont("scoreboardTitle", "fonts/basic.ttf", 16);
        GameApp.addFont("scoreboard", "fonts/basic.ttf", 18);
        GameApp.addFont("scoreboardSmall", "fonts/basic.ttf", 14);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        elapsedTime += delta;
        powerupSpawnTimer += delta;
        survivalTime += delta;

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

        // --- DRAW SCOREBOARD ---
        drawZombieScoreboard();

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

    private void drawZombieScoreboard() {
        float boardWidth = 160;
        float boardHeight = 100;
        float boardX = getWorldWidth() - boardWidth - 15;
        float boardY = getWorldHeight() - boardHeight - 15;

        // Draw shapes for scoreboard
        GameApp.startShapeRenderingFilled();

        // Draw thick border (blood red)
        GameApp.setColor(139, 0, 0, 255);
        GameApp.drawRect(boardX - 3, boardY - 3, boardWidth + 6, boardHeight + 6);
        GameApp.setColor(220, 20, 60, 255);
        GameApp.drawRect(boardX - 2, boardY - 2, boardWidth + 4, boardHeight + 4);

        // Draw background panel (dark zombie green)
        GameApp.setColor(15, 30, 15, 240);
        GameApp.drawRect(boardX, boardY, boardWidth, boardHeight);

        // Draw skull decorations
        GameApp.setColor(230, 230, 200, 255);
        GameApp.drawCircle(boardX + 15, boardY + boardHeight - 12, 8);
        GameApp.drawCircle(boardX + boardWidth - 15, boardY + boardHeight - 12, 8);

        // Draw blood splatters
        GameApp.setColor(139, 0, 0, 180);
        GameApp.drawCircle(boardX + 15, boardY + 15, 4);
        GameApp.drawCircle(boardX + boardWidth - 20, boardY + 18, 5);

        GameApp.endShapeRendering();

        // Draw text
        GameApp.startSpriteRendering();

        GameApp.drawText("scoreboardTitle", "ZOMBIE SURVIVAL", boardX + boardWidth / 2 - 65, boardY + boardHeight - 20, "red-600");

        // Score
        GameApp.drawText("scoreboard", "Score: " + score, boardX + 15, boardY + boardHeight - 45, "yellow-400");

        // Time
        int minutes = (int)(survivalTime / 60);
        int seconds = (int)(survivalTime % 60);
        String timeStr = String.format("Time: %d:%02d", minutes, seconds);
        GameApp.drawText("scoreboardSmall", timeStr, boardX + 15, boardY + boardHeight - 70, "lime-400");

        // Active powerup indicator
        if (invincibilityTimer > 0) {
            GameApp.drawText("scoreboardSmall", "INVINCIBLE!", boardX + 15, boardY + boardHeight - 90, "cyan-400");
        } else if (speedBoostTimer > 0) {
            GameApp.drawText("scoreboardSmall", "SPEED BOOST!", boardX + 15, boardY + boardHeight - 90, "yellow-300");
        }

        GameApp.endSpriteRendering();
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
    public void hide() {
        GameApp.disposeFont("scoreboard");
        GameApp.disposeFont("scoreboardTitle");
        GameApp.disposeFont("scoreboardSmall");
    }
}