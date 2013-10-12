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
package org.solhost.folko.uosl.network.packets;

import org.solhost.folko.uosl.network.SendableItem;
import org.solhost.folko.uosl.network.SendableMobile;
import org.solhost.folko.uosl.network.SendableObject;

public class SendObjectPacket extends SLPacket {
    public static final short ID = 0x35;

    public SendObjectPacket(SendableObject obj) {
        initWrite(ID, 0x15);
        addUDWord(obj.getSerial());
        addUWord(obj.getGraphic());
        addUByte((short) 0); // unknown, seems to be added to graphic
        if(obj instanceof SendableItem) {
            addUWord(((SendableItem) obj).getAmount());
        } else {
            addUWord(0);
        }
        addUWord(obj.getLocation().getX());
        addUWord(obj.getLocation().getY());
        if(obj instanceof SendableMobile) {
            addUByte(((SendableMobile) obj).getFacing().toByte());
        } else {
            addUByte((short) 0);
        }
        addSByte((byte) obj.getLocation().getZ());
        addUWord(obj.getHue());
    }

    @Override
    public short getID() {
        return ID;
    }
}