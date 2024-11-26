
```markdown
# Branch Overview: Move location and dimensions from nodes to skins

This branch is branched from the noemf branch and so has no EMF dependencies.  It moves the dimension (width, height) and the location (x, y) properties from the nodes (GNode, GConnection, GConnector and GJoint) and puts them in the skins.  It's pretty clear (and removing EMF make this more obvious) that these properties are not attributes of the node.  The node is an abstract concept without a location. It could, in principle, be represented by mutliple skins or have no location at all - the node need not have a graphical representation.

Modifying the "node" in the EMF version or the noemf branch consists of updating the x,y,width,height properties of the nodes and then generating a notification to the skin that the property has changed. That architecture is also making an attempt to keep two values in sync.  This is generally a sign of an architecture problem - which is the ground truth if they are not in sync.  If the node is he ground truth, then the representation of the node on the screen (the skin) is going to move/change when the values are finally sync (which may mean that the visual reprentation of the graph changes after, for instance, restarting the applicaiton). If the ground truth is the skin, then the node has no use for the value and it should just be in the skin.

## Motivation

Beyond the architectural cleanup as a result of this change the requirement to represent any of the nodes is simplified.  With the removal of the EMF hierarchy the node interfaces ceased to depend on any super-interface.  Removal of x,y,width,height properties simplifies the interfaces further. There's no longer a requirement for notification of the skin, or any other artifact, when properties change.  It's not precluded, but not required. A node can be any Java class that implements a, now fairly simple, interface that pulls in no dependencies.  The interface do not even have a dependency on JFX so the underlying nodes can be built in pure, even headless, Java and manipulated using JFX. This allows an architecture where the editor is used to design a graph, but where the final graph can be executed without the editor components.

You could, for example, imaging nodes that allow the creation of a web-app that is run in, say, the Spring framework.

## Skin Model

Persistence now requires that the skin model be saved.

The skin model now becomes an important part of the application and must, for example, be saved and restored with the GModel.  The downside of this is, clearly that we are now saving more/different information.  The benefit is that the skins have always been (should always have been) viewed as a critical part of the application and so should be persisited.  The full representation of the graph could never be restored accurately from the GModel, the skin type was missing and there was no ability to use different skins based on different instances of a node or store skin-specific properties beyond location (what about color, selection behavior, visibility, etc - the possibilities are endless) and polluting the nodes by adding those would be a questionable design choice.

```
