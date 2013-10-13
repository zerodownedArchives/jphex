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
package org.solhost.folko.uosl.data;

import java.io.IOException;
import org.solhost.folko.uosl.data.SLTiles.LandTile;
import org.solhost.folko.uosl.data.SLTiles.StaticTile;
import org.solhost.folko.uosl.types.Direction;
import org.solhost.folko.uosl.types.Point2D;
import org.solhost.folko.uosl.types.Point3D;
import org.solhost.folko.uosl.util.ObjectLister;

public class SLData {
    public static boolean DEBUG_MOVE = false;
    public static final int CHARACHTER_HEIGHT = 10; // height of a character
    private static SLData instance;
    private final String dataPath;
    private SLMap map;
    private SLPalette palette;
    private SLStatics statics;
    private SLSound sound;
    private SLArt art;
    private SLGumps gumps;
    private SLTiles tiles;

    private SLData(String dataPath) {
        this.dataPath = dataPath;
    }

    public static void init(String dataPath) throws IOException {
        if(instance != null) {
            throw new RuntimeException("client data already initialized");
        }
        instance = new SLData(dataPath);
        instance.load();
    }

    public static SLData get() {
        return instance;
    }

    private void load() throws IOException {
        map = new SLMap(dataPath +          "/MAP0.MUL");
        palette = new SLPalette(dataPath +  "/PALETTE.MUL");
        statics = new SLStatics(dataPath +  "/STATICS0.MUL", dataPath + "/STAIDX0.MUL");
        sound = new SLSound(dataPath +      "/SOUND.MUL", dataPath + "/SOUNDIDX.MUL");
        art = new SLArt(dataPath +          "/ART.MUL", dataPath + "/ARTIDX.MUL");
        tiles = new SLTiles(dataPath +      "/TILEDATA.MUL");
        gumps = new SLGumps(dataPath +      "/GUMPS.MUL");
    }

    private Point3D getElevatedPointEx(Point3D source, Direction dir, ObjectLister lister) {
        Point2D dest = source.getTranslated(dir);
        int mapZ =  map.getElevation(dest);

        int charLowerZ = source.getZ();
        int charUpperZ = charLowerZ + CHARACHTER_HEIGHT;
        StaticTile walkOn = null;
        boolean canWalk = true;
        int finalZ = mapZ;
        for(SLStatic stat : lister.getStaticAndDynamicsAtLocation(dest)) {
            StaticTile staTile = tiles.getStaticTile(stat.getStaticID());
            int staHeight = tiles.getStaticHeight(stat.getStaticID());
            int staLowerZ = stat.getLocation().getZ();
            int staUpperZ = staLowerZ + staHeight;
            int climbHeight = staUpperZ - charLowerZ;

            boolean startsAboveUs = staLowerZ > charUpperZ;
            boolean endsAboveUs = staUpperZ > charUpperZ;
            boolean endsBelowUs = staUpperZ <= charLowerZ;
            boolean inOurWay = (staLowerZ < charUpperZ && !endsBelowUs) || staLowerZ == charLowerZ;

            if(startsAboveUs) {
                // the static starts above us -> don't care
                if(DEBUG_MOVE) System.out.println("ignoring " + staTile.name + " because it is above us");
                continue;
            }

            if(inOurWay) {
                if(staTile.isImpassable() && !staTile.isSurface()) {
                    if(DEBUG_MOVE) System.err.println(staTile.name + " blocks because it is impassable and in our way");
                    canWalk = false;
                    break;
                } else if(staTile.isStair() && staUpperZ >= charUpperZ) {
                    if(DEBUG_MOVE) System.err.println(staTile.name + " blocks because it is a stair in our way " + staUpperZ + " / " + charUpperZ);
                    canWalk = false;
                    break;
                }
            }

            if(endsAboveUs) {
                if(DEBUG_MOVE) System.out.println(staTile.name + " ignored because it ends above us but wasn't impassible");
                continue;
            }

            if(climbHeight <= 7) {
                // the static is not above us but ends above us, we can climb it
                if(staUpperZ >= finalZ) {
                    if(DEBUG_MOVE) System.out.println("considering " + staTile.name + " as new standing position, climbing " + climbHeight);
                    walkOn = staTile;
                    finalZ = staUpperZ;
                    canWalk = true; // found at least one possible static
                } else {
                    if(DEBUG_MOVE) System.out.println(staTile.name + " ignored because we are climbing higher");
                }
            } else {
                if(DEBUG_MOVE) System.out.println(staTile.name + " ignored because we can't climb " + climbHeight);
                continue;
            }
        }
        if(!canWalk) {
            if(DEBUG_MOVE) System.err.println("none of the statics was walkable for us, blocking move");
            return null;
        }
        int diff = finalZ - charLowerZ;

        if(diff > 0) {
            if(DEBUG_MOVE) System.out.println("climbing " + diff);
        } else if(diff < 0) {
            if(DEBUG_MOVE) System.out.println("falling " + -diff);
        }

        // statics say yes -> but land could also block
        LandTile landTile = tiles.getLandTile(map.getTextureID(dest));
        if(walkOn == null && (landTile.isImpassable() || mapZ > CHARACHTER_HEIGHT + 7)) {
            if(DEBUG_MOVE) System.err.println("land impassable and in our way");
            return null;
        }
        if(walkOn != null) {
            if(DEBUG_MOVE) System.out.println("Walking on " + walkOn.name);
        }

        return new Point3D(dest, finalZ);
    }

    // when standing at "from" and moving in direction "dir", what's the effective 3D point?
    // returns null if impassable
    public Point3D getElevatedPoint(Point3D source, Direction dir, ObjectLister lister) {
        switch(dir) {
        case NORTH:
        case WEST:
        case SOUTH:
        case EAST:
            return getElevatedPointEx(source, dir, lister);
        case NORTH_EAST:
            if(getElevatedPointEx(source, Direction.NORTH, lister) == null) {
                return null;
            } else if(getElevatedPointEx(source, Direction.EAST, lister) == null) {
                return null;
            } else {
                return getElevatedPointEx(source, dir, lister);
            }
        case NORTH_WEST:
            if(getElevatedPointEx(source, Direction.NORTH, lister) == null) {
                return null;
            } else if(getElevatedPointEx(source, Direction.WEST, lister) == null) {
                return null;
            } else {
                return getElevatedPointEx(source, dir, lister);
            }
        case SOUTH_EAST:
            if(getElevatedPointEx(source, Direction.SOUTH, lister) == null) {
                return null;
            } else if(getElevatedPointEx(source, Direction.EAST, lister) == null) {
                return null;
            } else {
                return getElevatedPointEx(source, dir, lister);
            }
        case SOUTH_WEST:
            if(getElevatedPointEx(source, Direction.SOUTH, lister) == null) {
                return null;
            } else if(getElevatedPointEx(source, Direction.WEST, lister) == null) {
                return null;
            } else {
                return getElevatedPointEx(source, dir, lister);
            }
        default: return null;
        }
    }

    public SLMap getMap() {
        return map;
    }

    public SLPalette getPalette() {
        return palette;
    }

    public SLSound getSound() {
        return sound;
    }

    public SLArt getArt() {
        return art;
    }

    public SLTiles getTiles() {
        return tiles;
    }

    public SLGumps getGumps() {
        return gumps;
    }

    public SLStatics getStatics() {
        return statics;
    }
}