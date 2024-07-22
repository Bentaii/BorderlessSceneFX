package com.Bentaii.BorderlessFX.scene;

import java.util.List;

import com.Bentaii.BorderlessFX.geometry.Delta;
import com.Bentaii.BorderlessFX.geometry.Dimension;
import com.Bentaii.BorderlessFX.geometry.Direction;
import com.Bentaii.BorderlessFX.geometry.HDirection;
import com.Bentaii.BorderlessFX.geometry.VDirection;
import com.Bentaii.BorderlessFX.utils.OsUtils;
import com.Bentaii.BorderlessFX.utils.WindowInstance;
import com.Bentaii.BorderlessFX.window.TransparentWindow;
import com.sun.jna.platform.win32.WinUser;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Controller for the borderless scene.
 * Used internally by {@code BorderlessSceneFX}.
 */
class DockableSceneController
{
    private static final String PANE_RESIZE_STYLE_CLASS = "borderless-scene-resize-pane";
    final Dimension _prevSize = new Dimension(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    final Delta _prevPos = new Delta(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    private final DockableScene _scene;
    private final Stage _stage;
    private final SimpleBooleanProperty _wasSnapped = new SimpleBooleanProperty(false);
    private final Pane _leftPane = new Pane();
    private final Pane _rightPane = new Pane();
    private final Pane _topPane = new Pane();
    private final Pane _bottomPane = new Pane();
    private final Pane _topLeftPane = new Pane();
    private final Pane _topRightPane = new Pane();
    private final Pane _bottomLeftPane = new Pane();
    private final Pane _bottomRightPane = new Pane();
    /**
     * Transparent Window used to as visual feedback for window snapping
     */
    private TransparentWindow _transparentWindow;

    /**
     * An object holding our window handle used to enable window animations for Windows OS
     */
    private WindowInstance _windowInstance;

    /**
     * Constructor.
     *
     * @param stage The Stage the borderless scene is attached to.
     * @param scene The BorderlessScene that this controller is associated with.
     */
    protected DockableSceneController(Stage stage, DockableScene scene)
    {
        this._stage = stage;
        this._scene = scene;

        stage.setOnShown(windowEvent -> {
            if (!_scene.getSnapped())
                updatePrevSizeAndPos();

            if (OsUtils.IS_WINDOWS)
                _windowInstance = new WindowInstance();
        });
        stage.iconifiedProperty().addListener((observableValue, aBoolean, isMinimizing) -> {
            // Update minimize property in case the user minimizes/unminimizes the stage via
            // the taskbar
            if (isMinimizing)
                _wasSnapped.set(_scene.getSnapped());
            scene.setMinimized(isMinimizing);
        });
    }

    private static Double clamp(Double value, Double min, Double max)
    {
        return Math.min(Math.max(value, min), max);
    }

    protected void createTransparentWindow()
    {
        _transparentWindow = new TransparentWindow();
        _transparentWindow.getStage().initOwner(_stage);
    }

    protected void destroyTransparentWindow()
    {
        _transparentWindow.destroy();
        _transparentWindow = null;
    }

    protected TransparentWindow getTransparentWindow()
    {
        return _transparentWindow;
    }

    protected AnchorPane load()
    {
        var anchorPane = new AnchorPane();
        anchorPane.setMaxHeight(Double.NEGATIVE_INFINITY);
        anchorPane.setMaxWidth(Double.NEGATIVE_INFINITY);
        anchorPane.setMinHeight(Double.NEGATIVE_INFINITY);
        anchorPane.setMinWidth(Double.NEGATIVE_INFINITY);
        anchorPane.setId("borderlessScene");

        _topLeftPane.setId("topLeftPane");
        _topLeftPane.getStyleClass().add(PANE_RESIZE_STYLE_CLASS);
        _topLeftPane.setPrefHeight(8.0);
        _topLeftPane.setPrefWidth(8.0);
        AnchorPane.setLeftAnchor(_topLeftPane, 0.0);
        AnchorPane.setTopAnchor(_topLeftPane, 0.0);
        _topLeftPane.setCursor(Cursor.NW_RESIZE);

        _topRightPane.setId("topRightPane");
        _topRightPane.getStyleClass().add(PANE_RESIZE_STYLE_CLASS);
        _topRightPane.setPrefHeight(8.0);
        _topRightPane.setPrefWidth(8.0);
        AnchorPane.setRightAnchor(_topRightPane, 0.0);
        AnchorPane.setTopAnchor(_topRightPane, 0.0);
        _topRightPane.setCursor(Cursor.NE_RESIZE);

        _bottomRightPane.setId("bottomRightPane");
        _bottomRightPane.getStyleClass().add(PANE_RESIZE_STYLE_CLASS);
        _bottomRightPane.prefHeight(8.0);
        _bottomRightPane.prefWidth(8.0);
        AnchorPane.setBottomAnchor(_bottomRightPane, 0.0);
        AnchorPane.setRightAnchor(_bottomRightPane, 0.0);
        _bottomRightPane.setCursor(Cursor.SE_RESIZE);

        _bottomLeftPane.setId("bottomLeftPane");
        _bottomLeftPane.getStyleClass().add(PANE_RESIZE_STYLE_CLASS);
        _bottomLeftPane.prefHeight(8.0);
        _bottomLeftPane.prefWidth(8.0);
        AnchorPane.setBottomAnchor(_bottomLeftPane, 0.0);
        AnchorPane.setLeftAnchor(_bottomLeftPane, 0.0);
        _bottomLeftPane.setCursor(Cursor.SW_RESIZE);

        _leftPane.setId("leftPane");
        _leftPane.getStyleClass().add(PANE_RESIZE_STYLE_CLASS);
        _leftPane.setPrefWidth(5.0);
        AnchorPane.setBottomAnchor(_leftPane, 5.0);
        AnchorPane.setLeftAnchor(_leftPane, 0.0);
        AnchorPane.setTopAnchor(_leftPane, 5.0);
        _leftPane.setCursor(Cursor.W_RESIZE);

        _rightPane.setId("rightPane");
        _rightPane.getStyleClass().add(PANE_RESIZE_STYLE_CLASS);
        _rightPane.setPrefWidth(5.0);
        AnchorPane.setBottomAnchor(_rightPane, 5.0);
        AnchorPane.setRightAnchor(_rightPane, 0.0);
        AnchorPane.setTopAnchor(_rightPane, 5.0);
        _rightPane.setCursor(Cursor.E_RESIZE);

        _topPane.setId("topPane");
        _topPane.getStyleClass().add(PANE_RESIZE_STYLE_CLASS);
        _topPane.setPrefHeight(5.0);
        AnchorPane.setTopAnchor(_topPane, 0.0);
        AnchorPane.setLeftAnchor(_topPane, 5.0);
        AnchorPane.setRightAnchor(_topPane, 5.0);
        _topPane.setCursor(Cursor.N_RESIZE);

        _bottomPane.setId("bottomPane");
        _bottomPane.getStyleClass().add(PANE_RESIZE_STYLE_CLASS);
        _bottomPane.setPrefHeight(5.0);
        AnchorPane.setBottomAnchor(_bottomPane, 0.0);
        AnchorPane.setLeftAnchor(_bottomPane, 5.0);
        AnchorPane.setRightAnchor(_bottomPane, 5.0);
        _bottomPane.setCursor(Cursor.S_RESIZE);

        var children = List.of(new Region(), _topLeftPane, _topRightPane, _bottomRightPane, _bottomLeftPane, _leftPane, _rightPane, _topPane, _bottomPane);
        anchorPane.getChildren().addAll(children);

        setResizeControl(_topRightPane, Direction.TOP_RIGHT);
        setResizeControl(_topPane, Direction.TOP);
        setResizeControl(_topLeftPane, Direction.TOP_LEFT);
        setResizeControl(_rightPane, Direction.RIGHT);
        setResizeControl(_leftPane, Direction.LEFT);
        setResizeControl(_bottomRightPane, Direction.BOTTOM_RIGHT);
        setResizeControl(_bottomPane, Direction.BOTTOM);
        setResizeControl(_bottomLeftPane, Direction.BOTTOM_LEFT);

        BooleanBinding allowResizingBind = Bindings.createBooleanBinding(() -> !_scene.getResizable() || _scene.getMaximized(), _scene.resizableProperty(),
            _scene.maximizedProperty());

        _topRightPane.disableProperty().bind(allowResizingBind);
        _topPane.disableProperty().bind(allowResizingBind);
        _topLeftPane.disableProperty().bind(allowResizingBind);
        _rightPane.disableProperty().bind(allowResizingBind);
        _leftPane.disableProperty().bind(allowResizingBind);
        _bottomRightPane.disableProperty().bind(allowResizingBind);
        _bottomPane.disableProperty().bind(allowResizingBind);
        _bottomLeftPane.disableProperty().bind(allowResizingBind);

        addWindowsKeyListener();

        return anchorPane;
    }

    /**
     * Maximize/unmaximize the stage.
     */
    protected void maximize()
    {
        // Because this is called via the property invalidate method the value of the property
        // has changed already hence why we evaluate the negation of it
        if (!_scene.getMaximized())
        {
            revertToPreviousSizeAndPos();
        }
        else
        {
            if (!_scene.getSnapped())
            {
                updatePrevSizeAndPos();
            }

            Rectangle2D screen;
            ObservableList<Screen> screensIntersectingHalf = getScreensIntersectingHalf();

            if (screensIntersectingHalf.isEmpty())
            {
                screen = getScreensIntersectingFull().get(0).getVisualBounds();
            }
            else
            {
                screen = screensIntersectingHalf.get(0).getVisualBounds();
            }

            _stage.setX(screen.getMinX());
            _stage.setY(screen.getMinY());
            _stage.setWidth(screen.getWidth());
            _stage.setHeight(screen.getHeight());
        }
    }

    /**
     * Minimize/unminimize the stage.
     */
    protected void minimize()
    {
        if (OsUtils.IS_WINDOWS)
        {
            // Allows to minimize/unminimize the stage from the taskbar and enables minimize
            // animations for the Windows OS
            int newStyle = _windowInstance._oldStyle | WinUser.WS_MINIMIZEBOX | (_scene.isMinimized() ? WinUser.WS_SYSMENU | WinUser.WS_CAPTION : 0);
            _windowInstance._user32.SetWindowLong(_windowInstance._hwnd, WinUser.GWL_STYLE, newStyle);
        }

        _stage.setIconified(_scene.minimizedProperty().get());
    }

    /**
     * Set the move control to move the stage with.
     *
     * @param node The node to set as the move control.
     */
    protected void setMoveControl(Node node)
    {
        final Delta delta = new Delta();
        final Delta eventSource = new Delta();

        // We are using addEventHandler() instead of setOnXXXXX() because the node is known to
        // the user, and they could very likely use the convenience method to override these
        // event handlers. For more information see:
        // https://stackoverflow.com/questions/37821796/difference-between-setonxxx-method-and-addeventhandler-javafx

        addMousePressed(node, delta, eventSource);
        addMouseDragged(node, delta, eventSource);
        addMouseReleased(node, eventSource);
        addDoubleClickMaximize(node);
    }

    private void addDoubleClickMaximize(Node node)
    {
        // Maximize/unmaximize on double click
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!event.getButton().equals(MouseButton.PRIMARY) || event.getClickCount() < 2 || !_scene.getDoubleClickMaximizable())
            {
                return;
            }

            if (_scene.getSnapped())
            {
                snapOff();
                revertToPreviousSizeAndPosClamped(getScreensIntersectingHalf().get(0).getVisualBounds());
                return;
            }

            _scene.setMaximized(!_scene.getMaximized());
        });
    }

    private void addMouseReleased(Node node, Delta eventSource)
    {
        // Snap window to position on release.
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (!_scene.getAeroSnap() || !event.getButton().equals(MouseButton.PRIMARY) || event.getScreenX() == eventSource.getX())
            {
                return;
            }

            Rectangle2D screen = Screen.getScreensForRectangle(event.getScreenX(), event.getScreenY(), 1, 1).get(0).getVisualBounds();
            Direction snapDirection = resolveSnapDirection(event, screen);

            if (snapDirection == null || _scene.getDisabledDirections().contains(snapDirection))
            {
                return;
            }

            if (snapDirection.equals(Direction.TOP) || snapDirection.equals(Direction.BOTTOM))
            {
                _scene.setMaximized(true);
            }
            else
            {
                Stage transparentWindowStage = _transparentWindow.getStage();

                _stage.setX(transparentWindowStage.getX());
                _stage.setY(transparentWindowStage.getY());
                _stage.setWidth(transparentWindowStage.getWidth());
                _stage.setHeight(transparentWindowStage.getHeight());
                _scene.setSnapped(true);
            }
            // Close the visual feedback window regardless
            if (_transparentWindow != null)
            {
                _transparentWindow.close();
            }
            _stage.setAlwaysOnTop(false);
        });
    }

    @SuppressWarnings("java:S3776")
    private void addMouseDragged(Node node, Delta delta, Delta eventSource)
    {
        // Dragging moves the stage around and displays window snapping visual feedback if enabled
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (!event.isPrimaryButtonDown())
            {
                return;
            }

            // Move x axis
            _stage.setX(event.getScreenX() - delta.getX());

            if (_scene.getSnapped())
            {
                if (event.getScreenY() > eventSource.getY())
                {
                    snapOff();
                }
                else
                {
                    Rectangle2D screen = Screen.getScreensForRectangle(event.getScreenX(), event.getScreenY(), 1, 1).get(0).getVisualBounds();
                    _stage.setHeight(screen.getHeight());
                }
            }
            else
            {
                // Move y axis
                _stage.setY(event.getScreenY() - delta.getY());
            }

            // Aero snap off
            if (_scene.getMaximized())
            {
                snapOff();
                _scene.setMaximized(false);
            }

            if (!_scene.getAeroSnap())
            {
                return;
            }

            ObservableList<Screen> screens = Screen.getScreensForRectangle(event.getScreenX(), event.getScreenY(), 1, 1);

            if (screens.isEmpty())
            {
                return;
            }

            Rectangle2D screen = screens.get(0).getVisualBounds();
            Direction snapDirection = resolveSnapDirection(event, screen);

            if (_scene.getPreventMouseOverTaskbar())
            {
                limitMouseMovement(screen, event);
            }

            if (snapDirection == null || _scene.getDisabledDirections().contains(snapDirection))
            {
                _transparentWindow.close();
                _stage.setAlwaysOnTop(false);
                return;
            }

            Stage transparentWindowStage = _transparentWindow.getStage();

            if (snapDirection.equals(Direction.TOP_RIGHT))
            {
                transparentWindowStage.setX((screen.getWidth() / 2) + screen.getMinX());
                transparentWindowStage.setY(screen.getMinY());
                transparentWindowStage.setWidth(screen.getWidth() / 2);
                transparentWindowStage.setHeight(screen.getHeight() / 2);
            }
            else if (snapDirection.equals(Direction.TOP_LEFT))
            {
                transparentWindowStage.setX(screen.getMinX());
                transparentWindowStage.setY(screen.getMinY());
                transparentWindowStage.setWidth(screen.getWidth() / 2);
                transparentWindowStage.setHeight(screen.getHeight() / 2);
            }
            else if (snapDirection.equals(Direction.BOTTOM_RIGHT))
            {
                transparentWindowStage.setX((screen.getWidth() / 2) + screen.getMinX());
                transparentWindowStage.setY(screen.getMaxY() - (screen.getHeight() / 2));
                transparentWindowStage.setWidth(screen.getWidth() / 2);
                transparentWindowStage.setHeight(screen.getHeight() / 2);
            }
            else if (snapDirection.equals(Direction.BOTTOM_LEFT))
            {
                transparentWindowStage.setX(screen.getMinX());
                transparentWindowStage.setY(screen.getMaxY() - (screen.getHeight() / 2));
                transparentWindowStage.setWidth(screen.getWidth() / 2);
                transparentWindowStage.setHeight(screen.getHeight() / 2);
            }
            else if (snapDirection.equals(Direction.RIGHT))
            {
                transparentWindowStage.setY(screen.getMinY());
                transparentWindowStage.setHeight(screen.getHeight());
                transparentWindowStage.setWidth(Math.max(screen.getWidth() / 2, transparentWindowStage.getMinWidth()));
                transparentWindowStage.setX(screen.getMaxX() - transparentWindowStage.getWidth());
            }
            else if (snapDirection.equals(Direction.LEFT))
            {
                transparentWindowStage.setY(screen.getMinY());
                transparentWindowStage.setHeight(screen.getHeight());
                transparentWindowStage.setX(screen.getMinX());
                transparentWindowStage.setWidth(Math.max(screen.getWidth() / 2, transparentWindowStage.getMinWidth()));
            }
            else if (snapDirection.equals(Direction.TOP) || snapDirection.equals(Direction.BOTTOM))
            {
                transparentWindowStage.setX(screen.getMinX());
                transparentWindowStage.setY(screen.getMinY());
                transparentWindowStage.setWidth(screen.getWidth());
                transparentWindowStage.setHeight(screen.getHeight());
            }

            _transparentWindow.show();
            // stage.toFront() doesn't seem to work, so we have to do it with stage
            // .setAlwaysOnTop() in a "hackish" way
            _stage.setAlwaysOnTop(true);
        });
    }

    private void addMousePressed(Node node, Delta delta, Delta eventSource)
    {
        // Record drag deltas on mouse press event
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (!event.isPrimaryButtonDown())
            {
                return;
            }

            delta.setX(event.getSceneX());
            delta.setY(event.getSceneY());

            if (_scene.getMaximized() || _scene.getSnapped())
            {
                delta.setX(_prevSize.getWidth() * (event.getSceneX() / _stage.getWidth()));
                delta.setY(_prevSize.getHeight() * (event.getSceneY() / _stage.getHeight()));
            }
            else
            {
                updatePrevSizeAndPos();
            }

            eventSource.setX(event.getScreenX());
            eventSource.setY(node.prefHeight(_stage.getHeight()));
        });
    }

    /**
     * Set pane (anchor points) to resize application when pressed and dragged.
     *
     * @param pane      The pane to set the resize event on.
     * @param direction The resize direction this node handles, instance of {@link Direction}.
     */
    private void setResizeControl(Pane pane, final Direction direction)
    {
        HDirection hDirection = direction.getHDirection();
        VDirection vDirection = direction.getVDirection();

        setOnDragDetected(pane);
        setOnMouseDragged(pane, hDirection, vDirection);
        setUpdatePositionDimensionOnClick(pane);
        setAeroSnapOnVerticalResizing(pane, vDirection);
        setDoubleClickAeroSnap(pane, vDirection);
    }

    private void setOnDragDetected(Pane pane)
    {
        //Record the previous size and previous position
        pane.setOnDragDetected(event -> {
            if (!_scene.getSnapped())
            {
                updatePrevSizeAndPos();
            }
        });
    }

    @SuppressWarnings("java:S3776")
    private void setOnMouseDragged(Pane pane, HDirection hDirection, VDirection vDirection)
    {
        pane.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown())
            {
                return;
            }

            final double width = _stage.getWidth();
            final double height = _stage.getHeight();
            Rectangle2D screen = getScreensIntersectingHalf().get(0).getVisualBounds();

            if (_scene.getPreventMouseOverTaskbar())
            {
                limitMouseMovement(screen, event);
            }

            // Horizontal resize
            if (hDirection != null)
            {
                double comingWidth = hDirection.equals(HDirection.LEFT) ? width - event.getScreenX() + _stage.getX() : width + event.getX();

                if (comingWidth <= 0 || comingWidth < _stage.getMinWidth() || comingWidth > _stage.getMaxWidth())
                {
                    return;
                }

                if (hDirection.equals(HDirection.LEFT))
                {
                    _stage.setWidth(_stage.getX() - event.getScreenX() + width);
                    _stage.setX(event.getScreenX());
                }
                else
                {
                    _stage.setWidth(event.getSceneX());
                }
            }

            // Vertical resize
            if (vDirection != null)
            {
                if (_scene.getSnapped() && !(_stage.getX() <= screen.getMinX() || (_stage.getX() + width) >= screen.getMaxX()))
                {
                    _stage.setHeight(_prevSize.getHeight());
                    _scene.setSnapped(false);
                }

                double comingHeight = vDirection.equals(VDirection.TOP) ? height - event.getScreenY() + _stage.getY() : height + event.getY();

                if (comingHeight <= 0 || comingHeight < _stage.getMinHeight() || comingHeight > _stage.getMaxHeight())
                {
                    return;
                }

                if (vDirection.equals(VDirection.TOP))
                {
                    _stage.setHeight(_stage.getY() - event.getScreenY() + height);
                    _stage.setY(event.getScreenY());
                }
                else
                {
                    _stage.setHeight(event.getSceneY());
                }

                if (_scene.getAeroSnap() && _scene.getVerticalResizeSnap())
                {
                    if (!isLegalVerticalResizeSnap(screen, vDirection, event))
                    {
                        _transparentWindow.close();
                        _stage.setAlwaysOnTop(false);
                        return;
                    }

                    Stage transparentWindowStage = _transparentWindow.getStage();

                    transparentWindowStage.setWidth(_stage.getWidth());
                    transparentWindowStage.setHeight(screen.getHeight());
                    transparentWindowStage.setX(_stage.getX());
                    transparentWindowStage.setY(screen.getMinY());

                    _transparentWindow.show();
                    _stage.setAlwaysOnTop(true);
                }
            }
        });
    }

    private void setUpdatePositionDimensionOnClick(Pane pane)
    {
        // Capture stage dimensions and position when the move node is pressed
        pane.setOnMousePressed(event -> {
            if (!event.isPrimaryButtonDown() || _scene.getSnapped())
            {
                return;
            }

            updatePrevSizeAndPos();
        });
    }

    private void setAeroSnapOnVerticalResizing(Pane pane, VDirection vDirection)
    {
        // Aero Snap during vertical resizing
        pane.setOnMouseReleased(event -> {
            if (vDirection == null || !_scene.getVerticalResizeSnap() || !event.getButton().equals(MouseButton.PRIMARY))
            {
                return;
            }

            Rectangle2D screen = Screen.getScreensForRectangle(event.getScreenX(), event.getScreenY(), 1, 1).get(0).getVisualBounds();

            if (isLegalVerticalResizeSnap(screen, vDirection, event))
            {
                _stage.setY(screen.getMinY());
                _stage.setHeight(screen.getHeight());
                _scene.setSnapped(true);
            }

            if (_scene.getAeroSnap())
            {
                _transparentWindow.close();
                _stage.setAlwaysOnTop(false);
            }
        });
    }

    private void setDoubleClickAeroSnap(Pane pane, VDirection vDirection)
    {
        // Aero snap resize on double click
        pane.setOnMouseClicked(event -> {
            if (!(event.getButton().equals(MouseButton.PRIMARY)) || (event.getClickCount() < 2) || (vDirection == null))
            {
                return;
            }

            if (_scene.getSnapped())
            {
                _stage.setHeight(_prevSize.getHeight());
                _stage.setY(_prevPos.getY());
                _scene.setSnapped(false);
            }
            else
            {
                Rectangle2D screen = getScreensIntersectingHalf().get(0).getVisualBounds();

                _prevSize.setHeight(_stage.getHeight());
                _prevPos.setY(_stage.getY());
                _stage.setHeight(screen.getHeight());
                _stage.setY(screen.getMinY());
                _scene.setSnapped(true);
            }
        });
    }

    private void addWindowsKeyListener()
    {
        _stage.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
            if (!keyEvent.isMetaDown() || !keyEvent.getCode().isArrowKey())
                return;

            /*
             * The default minimize and restore are already handled by windows. We only need to handle the
             * maximize with WIN + UP, the restore from maximized with WIN + DOWN and the snapping to the
             * left and right with WIN + LEFT/RIGHT
             */
            switch (keyEvent.getCode())
            {
                case LEFT -> snapLeft();
                case RIGHT -> snapRight();
                case UP ->
                {
                    if (!_scene.getMaximized() && _scene.getSnapped() && !_wasSnapped.get())
                        _scene.maximizeStage();
                    _wasSnapped.set(false);
                }
                case DOWN ->
                {
                    if (!_scene.getMaximized() && _scene.getSnapped())
                        _scene.minimizeStage();
                }
                default ->
                {/*Should never happen but sonar is not happy*/}
            }
        });
    }

    private void snapRight()
    {
        if (_scene.isMinimized() || _scene.getDisabledDirections().contains(Direction.LEFT))
            return;

        Rectangle2D screen;
        ObservableList<Screen> screensIntersectingHalf = getScreensIntersectingHalf();
        if (screensIntersectingHalf.isEmpty())
        {
            screen = getScreensIntersectingFull().get(0).getVisualBounds();
        }
        else
        {
            screen = screensIntersectingHalf.get(0).getVisualBounds();
        }

        if (!_scene.getSnapped())
            updatePrevSizeAndPos();
        _scene.setMaximized(false);
        _stage.setY(screen.getMinY());
        _stage.setHeight(screen.getHeight());
        _stage.setWidth(screen.getWidth() / 2);
        _stage.setX(screen.getMaxX() - _stage.getWidth());
        _scene.setSnapped(true);
    }

    private void snapLeft()
    {
        if (_scene.isMinimized() || _scene.getDisabledDirections().contains(Direction.LEFT))
            return;
        Rectangle2D screen;
        ObservableList<Screen> screensIntersectingHalf = getScreensIntersectingHalf();
        if (screensIntersectingHalf.isEmpty())
        {
            screen = getScreensIntersectingFull().get(0).getVisualBounds();
        }
        else
        {
            screen = screensIntersectingHalf.get(0).getVisualBounds();
        }

        if (!_scene.getSnapped())
            updatePrevSizeAndPos();
        _scene.setMaximized(false);
        _stage.setX(screen.getMinX());
        _stage.setY(screen.getMinY());
        _stage.setWidth(screen.getWidth() / 2);
        _stage.setHeight(screen.getHeight());
        _scene.setSnapped(true);
    }

    private Direction resolveSnapDirection(MouseEvent event, Rectangle2D screen)
    {
        Double allowance = _scene.getAeroSnapAllowance();
        Double cornerAllowance = _scene.getAeroSnapCornerAllowance();

        if (event.getScreenY() <= screen.getMinY() + cornerAllowance && event.getScreenX() >= screen.getMaxX() - cornerAllowance)
        {
            // Window snap top right corner
            return Direction.TOP_RIGHT;
        }

        if (event.getScreenY() <= screen.getMinY() + cornerAllowance && event.getScreenX() <= screen.getMinX() + cornerAllowance)
        {
            // Window snap top left corner
            return Direction.TOP_LEFT;
        }

        if (event.getScreenY() >= screen.getMaxY() - cornerAllowance && event.getScreenX() >= screen.getMaxX() - cornerAllowance)
        {
            // Window snap bottom right corner
            return Direction.BOTTOM_RIGHT;
        }

        if (event.getScreenY() >= screen.getMaxY() - cornerAllowance && event.getScreenX() <= screen.getMinX() + cornerAllowance)
        {
            // Window snap bottom left corner
            return Direction.BOTTOM_LEFT;
        }

        if (event.getScreenX() >= screen.getMaxX() - allowance)
        {
            // Window snap right
            return Direction.RIGHT;
        }

        if (event.getScreenX() <= screen.getMinX() + allowance)
        {
            // Window snap left
            return Direction.LEFT;
        }

        if (event.getScreenY() <= screen.getMinY() + allowance)
        {
            // Window snap top
            return Direction.TOP;
        }

        if (event.getScreenY() >= screen.getMaxY() - allowance)
        {
            // Window snap bottom
            return Direction.BOTTOM;
        }

        return null;
    }

    private void snapOff()
    {
        _stage.setWidth(_prevSize.getWidth());
        _stage.setHeight(_prevSize.getHeight());
        _scene.setSnapped(false);
    }

    /**
     * Limits the mouse to move within the visual bounds of the screen.
     */
    private void limitMouseMovement(Rectangle2D screen, MouseEvent event)
    {
        if (event.getScreenX() >= screen.getMaxX() || event.getScreenY() >= screen.getMaxY())
        {
            Robot robot = new Robot();
            double x = Math.min(event.getScreenX(), screen.getMaxX()) - 1;
            double y = Math.min(event.getScreenY(), screen.getMaxY()) - 1;
            robot.mouseMove(x, y);
        }
    }

    /**
     * Determines whether the window is allowed to snap vertically while resizing.
     */
    private boolean isLegalVerticalResizeSnap(Rectangle2D screen, VDirection vDirection, MouseEvent event)
    {
        double upperBoundary = screen.getMinY() + _scene.getAeroSnapAllowance();
        double lowerBoundary = screen.getMaxY() - _scene.getAeroSnapAllowance();
        return _stage.getY() <= upperBoundary && vDirection.equals(VDirection.TOP) || event.getScreenY() >= lowerBoundary && vDirection.equals(VDirection.BOTTOM);
    }

    private void updatePrevSizeAndPos()
    {
        _prevSize.setWidth(_stage.getWidth());
        _prevSize.setHeight(_stage.getHeight());
        _prevPos.setX(_stage.getX());
        _prevPos.setY(_stage.getY());
    }

    private void revertToPreviousSizeAndPos()
    {
        _stage.setWidth(_prevSize.getWidth());
        _stage.setHeight(_prevSize.getHeight());
        _stage.setX(_prevPos.getX());
        _stage.setY(_prevPos.getY());
        _wasSnapped.set(false);
    }

    private void revertToPreviousSizeAndPosClamped(Rectangle2D screen)
    {
        Double maxWidth = Math.min(screen.getWidth(), _stage.getMaxWidth());
        Double maxHeight = Math.min(screen.getHeight(), _stage.getMaxHeight());

        _stage.setWidth(clamp(_prevSize.getWidth(), _stage.getMinWidth(), maxWidth));
        _stage.setHeight(clamp(_prevSize.getHeight(), _stage.getMinHeight(), maxHeight));
        _stage.setX(clamp(_prevPos.getX(), screen.getMinX(), screen.getMaxX()));
        _stage.setY(clamp(_prevPos.getY(), screen.getMinY(), screen.getMaxY()));
    }

    private ObservableList<Screen> getScreensIntersectingHalf()
    {
        return Screen.getScreensForRectangle(_stage.getX(), _stage.getY(), _stage.getWidth() / 2, _stage.getHeight() / 2);
    }

    private ObservableList<Screen> getScreensIntersectingFull()
    {
        return Screen.getScreensForRectangle(_stage.getX(), _stage.getY(), _stage.getWidth(), _stage.getHeight());
    }
}
