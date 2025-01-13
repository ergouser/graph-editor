
```markdown
# Branch Overview: Replacing EMF Framework with JFX Notifications

This branch replaces the EMF framework with JavaFX (JFX) notifications. Here’s an overview of the motivation behind this change.

## Motivation

The Eclipse Modeling Framework (EMF) is a powerful tool, offering extensive flexibility for supporting various types of models. However, this flexibility introduces complexity, especially when only basic functionality, like notifications, is needed. In the Graph Editor, EMF is used mainly for notifications.

Since the model components—such as `GNodes`, `GConnections`, `GConnectors`, and `GJoints`—are essentially JavaBeans, we can treat them accordingly. Thus, the EMF notification mechanism can be replaced with a custom application-specific setup using JavaFX’s `javafx.beans.property` classes and `Observable` classes from `javafx.collections`. This approach simplifies the code significantly and removes the EMF dependency.

To demonstrate the differences, let's take a look at a simple example - AddCommand


## Example of EMF AddCommand Execution

In the existing EMF-based setup, the `AddCommand` execution includes the following steps:

1. **doExecute() Method**: Adds elements to an `EList` (`ownerList`), potentially at a specific index.
    ```java
    @Override
    public void doExecute() {
      if (index == CommandParameter.NO_INDEX) {
        ownerList.addAll(collection);
      } else {
        ownerList.addAll(index, collection);
      }
    }
    ```

2. **Notification Mechanism**: When elements are added to the `EList`, EMF’s notification system triggers notifications:
   - **addAllUnique()**: Manages element additions, inverse relationships, and dispatches notifications.
     ```java
     public boolean addAllUnique(int index, Collection<? extends E> collection) {
       if (collectionSize == 0) return false;
       if (isNotificationRequired()) {
         NotificationImpl notification = createNotification(Notification.ADD, null, collection, index, isSet());
         if (hasInverse()) {
           NotificationChain notifications = createNotificationChain(collectionSize);
           for (int i = index; i < index + collectionSize; ++i) {
             notifications = inverseAdd((E)data[i], notifications);
           }
           notifications.dispatch();
         } else {
           dispatchNotification(notification);
         }
       }
       return true;
     }
     ```

3. **Queueing Notifications**: Generated notifications are added to a queue for subsequent processing.  The queue is created in EContentAdapter from the notification objects
   ```javafile:
   public final void notifyChanged(Notification pNotification) {
     if (pNotification.getEventType() != Notification.REMOVING_ADAPTER) {
       imQueue.add(pNotification);
     }
   }
       Queue<Notification> getQueue()
        {
            return imQueue;
        }
   ```

4. **CommandStackListener and Queue Processing**: The `commandStackChanged()` method from the `CommandStackListener` interface processes the queued notifications. This listener monitors the `CommandStack` state, iterating through queued notifications and triggering responses to feature changes.
   ```java
   public interface CommandStackListener {
     void commandStackChanged(EventObject event);
   }
   ```

5. **Processing Notification Queue**: During queue processing, specific features like `GMODEL__NODES` and `GNODE__CONNECTORS` are registered with listeners to handle changes:
   ```java
   while ((n = mContentAdapter.getQueue().poll()) != null) {
     try {
       processFeatureChanged(n);
     } catch (Exception e) {
       LOGGER.error("Could not process update notification '{}': ", n, e); //$NON-NLS-1$
     }
   }
   ```

6. **Listeners and UI Updates**: These listeners populate a variable (eg `mNodesToAdd`) that holds pending updates. When notified, the UI is updated by iterating over `mNodesToAdd` to add graphics:
   ```java
   if (!mNodesToAdd.isEmpty()) {
     for (GNode next : mNodesToAdd) {
       mSkinManager.lookupOrCreateNode(next);
       mModelLayoutUpdater.addNode(next);
       mSelectionManager.addNode(next);
       markConnectorsDirty(next);
     }
   }
   ```
7. ** Ultimately the updates all occur in the "process" method.

This update requires a list, a queue another list with appropriate notification on each culminating in the "process" task.  It's unnecessarily complex for the application and difficult to maintain.


## Benefits of JavaFX-Based Simplification

Using JavaFX, we can streamline the process by removing queues and reducing notification management complexity:

1. **JavaFX Properties and Collections**: JavaFX properties and collections are natively observable, triggering listeners immediately upon changes.

2. **Direct Listener Updates**: Listeners in JavaFX react directly to property or collection changes, eliminating the need for intermediary queues.

3. **Simplified Command Structure**: Commands directly modify the model, with UI updates handled by JavaFX listeners, which respond immediately.

Switching to JavaFX properties offers a more straightforward, dependency-free, and maintainable solution for handling notifications.

If we look at the JFX version of the AddCommand, the core funcationality is:
          listSupplier.getList(owner).add(element);
which adds the given type to the list and so triggers a notification.  For example, 

AddCommand.create(model, owner -> ((GModel) owner).getNodes(), node);

can be used to add a node to the model and which will cause a notification. There's a listener for this list added in GraphEditorController:

   ```java
    model.getNodes().addListener(nodesChangeListener);
   ```

   That listener calls "addNode" and the nod is added.
   ```java
    private final ListChangeListener<GNode> nodesChangeListener = change -> {
    while (change.next()) {
      if (change.wasAdded()) {
        for (GNode node : change.getAddedSubList()) {
          addNode(node);
        }
      }
      if (change.wasRemoved()) {
        for (GNode node : change.getRemoved()) {
          removeNode(node);
        }
      }
    }
  };
   ```

   The one feature that is lost by removing EMF is the ability to save the tree of nodes.  If that's important, take a look at XStream (http://x-stream.github.io/) which is particularly well suited to storing JavaBeans. The BeanInfos required to do that are in the repo.

```
