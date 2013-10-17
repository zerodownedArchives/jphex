#-------------------------------------------------------------------------------
# Copyright (c) 2013 Folke Will <folke.will@gmail.com>
# 
# This file is part of JPhex.
# 
# JPhex is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# JPhex is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#-------------------------------------------------------------------------------
require './scripts/packages/base/mobiles/BaseMobile'
class Wolf < BaseMobile
  include MobileBehavior
  
  def onSpawn(mob)
    $api.setName(mob, "a wolf")
    $api.setGraphic(mob, 0x32)

    setStats(mob, :str => 50, :fatigue => 80, :int => 25)

    $api.setAttribute(mob, Attribute::MELEE, 700)
    $api.setAttribute(mob, Attribute::BATTLE_DEFENSE, 600)
    $api.setAttribute(mob, Attribute::MAGIC_DEFENSE, 100)
  end

  def onEnterArea(me, player)
    beAggressiveToThemAndAll(me, player)
  end

  def onAttacked(me, attacker)
    beAggressiveToThemAndAll(me, attacker)
  end
  
  def onDeath(me, corpse)
    $api.createItemInContainer(corpse, 0x12C, "food") # ham
    gold = $api.createItemInContainer(corpse, 0x01F8) # gold
    gold.setAmount((rand * 99 + 1).to_i)
  end
end