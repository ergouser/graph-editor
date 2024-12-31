/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.connections;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GConnectorPort;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.connectors.DefaultConnectorTypes;
import javafx.geometry.Side;

/**
 * Miscellaneous helper methods for rectangular-shaped connections.
 */
public final class RectangularConnections {

  private RectangularConnections() {
    // Auto-generated constructor stub
  }

  /**
   * Returns true if the segment beginning at index i is horizontal.
   *
   * <p>
   * This calculates using the index of the segment and the connector type it starts from, and <b>not</b> the current
   * position of the segment. The latter may be unreliable in the case that 2 joints are on top of each other.
   * 
   * If the source of the connection is left or right then the connection is horiziontal for even (0,2,4, etc) segments.
   * If the source of the connection is not left or right (so, top or bottom) then the connection is horizontal for odd
   * (1,3,5, etc) segments.
   * 
   * This would seem to be problematic for connectors that have bumps over other connectors, invisible sections, or in
   * general are not entirely horizontal or vertical.
   * </p>
   *
   * @param connection
   *          a {@link GConnection} instance with a non-null source connector
   * @param i
   *          an index in the list of the connection's points
   * @return {@code true} if the segment beginning at this index is horizontal
   */
  public static boolean isSegmentHorizontal(final GConnection connection, final SkinLookup skinLookup, final int i) {
    GConnectorPort source = connection.getSource();
    GConnectorSkin connectorSkin = skinLookup.lookupConnector(source);
    final Side side = connectorSkin.getSide();
    final boolean sourceIsLeft = side == Side.LEFT;
    final boolean sourceIsRight = side == Side.RIGHT;
    final boolean firstSegmentHorizontal = sourceIsLeft || sourceIsRight;

    return firstSegmentHorizontal == ((i & 1) == 0);
  }

  /**
   * Checks that the given connection has a workable number of joints.
   *
   * @param connection
   *          a {@link GConnection} that should be rectangular
   * @return {@code true} if the joint count is correct
   */
  public static boolean checkJointCount(final GConnectionSkin connectionSkin, final SkinLookup skinLookup) {
    final Side sourceSide = skinLookup.lookupConnector(connectionSkin.getItem().getSource()).getSide();
    final Side targetSide = skinLookup.lookupConnector(connectionSkin.getItem().getTarget()).getSide();

    final boolean bothHorizontal = sourceSide.isHorizontal() && targetSide.isHorizontal();
    final boolean bothVertical = sourceSide.isVertical() && targetSide.isVertical();

    if (bothHorizontal || bothVertical) {
      return (connectionSkin.getJointSkins().size() & 1) == 0;
    } else {
      return (connectionSkin.getJointSkins().size() & 1) == 1;
    }
  }
}
