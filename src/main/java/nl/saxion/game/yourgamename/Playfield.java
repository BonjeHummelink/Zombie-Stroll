package nl.saxion.game.yourgamename;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

import java.util.ArrayList;

public class Playfield extends ScalableGameScreen {
    Player player;
    //Zombie zombie;
    float size = 20;
    float height = 20;
    //ArrayList<Zombie> zombies = new ArrayList<>();


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

        // --- PLAYER MOVEMENT ---
        if (GameApp.isKeyPressed(Input.Keys.UP)) {
            player.y += 200 * delta;    // smooth movement
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
        GameApp.endShapeRendering();
//        for (Zombie zombie1: zombies){
//            GameApp.drawCircle(zombie1.x, zombie1.y, 20);
//        }

    }

//    public void zombieSpawner() {
//        float x = GameApp.random(0, getWorldWidth());
//        float y = GameApp.random(0, getWorldHeight());
//        Zombie zombie = new Zombie();
//        zombie.x = x;
//        zombie.y = y;
//
//        zombies.add(zombie);
//
//    }


    @Override
    public void hide() { }
}
