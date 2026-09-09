package io.github.alien.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.alien.Alien;

import static io.github.alien.Alien.Camera;
import static io.github.alien.Alien.Viewport;

public class MainMenu implements Screen {
    public static Alien alien;

    private final ImageButton playButton, modelEditor;

    public MainMenu(Alien alien) {
        Drawable button = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/button-play.png"))));
        Drawable button2 = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("ui/button-editor.png"))));

        playButton = new ImageButton(button);
        playButton.setSize(10, 4);
        playButton.setPosition(0, 20);

        modelEditor = new ImageButton(button2);
        modelEditor.setSize(10, 4);
        modelEditor.setPosition(0, 15);

        alien.stage.addActor(playButton);
        alien.stage.addActor(modelEditor);
        Gdx.input.setInputProcessor(alien.stage);

        MainMenu.alien = alien;

        Camera.update();

        alien.stage.draw();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        alien.stage.act(Gdx.graphics.getDeltaTime()); //Perform ui logic

        playButton.addListener(event -> {
            if(event.isHandled()) {
                try {
                    alien.setScreen(new GameScreen(alien));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                alien.stage.dispose();
            }
            return false;
        });

        Camera.update();
    }

    @Override
    public void resize(int width, int height) {
        Viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        alien.stage.dispose();
    }
}
