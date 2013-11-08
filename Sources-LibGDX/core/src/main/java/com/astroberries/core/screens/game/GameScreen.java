package com.astroberries.core.screens.game;

import com.astroberries.core.CastleGame;
import com.astroberries.core.config.GameConfig;
import com.astroberries.core.config.GameLevel;
import com.astroberries.core.config.GlobalGameConfig;
import com.astroberries.core.screens.game.ai.AI;
import com.astroberries.core.screens.game.ai.AIFactory;
import com.astroberries.core.screens.game.bullets.Bullet;
import com.astroberries.core.screens.game.camera.PixelCamera;
import com.astroberries.core.screens.game.castle.Castle;
import com.astroberries.core.screens.game.level.CheckRectangle;
import com.astroberries.core.screens.game.touch.MoveAndZoomListener;
import com.astroberries.core.screens.game.wind.Wind;
import com.astroberries.core.screens.game.physics.BulletContactListener;
import com.astroberries.core.screens.game.physics.GameUserData;
import com.astroberries.core.screens.game.physics.PhysicsManager;
import com.astroberries.core.state.StateName;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import java.util.Timer;
import java.util.TimerTask;

public class GameScreen implements Screen {

    private final CastleGame game;
    private final World world;

    public final PixelCamera camera;
    public final Castle castle1;
    public final Castle castle2;
    public final Wind wind;
    public final AI ai;
    public float scrollRatio;

    private BitmapFont font = new BitmapFont(Gdx.files.internal("arial-15.fnt"), false);

    private Box2DDebugRenderer debugRenderer;
    private final Matrix4 fixedPosition = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

    private int displayWidth;
    private int displayHeight;

    public int levelHeight;
    public int levelWidth;

    public float viewPortHeight;

    //disposable
    private Texture level;
    private final Texture background;
    private final Texture sky;

    private final Pixmap bulletPixmap;
    public Bullet bullet;

    private Vector3 aimEnd = new Vector3(0, 0, 0);


    private final GameLevel gameLevelConfig;
    private final PhysicsManager physicsManager;


    private static GameScreen instance;

    public static GameScreen geCreate(CastleGame game, int setNumber, int levelNumber) {
        if (GameScreen.instance == null) {
            synchronized (GameScreen.class) {
                GameScreen.instance = new GameScreen(game, setNumber, levelNumber);
            }
        }
        return GameScreen.instance;
    }

    public static GameScreen geCreate() {
        if (GameScreen.instance == null) {
            throw new Error(String.format("Cannot geCreate %s, should've been initializes prior " +
                    "to method calling with null parametres", GameScreen.class.getName()));
        }
        return GameScreen.instance;
    }

    //todo: split init to different functions
    private GameScreen(final CastleGame game, int setNumber, int levelNumber) {
        this.game = game;
        camera = new PixelCamera();
        world = new World(new Vector2(0, GlobalGameConfig.GRAVITY), true);


        debugRenderer = new Box2DDebugRenderer();

        Json json = new Json();
        GameConfig config = json.fromJson(GameConfig.class, Gdx.files.internal("configuration.json"));

        gameLevelConfig = config.getSets().get(setNumber).getLevels().get(levelNumber);
        ai = new AIFactory().getAi(gameLevelConfig.getAiVariant());
        wind = new Wind(world, gameLevelConfig);

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap levelPixmap = new Pixmap(Gdx.files.internal("levels/" + gameLevelConfig.getPath() + "/level.png"));
        levelWidth = levelPixmap.getWidth();
        levelHeight = levelPixmap.getHeight();

        castle1 = new Castle(gameLevelConfig.getCastle1(), levelWidth, levelHeight, Castle.Location.LEFT, gameLevelConfig.getVelocity(), world);
        castle2 = new Castle(gameLevelConfig.getCastle2(), levelWidth, levelHeight, Castle.Location.RIGHT, gameLevelConfig.getVelocity(), world);

        levelPixmap.drawPixmap(castle1.getCastlePixmap(), gameLevelConfig.getCastle1().getX(), gameLevelConfig.getCastle1().getY() - castle1.getCastlePixmap().getHeight());
        levelPixmap.drawPixmap(castle2.getCastlePixmap(), gameLevelConfig.getCastle2().getX(), gameLevelConfig.getCastle2().getY() - castle2.getCastlePixmap().getHeight());

        level = new Texture(levelPixmap);
        background = new Texture(Gdx.files.internal("levels/" + gameLevelConfig.getPath() + "/background.png"));
        sky = new Texture(Gdx.files.internal("levels/" + gameLevelConfig.getPath() + "/sky.png"));

        physicsManager = new PhysicsManager(world, levelPixmap, level);
        physicsManager.addRectToCheckPhysicsObjectsCreation(new CheckRectangle(0, 0, levelWidth, levelHeight));
        physicsManager.createPhysicsObjects();

        bulletPixmap = new Pixmap(Gdx.files.internal("bullets/11.png"));
        world.setContactListener(new BulletContactListener(physicsManager, bulletPixmap)); //todo: set bulletPixmap to bullet

        castle1.recalculateHealth(physicsManager);
        castle2.recalculateHealth(physicsManager);

        GestureDetector.GestureListener moveAndZoomListener = new MoveAndZoomListener(camera, game, aimEnd, this);
        InputMultiplexer processorsChain = new InputMultiplexer();
        processorsChain.addProcessor(new GestureDetector(moveAndZoomListener));
        Gdx.input.setInputProcessor(processorsChain);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        camera.handle();

        game.spriteBatch.setProjectionMatrix(camera.combined);
        game.spriteBatch.begin();
        game.spriteBatch.draw(sky, camera.position.x * 0.6f - levelWidth / 2f * 0.6f, 0);
        game.spriteBatch.draw(background, camera.position.x * 0.4f - levelWidth / 2f * 0.4f, 0);
        game.spriteBatch.draw(level, 0, 0);


        game.spriteBatch.setProjectionMatrix(fixedPosition);
        font.draw(game.spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond(), 20, 30);

        game.spriteBatch.end();

        renderAim();
        renderAimButton();
        renderWeapon();
        castle1.renderHealth(game, camera);
        castle2.renderHealth(game, camera);
        wind.render(game.spriteBatch);

        world.step(1 / 30f, 6, 2); //todo: play with this values for performance


        game.shapeRenderer.setProjectionMatrix(camera.combined); //todo: is it necessary?

        renderOrDisposeBullet();

        physicsManager.sweepDeadBodies(); //todo: sweep bodies should be only after this mess with bullet which is bad. Refactor.
        physicsManager.createPhysicsObjects();

        //debugRenderer.render(world, camera.combined);
    }

    private void renderOrDisposeBullet() {
        if (game.getStateMachine().getCurrentState() == StateName.BULLET1 || game.getStateMachine().getCurrentState() == StateName.BULLET2) {
            if (bullet != null) {
                if (!bullet.isAlive() || bullet.getCoordinates().x < 0 || bullet.getCoordinates().x > levelWidth || bullet.getCoordinates().y < 0) {
                    Gdx.app.log("bullet:", "destroy bullet!!");
                    bullet.dispose();
                    bullet = null;
                    physicsManager.sweepBodyes = true;

                    castle1.recalculateHealth(physicsManager);
                    castle2.recalculateHealth(physicsManager);
                    if (castle1.getHealth() < Castle.MIN_HEALTH) {
                        game.getStateMachine().transitionTo(StateName.PLAYER_1_LOST);
                    } else if (castle2.getHealth() < Castle.MIN_HEALTH) {
                        game.getStateMachine().transitionTo(StateName.PLAYER_2_LOST);
                    } else if (game.getStateMachine().getCurrentState() == StateName.BULLET1) {
                        game.getStateMachine().transitionTo(StateName.CAMERA_MOVING_TO_PLAYER_2);
                    } else {
                        game.getStateMachine().transitionTo(StateName.CAMERA_MOVING_TO_PLAYER_1);
                    }
                }
            }
            if (bullet != null) {
                bullet.render(game.shapeRenderer);
            }
        }
    }

    private void renderAim() {
        if (game.getStateMachine().getCurrentState() == StateName.AIMING1) {
            castle1.renderAim(aimEnd.x, aimEnd.y, game, camera);
        } else if (game.getStateMachine().getCurrentState() == StateName.AIMING2) {
            castle2.renderAim(aimEnd.x, aimEnd.y, game, camera);
        }
    }

    private void renderAimButton() {
        if (game.getStateMachine().getCurrentState() == StateName.PLAYER1) {
            castle1.renderAimButton(game, camera);
        } else if (game.getStateMachine().getCurrentState() == StateName.PLAYER2) {
            castle2.renderAimButton(game, camera);
        }
    }

    private void renderWeapon() {
        if (game.getStateMachine().getCurrentState() == StateName.PLAYER1) {
            castle1.renderWeapon(game, camera);
        } else if (game.getStateMachine().getCurrentState() == StateName.PLAYER2) {
            castle2.renderWeapon(game, camera);
        }
    }

    @Override
    public void resize(int width, int height) {
        displayWidth = width;
        displayHeight = height;
        float ratio = (float) width / (float) height;
        viewPortHeight = levelWidth / ratio;
        scrollRatio = levelWidth / (float) width;
        camera.setToOrtho(false, levelWidth, viewPortHeight);
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        background.dispose();
        sky.dispose();
        level.dispose();
        physicsManager.dispose();

        Array<Body> bodies = new Array<Body>();
        world.getBodies(bodies);
        for (Body body : bodies) {
            if (body != null) {
                GameUserData data = (GameUserData) body.getUserData();
                if (data != null) {
                    world.destroyBody(body);
                    body.setUserData(null);
                }
            }
        }
        world.dispose();
    }

    public void fire(float x, float y) {
        if (game.getStateMachine().getCurrentState() == StateName.AIMING1) {
            bullet = castle1.fire(x, y, gameLevelConfig.getVelocity(), camera, world);
            game.getStateMachine().transitionTo(StateName.BULLET1);
        } else {
            bullet = castle2.fire(x, y, gameLevelConfig.getVelocity(), camera, world);
            game.getStateMachine().transitionTo(StateName.BULLET2);
        }
    }

    // Transitions
    public void mainMenuToOverview() {
        camera.to(PixelCamera.CameraState.OVERVIEW, null, null);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                CastleGame.INSTANCE.getStateMachine().transitionTo(StateName.CAMERA_MOVING_TO_PLAYER_1);
            }
        }, GlobalGameConfig.LEVEL_INTRO_TIMEOUT);
    }


    public void toPlayer1() {
        camera.to(PixelCamera.CameraState.CASTLE1, null, StateName.PLAYER1);
    }

    public void player1ToAiming1() {
    }

    public void aiming1ToBullet1() {
        camera.to(PixelCamera.CameraState.BULLET, null, null);
    }

    public void toPlayer2() {
        camera.to(PixelCamera.CameraState.CASTLE2, null, StateName.PLAYER2);
    }

    public void toComputer2() {
        camera.to(PixelCamera.CameraState.CASTLE2, null, StateName.COMPUTER2);
    }

    public void player2ToAiming2() {
    }

    public void toBullet2() {
        camera.to(PixelCamera.CameraState.BULLET, null, null);
    }

    public void updateWind() {
        wind.update();
    }

    public void aiAimAndShoot() {
        bullet = castle2.fire(ai.nextAngle(), gameLevelConfig.getVelocity(), camera, world);
        CastleGame.INSTANCE.getStateMachine().transitionTo(StateName.BULLET2);
    }

    public void setCameraFree() {
        camera.setFree();
    }
}
