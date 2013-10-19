/*******************************************************************************
 * Copyright (c) 2013 Folke Will <folke.will@gmail.com>
 *
 * This file is part of JPhex.
 *
 * JPhex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPhex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.solhost.folko.jphex.scripting;

import org.solhost.folko.jphex.types.Item;
import org.solhost.folko.jphex.types.Mobile;
import org.solhost.folko.jphex.types.Player;
import org.solhost.folko.uosl.types.Point3D;

public abstract class SpellHandler {
    public void cast(Player player, Item scroll) { }
    public void castAt(Player player, Item scroll, Point3D target) { }
    public void castOn(Player player, Item scroll, Mobile target) { }
}