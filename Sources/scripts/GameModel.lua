module(..., package.seeall)

-- local dataDumper = require("scripts.ext.DataDumper")

GameModel = {
    delay = 100,
	pixel = 10,
	cameraGoBackDelay = 2500, -- delay between stop gragging the world map and a camera to go back to it's initial place
	groundYOffset = 3,
	cannonYOffset = 5, -- in Pixel
	cameraState = "VOID", -- "CASTLE1_FOCUS", "CASTLE2_FOCUS", "CANNONBALL_FOCUS", "FOCUSING"
	minCastleHealthPercet = 99,
	exState = nil, -- Prev state should be stored here
    state = "PLAYER1", -- "PLAYER2", "PLAYER1_LOST", "PLAYER2_LOST"
    stateNumber = 1,
    states = {},
    -- options
    vibration = false,
    sfxVolume = 50,
    bgmVolume = 50,
    tint = false, -- whether to use tint or not, simply set false to eliminate it atm
    tintColor = {r = 255, g = 0, b = 255}, -- actuall tint color

    LEVEL_INTRO_DELAY = 3000
}

-- Constructor
function GameModel:new(o)
	o = o or {}   -- create object if user does not provide one
	setmetatable(o, self)
	self.__index = self
	return o
end

-- state looks like {name = "STATE_NAME", transitions = {SECOND_STATE_NAME = transitionMethod, THIRD_STATE_NAME = thirdTransitionMethod}}
function GameModel:addState(state)
    self.states[state.name] = state
end

function GameModel:setState(stateName)        
    self.state = self.states[stateName]
end

function GameModel:goto(gotoState)
    print(self.state.name .. ' -> ' .. gotoState)
    self.state.transitions[gotoState]()    
    self.exState = "" .. self.state.name
    self.state = self.states[gotoState]
end

function GameModel:enterFrame(event)
    self.camera:moveCamera()
end
