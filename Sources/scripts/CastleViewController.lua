module(..., package.seeall)

local Memmory = require("scripts.util.Memmory")
local imageHelper = require("scripts.util.Image")
local customUI = require("scripts.util.CustomUI")
local eventTrack = require("scripts.util.EventTrack")

CastleViewController = {}

-- Constructor
function CastleViewController:new (o) --todo save (physics, world, game, x, y) at this point since tower is static
	o = o or {}   -- create object if user does not provide one
	o.bricks = {}
	o.events = eventTrack.EventTrack:new()
	setmetatable(o, self)
	self.__index = self
	return o
end

-- physics — physics object to attach to
-- world - display group for the whole scene
function CastleViewController:render(physics, world, game) --todo remove redundant params

    self.leftX = self.castleData.x * game.pixel
    self.rightX = (self.castleData.x + self.castleData.width) * game.pixel
    self.topY = (self.castleData.y + 1) * game.pixel

    self.totalHealth = self:health()
    self.width = self.castleData.width * game.pixel

    self.healthBar = display.newRect(0, 0, self.width, 3)
    self.healthBar:setReferencePoint(display.BottomLeftReferencePoint)
    self.healthBar.x = self.leftX
    self.healthBar.y = self.topY - 15
    world:insert(self.healthBar)
    self.healthBar:setFillColor(0, 255, 0, 150)

    print("Rendered castle with " .. self.castleData.x .. ", " .. self.castleData.y)
end

function CastleViewController:isDestroyed(game)
	if (self:healthPercent() < game.minCastleHealthPercet) then
		return true
	else
		return false
	end
end

function CastleViewController:updateHealth(game)
	local curr = self:healthPercent()

	local healthEstimation = (curr - game.minCastleHealthPercet ) / (100 - game.minCastleHealthPercet) * 30	
	if healthEstimation > 20 then
		self.healthBar:setFillColor(0, 255, 0, 150)
	elseif healthEstimation > 10 then
		self.healthBar:setFillColor(255, 255, 0, 150)
	else
		self.healthBar:setFillColor(255, 0, 0, 150)
	end

	if curr <= game.minCastleHealthPercet then
		self.healthBar.width = 1
	else		
		self.healthBar.width = self.width * (curr - game.minCastleHealthPercet ) / (100 - game.minCastleHealthPercet)
	end
end

function CastleViewController:say(message, callback, tint)
    local bubbleGroup = display.newGroup()
    local bubble
    if (self.location == "left") then
        bubble = display.newImageRect("images/speech_left.png", 80, 60)
    else
        bubble = display.newImageRect("images/speech_right.png", 80, 60)
    end
    if tint ~=nil then
        bubble:setFillColor(tint.r, tint.g, tint.b)
    end

    bubbleGroup:insert(bubble)

    local text = display.newText(message, -100, -100, "TrebuchetMS-Bold", 14)
    text:setReferencePoint(display.CenterReferencePoint)
    text:setTextColor(0, 0, 0)
    text.x, text.y = 0, -7
    bubbleGroup:insert(text)

    game.world:insert(bubbleGroup)
    bubbleGroup.x, bubbleGroup.y = self:cannonX(), self:cannonY()
    table.insert(Memmory.transitionStash, transition.to(bubbleGroup, {time = 700, alpha = 0, y = self:cannonY() - 50, 
        onComplete = function() 
            bubbleGroup:removeSelf() 
            callback()
        end}))
    -- Memmory.timerStash.castleSayTimer = timer.performWithDelay(2000, function()
    --         bubbleGroup:removeSelf()
    --         callback()
    --     end)
end

function CastleViewController:showBubble(game, message)
    local bubbleGroup = display.newGroup()
    local bubble
    if (self.location == "left") then
        bubble = display.newImageRect("images/speech_left.png", 80, 60)
    else
        bubble = display.newImageRect("images/speech_right.png", 80, 60)
    end
    bubbleGroup:insert(bubble)

    local text = display.newText(message, -100, -100, "TrebuchetMS-Bold", 14)
    text:setReferencePoint(display.CenterReferencePoint)
    text:setTextColor(0, 0, 0)
    text.x, text.y = 0, -7
    bubbleGroup:insert(text)

    game.world:insert(bubbleGroup)
    bubbleGroup.x, bubbleGroup.y = self:cannonX(), self:cannonY()
    table.insert(Memmory.transitionStash, transition.to(bubbleGroup, {time = 3500, alpha = 0, y = self:cannonY() - 100, onComplete = function() bubbleGroup:removeSelf() end}))
end

function CastleViewController:health()
    -- to let the castles die right away, return 5 health points
    -- return 5
    local score = 0
    for y = self.castleData.y, self.castleData.y + self.castleData.height do
        for x = self.castleData.x, self.castleData.x + self.castleData.width do
            if self.pixels[y] ~= nil and self.pixels[y][x] ~= nil then
                if self.pixels[y][x].hl == 4 then
                    score = score + 3
                elseif self.pixels[y][x].hl < 0 then
                    --do nothing
                else
                    score = score + self.pixels[y][x].hl
                end
            end
        end
    end

    return score
end

function CastleViewController:healthPercent()
	return math.round(100 * self:health() / self.totalHealth)
end	

function CastleViewController:cannonX()
    if (self.location == "left") then
	    return self.rightX + 1
    else
        return self.leftX - 1
    end
end

function CastleViewController:cannonY()
    return self.topY - 1
end


