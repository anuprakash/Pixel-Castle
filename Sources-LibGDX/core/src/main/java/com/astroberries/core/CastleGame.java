package com.astroberries.core;

import com.astroberries.core.screens.lost.LevelLostScreen;
import com.astroberries.core.screens.mainmenu.MainScreen;
import com.astroberries.core.screens.game.GameScreen;
import com.astroberries.core.screens.win.LevelClearScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.astroberries.core.state.StateMachine;
import com.astroberries.core.state.StateMashineBuilder;
import com.astroberries.core.state.StateName;
import com.astroberries.core.state.Transition;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import static com.astroberries.core.state.StateName.*;

public class CastleGame extends Game {

    public static final String HUGE_TITLE_DARK_STYLE = "huge-title-dark";
    public static final String HUGE_TITLE_WHITE_STYLE = "huge-title-white";
    public static final String TITLE_WHITE_STYLE = "title-white";

    private Skin skin;
    private float ratio;

    public SpriteBatch fixedBatch;
    public ShapeRenderer shapeRenderer;
    private GameScreen gameScreen;
    private StateMachine stateMachine;
    private MainScreen mainScreen;

    private final static CastleGame instance = new CastleGame();

    private CastleGame() {
        super();
        Texture.setEnforcePotImages(false); //todo: remove and use onle gl 2.0
    }

    public static CastleGame game() {
        return instance;
    }

    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("scene2d/ui_skin/uiskin.json"));
        ratio =  Gdx.graphics.getHeight() / (float) 720;
        mainScreen = new MainScreen();

        List<BitmapFont> fonts = new ArrayList<>();
        fonts.add(skin.getFont("button-font"));
        fonts.add(skin.getFont("huge-title-font-dark-green"));
        fonts.add(skin.getFont("huge-title-font-white"));
        fonts.add(skin.getFont("title-font"));

        for (BitmapFont font : fonts) {
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            font.setScale(ratio);
        }

        fixedBatch = new SpriteBatch();
        final Matrix4 fixedPosition = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        fixedBatch.setProjectionMatrix(fixedPosition);
        shapeRenderer = new ShapeRenderer();
        Texture.setEnforcePotImages(false);
        stateMachine = initStateMachine();
        stateMachine.transitionTo(StateName.MAINMENU);
    }

    private StateMachine initStateMachine() {
/*
        Transition nilToMainMenu = new Transition() {
            @Override
            public void execute() {
                CastleGame.this.setScreen(new LevelClearScreen());
            }
        };
*/
        Transition nilToMainMenu = new Transition() {
            @Override
            public void execute() {
                CastleGame.this.setScreen(mainScreen);
            }
        };
        Transition mainMenuToChooseGame = new Transition() {
            @Override
            public void execute() {
                mainScreen.setSubScreen(MainScreen.Type.GAME_TYPE_SELECT);
            }
        };
        Transition chooseGameToMainMenu = new Transition() {
            @Override
            public void execute() {
                mainScreen.setSubScreen(MainScreen.Type.MAIN_MENU);
            }
        };
        Transition chooseGameToLevelSelect = new Transition() {
            @Override
            public void execute() {
                mainScreen.setSubScreen(MainScreen.Type.SELECT_LEVEL);
            }
        };
        Transition createGameScreen = new Transition() {
            @Override
            public void execute() {
                //todo: here we should set level and set number (set is a group of levels displayed on screen)
                gameScreen = new GameScreen(0, 0);
                CastleGame.this.getScreen().dispose();
                CastleGame.this.setScreen(gameScreen);
            }
        };
        Transition toOverview = new Transition() {
            @Override
            public void execute() {
                gameScreen.toOverview();
            }
        };
        Transition toPlayer1 = new Transition() {
            @Override
            public void execute() {
                gameScreen.toPlayer1();
            }
        };
        Transition player1ToAiming1 = new Transition() {
            @Override
            public void execute() {
                gameScreen.player1ToAiming1();
            }
        };
        Transition aiming1ToBullet1 = new Transition() {
            @Override
            public void execute() {
                gameScreen.aiming1ToBullet1();
            }
        };
        Transition toPlayer2 = new Transition() {
            @Override
            public void execute() {
                gameScreen.toPlayer2();
            }
        };
        Transition player2ToAiming2 = new Transition() {
            @Override
            public void execute() {
                gameScreen.player2ToAiming2();
            }
        };
        Transition toBullet2 = new Transition() {
            @Override
            public void execute() {
                gameScreen.toBullet2();
            }
        };
        Transition player1lost = new Transition() {
            @Override
            public void execute() {
                TextureRegion screenshot = ScreenUtils.getFrameBufferTexture();
                //CastleGame.this.getScreen().dispose();  //todo: dig here!!!
                CastleGame.this.setScreen(new LevelLostScreen(screenshot));
            }
        };
        Transition player2lost = new Transition() {
            @Override
            public void execute() {
                TextureRegion screenshot = ScreenUtils.getFrameBufferTexture();
                //CastleGame.this.getScreen().dispose();  //todo: dig here!!!
                CastleGame.this.setScreen(new LevelClearScreen(screenshot));
            }
        };
        Transition updateWind = new Transition() {
            @Override
            public void execute() {
                gameScreen.updateWind();
            }
        };
        Transition aiAimAndShoot = new Transition() {
            @Override
            public void execute() {
                gameScreen.aiAimAndShoot();
            }
        };
        Transition toComputer2 = new Transition() {
            @Override
            public void execute() {
                gameScreen.toComputer2();
            }
        };
        Transition setCameraFree = new Transition() {
            @Override
            public void execute() {
                gameScreen.setCameraFree();
            }
        };

          //PVP
/*        return new StateMashineBuilder()
                .from(NIL).to(MAINMENU).with(nilToMainMenu)
                .from(MAINMENU).to(CHOOSE_GAME).with(mainMenuToChooseGame)
                               .to(LEVEL_OVERVIEW).with(createGameScreen, toOverview)

                .from(LEVEL_OVERVIEW).to(CAMERA_MOVING_TO_PLAYER_1).with(toPlayer1)
                .from(CAMERA_MOVING_TO_PLAYER_1).to(PLAYER1).with(updateWind, setCameraFree)
                .from(PLAYER1).to(AIMING1).with(player1ToAiming1)
                .from(AIMING1).to(BULLET1).with(aiming1ToBullet1)
                .from(BULLET1).to(CAMERA_MOVING_TO_PLAYER_2).with(toPlayer2)
                              .to(PLAYER_2_LOST).with(player2lost)
                              .to(PLAYER_1_LOST).with(player1lost)
                .from(CAMERA_MOVING_TO_PLAYER_2).to(PLAYER2).with(updateWind, setCameraFree)
                .from(PLAYER2).to(AIMING2).with(player2ToAiming2)
                .from(AIMING2).to(BULLET2).with(toBullet2)
                .from(BULLET2).to(CAMERA_MOVING_TO_PLAYER_1).with(toPlayer1)
                              .to(PLAYER_2_LOST).with(player2lost)
                              .to(PLAYER_1_LOST).with(player1lost)
                .build();*/

        //AI
        return new StateMashineBuilder()
        .from(NIL).to(MAINMENU).with(nilToMainMenu)
                .from(MAINMENU).to(CHOOSE_GAME).with(mainMenuToChooseGame)
                .from(CHOOSE_GAME).to(LEVEL_OVERVIEW).with(createGameScreen, toOverview)
                                  .to(MAINMENU).with(chooseGameToMainMenu)
                                  .to(LEVEL_SELECT).with(chooseGameToLevelSelect)

                .from(LEVEL_OVERVIEW).to(CAMERA_MOVING_TO_PLAYER_1).with(toPlayer1)
                .from(CAMERA_MOVING_TO_PLAYER_1).to(PLAYER1).with(updateWind, setCameraFree)
                .from(PLAYER1).to(AIMING1).with(player1ToAiming1)
                .from(AIMING1).to(BULLET1).with(aiming1ToBullet1)
                .from(BULLET1).to(CAMERA_MOVING_TO_PLAYER_2).with(toComputer2)
                              .to(PLAYER_2_LOST).with(player2lost)
                              .to(PLAYER_1_LOST).with(player1lost)
                .from(CAMERA_MOVING_TO_PLAYER_2).to(COMPUTER2).with(updateWind, aiAimAndShoot)
                .from(COMPUTER2).to(BULLET2).with(toBullet2)
                .from(BULLET2).to(CAMERA_MOVING_TO_PLAYER_1).with(toPlayer1)
                              .to(PLAYER_2_LOST).with(player2lost)
                              .to(PLAYER_1_LOST).with(player1lost)
                .build();
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        fixedBatch.dispose();
        skin.dispose();
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public StateName state() {
        return stateMachine.getCurrentState();
    }

    public Skin getSkin() {
        return skin;
    }

    public float getRatio() {
        return ratio;
    }
}
