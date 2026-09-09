package io.github.alien.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.alien.Alien;
import io.github.alien.inputs.InputHandler;
import io.github.alien.loads.Loads;
import io.github.alien.world.generation.WorldGenerator;

import static io.github.alien.Alien.Camera;
import static io.github.alien.Alien.Viewport;

public class GameScreen implements Screen{
    public static Alien alien;
    public InputHandler inputHandler;

    public Stage stage;

    WorldGenerator world;

    public GameScreen(final Alien alien) throws Exception{
        GameScreen.alien = alien;

        Loads.loadContent();
        inputHandler = new InputHandler();

        world = new WorldGenerator(15, 15, 15);
        world.update();
    }

    // добавить новую анимацию ( зацикленное перемещение частей )

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        inputHandler.handleInput(delta);

        Viewport.apply();

        alien.batch.begin(Camera);
        world.render(alien.batch);
        alien.batch.end();

        world.reload();

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
        alien.batch.dispose();
    }
}
