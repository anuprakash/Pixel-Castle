module(..., package.seeall)

Camera = {game = nil, world = nil, display = nil}	

-- Constructor
-- Requires object with game, world and display parametres
function Camera:new (o)
	o = o or {}   -- create object if user does not provide one
	setmetatable(o, self)
	self.__index = self
	return o
end

local function calculateX(desiredX, game, display)
	if (desiredX < display.contentWidth / 2) then
		return 0
	elseif (desiredX > game.worldWidth - display.contentWidth / 2) then
		return - (game.worldWidth - display.contentWidth)
	else
		return -desiredX + display.contentWidth / 2
	end
end

local function calculateY(desiredY, game, display)
	if (desiredY < display.contentHeight / 4) then
		return 0
	elseif (desiredY > game.worldHeight - 3 * display.contentHeight / 4) then
		return - (game.worldHeight - display.contentHeight)
	else
		return -desiredY + display.contentHeight / 4
	end	
end

-- Camera follows bolder automatically
function Camera:moveCamera()		
	if (self.game.cameraState == "CASTLE1_FOCUS") then		
		if (self.game.castle1 ~= nil) then
			local cannonX = (self.game.castle1xOffset + self.game.castleWidth / 2) * self.game.pixel
			local cannonY =  self.game.worldHeight - (self.game.castle1.yLevel + self.game.castleHeight + self.game.cannonYOffset) * self.game.castleHeight
			transition.to(self.world, {time = 100, x = calculateX(cannonX, self.game, self.display), y = calculateY(cannonY, self.game, self.display)})
			self.game.cameraState = "FOCUSING"
		end	
	elseif (self.game.cameraState == "CASTLE2_FOCUS") then
		if (self.game.castle2 ~= nil) then
			local cannonX = (self.game.castle2xOffset + self.game.castleWidth / 2) * self.game.pixel
			local cannonY =  self.game.worldHeight - (self.game.castle2.yLevel + self.game.castleHeight + self.game.cannonYOffset) * self.game.castleHeight			
			transition.to(self.world, {time = 100, x = calculateX(cannonX, self.game, self.display), y = calculateY(cannonY, self.game, self.display)})
			self.game.cameraState = "FOCUSING"
		end		
	elseif (self.game.cameraState == "CANNONBALL_FOCUS") then
		if (self.game.bullet ~= nil and self.game.bullet.x ~= nil and self.game.bullet.y ~= nil) then
			self.world.x = calculateX(self.game.bullet.x, self.game, self.display)
			self.world.y = calculateY(self.game.bullet.y, self.game, self.display)
		end
	elseif (self.game.cameraState == "FOCUSING") then
		-- do nothing
	end
end