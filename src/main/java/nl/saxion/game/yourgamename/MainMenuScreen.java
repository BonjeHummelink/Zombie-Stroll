package nl.saxion.game.yourgamename;

import com.badlogic.gdx.Input;
import nl.saxion.gameapp.GameApp;
import nl.saxion.gameapp.screens.ScalableGameScreen;

import static nl.saxion.gameapp.GameApp.isKeyJustPressed;

public class MainMenuScreen extends ScalableGameScreen {
    int SelectedItem = 0;
    public MainMenuScreen() {
        super(500, 500);
    }

    @Override
    public void show()
    {
        GameApp.addFont("basic", "fonts/basic.ttf", 60);
        GameApp.addTexture("Background","Images/Background2.png" );
    }

    @Override
    public void render(float delta) {
        super.render(delta);




        // When the user presses enter, go to the next screen
        if (isKeyJustPressed(Input.Keys.ENTER)) {
            GameApp.switchScreen("Playfield");
        }

        // Render the main menu
        GameApp.clearScreen("white");
        GameApp.startSpriteRendering();
        GameApp.drawTexture("Background",0 ,0, GameApp.getWorldWidth(), getWorldHeight());
        if (SelectedItem == 0) {
            GameApp.drawTextCentered("basic", "START", getWorldWidth() / 2, getWorldHeight() / 2, "white");
        } else {
            GameApp.drawTextCentered("basic", "START", getWorldWidth()/2, getWorldHeight()/2, "yellow-500");
        }

        if (SelectedItem == 1) {
            GameApp.drawTextCentered("basic", "QUIT", getWorldWidth()/2, getWorldHeight()/2 - 150, "white");

        }else {
            GameApp.drawTextCentered("basic", "QUIT", getWorldWidth()/2, getWorldHeight()/2 - 150, "yellow-500");

        }
        GameApp.endSpriteRendering();
    }

    @Override
    public void hide() {
        GameApp.disposeFont("basic");
        GameApp.disposeTexture("Background");
    }
}
