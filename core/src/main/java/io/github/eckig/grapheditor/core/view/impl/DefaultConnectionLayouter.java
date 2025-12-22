package io.github.eckig.grapheditor.core.view.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ergotech.grapheditor.model.GConnection;
import com.ergotech.grapheditor.model.GModel;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.view.ConnectionLayouter;
import javafx.geometry.Point2D;

/**
 * Default implementation of {@link ConnectionLayouter}
 */
public class DefaultConnectionLayouter implements ConnectionLayouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionLayouter.class);

  private final SkinLookup mSkinLookup;

  private GModel mModel;

  /**
   * Creates a new {@link DefaultConnectionLayouter} instance. Only one instance should exist per
   * {@link DefaultGraphEditor} instance.
   *
   * @param pSkinLookup
   *          the {@link SkinLookup} used to look up skins
   */
  public DefaultConnectionLayouter(final SkinLookup pSkinLookup) {
    mSkinLookup = pSkinLookup;
  }

  @Override
  public void initialize(final GModel pModel) {
    mModel = pModel;
  }

  @Override
  public void draw() {
    if (mModel == null /*|| mModel.getConnections().isEmpty()*/) {
      return;
    }

    try {
      redrawAllConnections(mModel.getConnections());
    } catch (Exception e) {
      LOGGER.debug("Could not redraw Connections: ", e); //$NON-NLS-1$
    }
  }

  private void redrawAllConnections(Collection<? extends GConnection> connections ) {
    final Map<GConnectionSkin, Point2D[]> connectionPoints = new HashMap<>();
    for (final GConnection connection : connections) {
      final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(connection);
      if (connectionSkin != null) {
        final Point2D[] points = connectionSkin.update();
        if (points != null) {
          connectionPoints.put(connectionSkin, points);
        }
      }
    }

    for (final GConnectionSkin skin : connectionPoints.keySet()) {
      skin.draw(connectionPoints);
    }
  }
}
