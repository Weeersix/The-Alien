package io.github.alien;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.decals.GroupStrategy;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.alien.screens.MainMenu;

public class Alien extends Game {
    public ModelBatch batch;
    public SpriteBatch spriteBatch;

    public Stage stage;
    public BitmapFont font;

    public static PerspectiveCamera Camera;

    public static Viewport Viewport;

    @Override
    public void create() {
        Viewport = new FillViewport(50, 50);

        Camera = new PerspectiveCamera(67, 500, 500);
        Camera.position.set(0, 0, 4);
        Camera.near = 0.1f;
        Camera.far = 200;
        Camera.update();

        batch = new ModelBatch();

        spriteBatch = new SpriteBatch();

        stage = new Stage(Viewport);
        font = new BitmapFont();

        setScreen(new MainMenu(this));
    }

    @Override
    public void render(){
        super.render();
    }
}