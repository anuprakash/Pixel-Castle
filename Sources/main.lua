-- hide status bar
display.setStatusBar(display.HiddenStatusBar)

physics = require("physics")
physics.start()
physics.setGravity(0, 9.8)

local game_module = require("scripts.GameModel")
local sky_module = require("scripts.SkyViewController")
local earth_module = require("scripts.EarthViewController")
local camera_module = require("scripts.util.Camera")
local controls_module = require("scripts.Controls")
local bullet_module = require("scripts.Bullet")
local wind_module = require("scripts.Wind")

local game = game_module.GameModel:new()

local splash = require("scripts.splash")
local world = display.newGroup()

-- todo sergey: move to a specific object
local controls1
local controls2
local wind = wind_module.Wind:new({ x = 10, y = 10 })

-- Main game loop
local function gameLoop()

    if (game.state.name == "P1") then
        controls1:render(30, 200)
    elseif (game.state.name == "BULLET") then
        if (game.bullet ~= nil and not game.bullet:isAlive()) then
            game.bullet:remove()
            game.bullet = nil
            game:nextState()
        end
    elseif (game.state.name == "MOVE_TO_P2") then
        game.cameraState = "CASTLE2_FOCUS"
        game:nextState()
    elseif (game.state.name == "P2") then
        controls2:render(300, 200)
    elseif (game.state.name == "MOVE_TO_P1") then
        game.cameraState = "CASTLE1_FOCUS"
        game:nextState()
    end
end

local impulse = 11

local fireButtonRelease = function(event)
end

local function cameraListener()
    print("move complete!")
end

local function eventPlayer1Fire()
    controls1:hide()
    local cannonX = game.castle1:cannonX()
    local cannonY = game.castle1:cannonY()
    game.bullet = bullet_module.Bullet:new({game = game, world = world})
    game.bullet:fireBullet(cannonX, cannonY, impulse * math.sin(math.rad(controls1:getAngle())), impulse * math.cos(math.rad(controls1:getAngle())))
    game.cameraState = "CANNONBALL_FOCUS"
end

local function eventPlayer2Fire()
    controls2:hide()
    local cannonX = game.castle2:cannonX()
    local cannonY = game.castle2:cannonY()
    game.bullet = bullet_module.Bullet:new({game = game, world = world})
    game.bullet:fireBullet(cannonX, cannonY, impulse * math.sin(math.rad(controls2:getAngle())), impulse * math.cos(math.rad(controls2:getAngle())))
    game.cameraState = "CANNONBALL_FOCUS"
end

local function eventBulletRemoved()
    --move camera to next player
    --todo: camera:moveTo(x, y) -> listener(game:nextState())
end

local function eventPlayer1Active()
    wind:update()
    controls1:show()
    --todo: button.push -> listener(game:nextState)
end

local function eventPlayer2Active()
    wind:update()
    controls2:show()
--    random_carma(castle)
    --todo: button.push -> listener(game:nextState)
end


local function startGame()
    splash.dismissSplashScreen()

    --todo pre-step P1
    game.cameraState = "CASTLE1_FOCUS"

    game:addState({ name = "P1", listener = eventPlayer1Fire })
    game:addState({ name = "BULLET", listener = eventBulletRemoved })
    game:addState({ name = "MOVE_TO_P2", listener = eventPlayer2Active })
    game:addState({ name = "P2", listener = eventPlayer2Fire })
    game:addState({ name = "BULLET", listener = eventBulletRemoved })
    game:addState({ name = "MOVE_TO_P1", listener = eventPlayer1Active })

    game:setState("P1")

    local camera = camera_module.Camera:new({ game = game, world = world, display = display, listener = cameraListener })

    local sky = sky_module.SkyViewController:new()
    sky:render(physics, world, game)

    local earth = earth_module.EarthViewController:new()
    earth:render(physics, world, game)

    controls1 = controls_module.Controls:new({ angle = 45, x = 30, y = 200 })
    controls2 = controls_module.Controls:new({ angle = -45, x = 300, y = 200 })
    controls1.button:addEventListener("touch", function() game:nextState() end)
    controls2.button:addEventListener("touch", function() game:nextState() end)

    --    Runtime:addEventListener("collision", onCollision)
    Runtime:addEventListener("enterFrame",
        function()
            camera:moveCamera()
        end)

    timer.performWithDelay(game.delay, gameLoop, 0)
end

splash.splashScreen()
-- splash.fullSplash:addEventListener("tap", startGame)
startGame()

