package starvationevasion.sim.io;

import starvationevasion.sim.io.XMLparsers.KMLParser;
import starvationevasion.sim.LandTile;
import starvationevasion.sim.Territory;
import starvationevasion.sim.TileManager;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CropZoneDataIO
{
  public static final String DEFAULT_FILE = "/sim/geography/tiledata.bil";

  public static TileManager parseFile(Territory[] territoryList)
  {
    TileManager dataSet = new TileManager();

    InputStream resourceStream = KMLParser.class.getResourceAsStream(DEFAULT_FILE);


    BufferedInputStream inputStream = new BufferedInputStream(resourceStream);

    try
    {
      Territory lastUnit = null;

      //System.out.println("starting tiledata loading");
      byte bytes[] = new byte[LandTile.BYTE_DEF.SIZE_IN_BYTES];
      ByteBuffer buf = ByteBuffer.allocate(LandTile.BYTE_DEF.SIZE_IN_BYTES);
      LandTile tile;
      while (inputStream.read(bytes) != -1)
      {
        buf.clear();
        buf.put(bytes);
        tile = new LandTile(buf);
        dataSet.putTile(tile);

        // In order to avoid scanning through every territory during every iteration, we
        // hope that checking the most recent one will save us time.
        //
        if (lastUnit != null && lastUnit.containsMapPoint(tile.getCenter()))
        {
          lastUnit.addLandTile(tile);
          continue;
        }

        for (Territory agriculturalUnit : territoryList)
        {
          if (agriculturalUnit.containsMapPoint(tile.getCenter()))
          {
            agriculturalUnit.addLandTile(tile);
            lastUnit = agriculturalUnit;
          }
        }

      }
      //System.out.printf("read %d tiles in %dms%n", tiles, System.currentTimeMillis() - start);
    }
    catch (IOException e)
    {
      Logger.getGlobal().log(Level.SEVERE, "Error parsing tile data", e);
      e.printStackTrace();
    }
    return dataSet;
  }


}
