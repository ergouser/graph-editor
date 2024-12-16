/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults.connection;

import java.util.Arrays;
import java.util.Map;

import com.ergotech.grapheditor.model.impl.GConnectionImpl;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.core.connections.RectangularConnections;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;

/**
 * Responsible for finding the intersection points between a connection and other connections.
 */
public class IntersectionFinder {

  /**
   * Finds the intersection points of the connection with all other connections that are in front of / behind it.
   *
   * @param allPoints
   *          the map of all current points of all connections in the model
   * @param behind
   *          {@code true} to find intersections with the connections that are behind
   * @return array of intersection points for each segment of the connection
   */
//  public static double[][] find(final GConnectionSkin pSkin, final Map<GConnectionSkin, Point2D[]> allPoints,
//      final boolean behind) {
//    final Point2D[] points = allPoints.get(pSkin);
//    if (points == null) {
//      return null;
//    }
//    double[][] intersections = null;
//
//    for (int i = 0; i < points.length - 1; i++) {
//      final boolean isHorizontal = RectangularConnections.isSegmentHorizontal(pSkin.getItem(), i);
//      final double[] segmentIntersections = findSegmentIntersections(pSkin, allPoints, behind, i, isHorizontal);
//
//      final boolean isDecreasing;
//      if (isHorizontal) {
//        isDecreasing = points[i + 1].getX() < points[i].getX();
//      } else {
//        isDecreasing = points[i + 1].getY() < points[i].getY();
//      }
//
//      if (segmentIntersections != null && segmentIntersections.length > 0) {
//        Arrays.sort(segmentIntersections);
//        if (isDecreasing) {
//          reverse(segmentIntersections);
//        }
//
//        if (intersections == null) {
//          intersections = new double[points.length][];
//        }
//        intersections[i] = segmentIntersections;
//      }
//    }
//    return intersections;
//  }
  /**
   * Finds the intersection points of the connection with all other connections that are in front of / behind it.
   *
   * @param pSkin     the GConnectionSkin of the connection for which intersections are to be found
   * @param allPoints a map containing arrays of points for all connections in the model
   * @param behind    {@code true} to find intersections with the connections that are behind this one
   * @return a 2D array of intersection points, where each row corresponds to a segment of the connection, or null if none are found
   */
  public static double[][] find(final GConnectionSkin pSkin, final Map<GConnectionSkin, Point2D[]> allPoints,
                                final boolean behind) {
      final Point2D[] points = allPoints.get(pSkin);
      if (points == null) {
          return null;
      }
      double[][] intersections = null;

      // Iterate through each segment of the connection
      for (int i = 0; i < points.length - 1; i++) {
          // Determine if the segment is horizontal or vertical.
          // Assuming rectangular connections, if Y is the same, it's horizontal; if X is the same, it's vertical.
          final boolean isHorizontal = (points[i].getY() == points[i + 1].getY());

          // Find intersections for this particular segment.
          final double[] segmentIntersections = findSegmentIntersections(pSkin, allPoints, behind, i, isHorizontal);

          // Determine if the segment is decreasing or increasing along its primary axis.
          // For horizontal segments, compare X-coordinates; for vertical segments, compare Y-coordinates.
          final boolean isDecreasing;
          if (isHorizontal) {
              isDecreasing = points[i + 1].getX() < points[i].getX();
          } else {
              isDecreasing = points[i + 1].getY() < points[i].getY();
          }

          // If we found intersections, sort them and potentially reverse the order if the segment is decreasing.
          if (segmentIntersections != null && segmentIntersections.length > 0) {
              Arrays.sort(segmentIntersections);
              if (isDecreasing) {
                  reverse(segmentIntersections);
              }

              if (intersections == null) {
                  intersections = new double[points.length][];
              }
              intersections[i] = segmentIntersections;
          }
      }

      return intersections;
  }

  /**
   * Reverses the order of the elements in the given array.
   *
   * @param arr the array to be reversed
   */
  private static void reverse(double[] arr) {
      int start = 0;
      int end = arr.length - 1;
      while (start < end) {
          double temp = arr[start];
          arr[start] = arr[end];
          arr[end] = temp;
          start++;
          end--;
      }
  }

//  private static void reverse(final double[] array) {
//    for (int i = 0; i < array.length / 2; i++) {
//      double temp = array[i];
//      array[i] = array[array.length - i - 1];
//      array[array.length - i - 1] = temp;
//    }
//  }

  /**
   * Finds the intersection points of other connections with a particular connection segment.
   *
   * @param behind
   *          {@code true} to find intersections with the connections that are behind
   * @param index
   *          the index of the connection segment
   * @param isHorizontal
   *          {@code true} if the connection segment is horizontal
   * @return a list of positions along the segment where intersections occur
   */
  private static double[] findSegmentIntersections(final GConnectionSkin connection,
      final Map<GConnectionSkin, Point2D[]> allPoints, final boolean behind, final int index,
      final boolean isHorizontal) {
    double[] segmentIntersections = null;
    int resultLen = 0;
    final Point2D[] points = allPoints.get(connection);

    if (points == null) {
      return null;
    }

    for (final Map.Entry<GConnectionSkin, Point2D[]> entry : allPoints.entrySet()) {
      if (!filterConnection(connection, behind, entry.getKey())) {
        continue;
      }

      final Point2D[] otherPoints = entry.getValue();
      if (otherPoints == null) {
        continue;
      }

      for (int j = 0; j < otherPoints.length - 1; j++) {
        if (connection.equals(entry.getKey()) && index > j ^ behind) {
          continue;
        }

        if (isHorizontal) {
          final Point2D a = points[index];
          final Point2D b = points[index + 1];
          final Point2D c = otherPoints[j];
          final Point2D d = otherPoints[j + 1];

          if (GeometryUtils.checkIntersection(a, b, c, d)) {
            if (segmentIntersections == null) {
              segmentIntersections = new double[5];
            }
            if (resultLen >= segmentIntersections.length) {
              segmentIntersections = Arrays.copyOf(segmentIntersections, segmentIntersections.length + 5);
            }
            segmentIntersections[resultLen++] = c.getX();
          }
        } else {
          final Point2D a = otherPoints[j];
          final Point2D b = otherPoints[j + 1];
          final Point2D c = points[index];
          final Point2D d = points[index + 1];

          if (GeometryUtils.checkIntersection(a, b, c, d)) {
            if (segmentIntersections == null) {
              segmentIntersections = new double[5];
            }
            if (resultLen >= segmentIntersections.length) {
              segmentIntersections = Arrays.copyOf(segmentIntersections, segmentIntersections.length + 5);
            }
            segmentIntersections[resultLen++] = a.getY();
          }
        }
      }
    }
    return segmentIntersections;
  }

  /**
   * Filters out some connections, because we want to either ignore connections in front or behind. We only want one of
   * the connections at an intersection to draw a detour graphic.
   *
   * @param behind
   *          {@code true} to leave in connections behind this one and filter out those in front
   * @return a stream of connections with some filtered out
   */
  private static boolean filterConnection(final GConnectionSkin connection, final boolean behind,
      final GConnectionSkin otherConnection) {
    if (connection.equals(otherConnection)) {
      return true;
    } else if (behind) {
      return checkIfBehind(connection, otherConnection);
    } else {
      return !checkIfBehind(connection, otherConnection);
    }
  }

  /**
   * Checks if the given connection is behind this one.
   *
   * @param other
   *          another {@link GConnectionImpl} instance
   * @return {@code true} if the given connection is behind this one
   */
  private static boolean checkIfBehind(final GConnectionSkin skin, final GConnectionSkin otherSkin) {
    if (skin == null || skin.getParentIndex() == -1) {
      return false;
    } else {
      final int otherIndex = otherSkin == null ? -1 : otherSkin.getParentIndex();
      return otherIndex < skin.getParentIndex();
    }
  }
}
