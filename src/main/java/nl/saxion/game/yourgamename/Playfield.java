package nl.saxion.game.yourgamename;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

import java.util.ArrayList;

public class Playfield extends ScalableGameScreen {
    Player player;
    float size = 20;
    Zombie zombie;
    float height = 20;
    ArrayList<Zombie> zombies = new ArrayList<>();
    private float elapsedTime = 0f;
    private float spawnInterval = 2f;


    public Playfield(int worldWidth, int worldHeight) {
        super(worldWidth, worldHeight);
    }

    @Override
    public void show() {
        player = new Player();
        player.x = getWorldWidth() / 2;
        player.y = 0;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        elapsedTime += delta;

        // --- PLAYER MOVEMENT ---
        if (GameApp.isKeyPressed(Input.Keys.UP)) {
            player.y += 200 * delta;
        }
        if (GameApp.isKeyPressed(Input.Keys.DOWN)) {
            player.y -= 200 * delta;
        }
        if (GameApp.isKeyPressed(Input.Keys.LEFT)) {
            player.x -= 200 * delta;
        }
        if (GameApp.isKeyPressed(Input.Keys.RIGHT)) {
            player.x += 200 * delta;
        }
        player.y = GameApp.clamp(player.y, 0, getWorldHeight() - height);
        player.x = GameApp.clamp(player.x, 0, getWorldWidth() - size);

        // --- DRAW PLAYER ---
        GameApp.clearScreen();
        GameApp.startShapeRenderingFilled();
        GameApp.drawRect(player.x, player.y, size, height);

        for (Zombie zombie : zombies) {
            GameApp.drawCircle(zombie.x, zombie.y, 20);
            float playerCenterX = player.x + size / 2;
            float playerCenterY = player.y + height / 2;

            float dx = zombie.x - playerCenterX;
            float dy = zombie.y - playerCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 20 + Math.max(size, height) / 2) {
                player.isDead = true;
            }
        }
        if (player.isDead) {
            GameApp.clearScreen();
            return;
        }


        GameApp.endShapeRendering();

        if (elapsedTime >= spawnInterval) {
            zombieSpawner();
            elapsedTime = 0f;
        }
        spawnInterval = 2f;

    }

    public void zombieSpawner() {
        float x = GameApp.random(0, getWorldWidth());
        float y = GameApp.random(0, getWorldHeight());
        Zombie zombie = new Zombie();
        zombie.x = x;
        zombie.y = y;
        int side = (int) GameApp.random(0, 2);
        switch(side) {
            case 0:
                zombie.x = 0;
                zombie.y = GameApp.random(0, getWorldHeight());
                break;
            case 1:
                zombie.x = getWorldWidth();
                zombie.y = GameApp.random(0, getWorldHeight());
                break;
        }

        zombies.add(zombie);

    }


    @Override
    public void hide() { }
}
