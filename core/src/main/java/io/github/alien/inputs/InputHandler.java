package io.github.alien.inputs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

import static io.github.alien.Alien.Camera;

public class InputHandler {
    private static final float ROTATION_SPEED = 0.15f;

    private final Vector3 forward = new Vector3();
    private final Vector3 right = new Vector3();

    private float yaw;
    private float pitch;

    public InputHandler(){
        yaw = 0;
        pitch = 0;

        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    public void handleInput(float delta) {
        float mouseX = -Gdx.input.getDeltaX();
        float mouseY = Gdx.input.getDeltaY();

        yaw -= mouseX * ROTATION_SPEED;
        pitch -= (mouseY * ROTATION_SPEED);
        pitch = Math.max(-89f, Math.min(89f, pitch));
        updateCameraOrientation();

        right.set(Camera.direction).crs(Camera.up).nor();
        forward.set(Camera.direction).nor();

        float speed = 0.1f;

        if (Gdx.input.isKeyPressed(Input.Keys.W))          Camera.position.add(forward.x * speed, forward.y * speed, forward.z * speed);
        if (Gdx.input.isKeyPressed(Input.Keys.S))          Camera.position.sub(forward.x * speed, forward.y * speed, forward.z * speed);
        if (Gdx.input.isKeyPressed(Input.Keys.A))          Camera.position.sub(right.x * speed, right.y * speed, right.z * speed);
        if (Gdx.input.isKeyPressed(Input.Keys.D))          Camera.position.add(right.x * speed, right.y * speed, right.z * speed);
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE))      Camera.position.add(0, speed, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) Camera.position.add(0, -speed, 0);
    }

    private void updateCameraOrientation() {
        Vector3 direction = new Vector3();

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        direction.x = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        direction.y = (float) Math.sin(pitchRad);
        direction.z = (float) (Math.sin(yawRad) * Math.cos(pitchRad));

        Camera.direction.set(direction.nor());
        Camera.up.set(Vector3.Y);
        Camera.update();
    }
}
