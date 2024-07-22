package com.Bentaii.BorderlessFX.scene;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.Bentaii.BorderlessFX.geometry.Delta;
import com.Bentaii.BorderlessFX.geometry.Dimension;
import com.Bentaii.BorderlessFX.geometry.Direction;
import com.Bentaii.BorderlessFX.window.TransparentWindow;
import com.Bentaii.BorderlessFX.window.TransparentWindow.TransparentWindowStyle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * <p>
 * This class is a wrapper around a {@link Scene} that provides an undecorated scene with
 * the ability to move, resize, minimize, maximize and Aero Snap/Quarter Tile the stage.
 * <p>
 * Usage:
 * <pre>
 * {@code
 *     BorderlessScene scene = new BorderlessScene(primaryStage, StageStyle.UNDECORATED, root, 250, 250);
 *     primaryStage.setScene(scene);
 *     scene.setMoveControl(topBar);
 *     primaryStage.setTitle("Draggable and Undecorated JavaFX Window");
 *           primaryStage.show();
 * }
 * </pre>
 */
public class DockableScene extends Scene
{

    private static final String DEFAULT_STYLE_CLASS = "borderless-scene";
    private static final HashSet<StageStyle> ALLOWED_STAGE_STYLES = new HashSet<>(Arrays.asList(StageStyle.UNDECORATED, StageStyle.TRANSPARENT));
    private final HashSet<Direction> _disabledDirections = new HashSet<>();
    private final DockableSceneController _controller;
    private final Stage _stage;
    private BooleanProperty _maximized;
    private BooleanProperty _minimized;
    private BooleanProperty _resizable;
    private BooleanProperty _aeroSnap;
    private DoubleProperty _aeroSnapAllowance;
    private DoubleProperty _aeroSnapCornerAllowance;
    private BooleanProperty _doubleClickMaximizable;
    private BooleanProperty _verticalResizeSnap;
    private BooleanProperty _preventMouseOverTaskbar;
    private ReadOnlyBooleanWrapper _snapped;

    /**
     * Creates a new {@link DockableScene} with the given parameters.
     *
     * @param stage      The {@link Stage} that this scene is attached to.
     * @param stageStyle The {@link StageStyle} of the stage.
     *                   <p>
     *                   Must be {@link StageStyle#UNDECORATED} or
     *                   {@link StageStyle#TRANSPARENT}.
     *                   If  a different {@link StageStyle} is passed, the
     *                   {@link StageStyle#TRANSPARENT}
     *                   will be used.
     *                   </p>
     * @param parent     The parent {@link Parent} of the scene.
     *                   This is the same as the root parameter in {@link Scene#Scene(Parent)}.
     */
    public DockableScene(Stage stage, StageStyle stageStyle, Parent parent)
    {
        super(parent);
        _controller = new DockableSceneController(stage, this);

        setRoot(loadRoot(_controller));
        setContent(parent);

        // Defaults
        getRoot().getStyleClass().add(DEFAULT_STYLE_CLASS);
        setMaximized(false);
        setAeroSnap(true);
        resizableProperty().bindBidirectional(stage.resizableProperty());
        setDoubleClickMaximizable(true);
        setVerticalResizeSnap(true);
        _disabledDirections.add(Direction.BOTTOM);
        stage.initStyle(ALLOWED_STAGE_STYLES.contains(stageStyle) ? stageStyle : StageStyle.TRANSPARENT);

        // Load default CSS
        getStylesheets().add(requireNonNull(DockableScene.class.getResource("/css/styles.css")).toExternalForm());

        this._stage = stage;
    }

    /**
     * Creates a new {@link DockableScene} with the given parameters.
     *
     * @param stage      The {@link Stage} that this scene is attached to.
     * @param stageStyle The {@link StageStyle} of the stage.
     *                   <p>
     *                   Must be {@link StageStyle#UNDECORATED} or
     *                   {@link StageStyle#TRANSPARENT}.
     *                   If  a different {@link StageStyle} is passed, the
     *                   {@link StageStyle#TRANSPARENT}
     *                   will be used.
     *                   </p>
     * @param parent     The parent {@link Parent} of the scene.
     *                   This is the same as the root parameter in {@link Scene#Scene(Parent)}.
     * @param width      The width of the scene.
     * @param height     The height of the scene.
     */
    public DockableScene(Stage stage, StageStyle stageStyle, Parent parent, double width, double height)
    {
        this(stage, stageStyle, parent);
        stage.setWidth(width);
        stage.setHeight(height);
    }

    /**
     * Creates a new {@link DockableScene} with the given parameters.
     *
     * @param stage      The {@link Stage} that this scene is attached to.
     * @param stageStyle The {@link StageStyle} of the stage.
     *                   <p>
     *                   Must be {@link StageStyle#UNDECORATED} or
     *                   {@link StageStyle#TRANSPARENT}.
     *                   If  a different {@link StageStyle} is passed, the
     *                   {@link StageStyle#TRANSPARENT}
     *                   will be used.
     *                   </p>
     * @param parent     The parent {@link Parent} of the scene.
     *                   This is the same as the root parameter in {@link Scene#Scene(Parent)}.
     * @param width      The width of the scene.
     * @param height     The height of the scene.
     * @param paint      The paint {@link Paint} to define the background fill of the
     *                   {@link Scene}.
     */
    public DockableScene(Stage stage, StageStyle stageStyle, Parent parent, double width, double height, Paint paint)
    {
        this(stage, stageStyle, parent);
        stage.setWidth(width);
        stage.setHeight(height);
        setFill(paint);
    }

    /**
     * Creates a new {@link DockableScene} with the given parameters.
     *
     * @param stage      The {@link Stage} that this scene is attached to.
     * @param stageStyle The {@link StageStyle} of the stage.
     *                   <p>
     *                   Must be {@link StageStyle#UNDECORATED} or
     *                   {@link StageStyle#TRANSPARENT}.
     *                   If  a different {@link StageStyle} is passed, the
     *                   {@link StageStyle#TRANSPARENT}
     *                   will be used.
     *                   </p>
     * @param parent     The parent {@link Parent} of the scene.
     *                   This is the same as the root parameter in {@link Scene#Scene(Parent)}.
     * @param paint      The paint {@link Paint} to define the background fill of the
     *                   {@link Scene}.
     */
    public DockableScene(Stage stage, StageStyle stageStyle, Parent parent, Paint paint)
    {
        this(stage, stageStyle, parent);
        setFill(paint);
    }

    public BooleanProperty maximizedProperty()
    {
        if (_maximized == null)
        {
            _maximized = new SimpleBooleanProperty()
            {
                @Override
                protected void invalidated()
                {
                    _controller.maximize();
                }

                @Override
                public Object getBean()
                {
                    return DockableScene.this;
                }

                @Override
                public String getName()
                {
                    return "maximized";
                }
            };
        }

        return _maximized;
    }

    public BooleanProperty minimizedProperty()
    {
        if (_minimized == null)
        {
            _minimized = new SimpleBooleanProperty()
            {
                @Override
                protected void invalidated()
                {
                    _controller.minimize();
                }

                @Override
                public Object getBean()
                {
                    return DockableScene.this;
                }

                @Override
                public String getName()
                {
                    return "minimized";
                }
            };
        }

        return _minimized;
    }

    public BooleanProperty resizableProperty()
    {
        if (_resizable == null)
        {
            _resizable = new SimpleBooleanProperty(true);
        }

        return _resizable;
    }

    public BooleanProperty aeroSnapProperty()
    {
        if (_aeroSnap == null)
        {
            _aeroSnap = new SimpleBooleanProperty()
            {
                @Override
                protected void invalidated()
                {
                    if (aeroSnapProperty().get() && _controller.getTransparentWindow() == null)
                    {
                        _controller.createTransparentWindow();
                    }
                    else if (aeroSnapProperty().not().get())
                    {
                        _controller.destroyTransparentWindow();
                    }
                }

                @Override
                public Object getBean()
                {
                    return DockableScene.this;
                }

                @Override
                public String getName()
                {
                    return "aeroSnap";
                }
            };
        }

        return _aeroSnap;
    }

    public DoubleProperty aeroSnapAllowanceProperty()
    {
        if (_aeroSnapAllowance == null)
        {
            _aeroSnapAllowance = new SimpleDoubleProperty(25);
        }

        return _aeroSnapAllowance;
    }

    public DoubleProperty aeroSnapCornerAllowanceProperty()
    {
        if (_aeroSnapCornerAllowance == null)
        {
            _aeroSnapCornerAllowance = new SimpleDoubleProperty(50);
        }

        return _aeroSnapCornerAllowance;
    }

    public BooleanProperty doubleClickMaximizableProperty()
    {
        if (_doubleClickMaximizable == null)
        {
            _doubleClickMaximizable = new SimpleBooleanProperty(true);
        }

        return _doubleClickMaximizable;
    }

    public BooleanProperty verticalResizeSnapProperty()
    {
        if (_verticalResizeSnap == null)
        {
            _verticalResizeSnap = new SimpleBooleanProperty(true);
        }

        return _verticalResizeSnap;
    }

    public BooleanProperty preventMouseOverTaskbarProperty()
    {
        if (_preventMouseOverTaskbar == null)
        {
            _preventMouseOverTaskbar = new SimpleBooleanProperty(true);
        }

        return _preventMouseOverTaskbar;
    }

    public ReadOnlyBooleanWrapper snappedProperty()
    {
        if (_snapped == null)
        {
            _snapped = new ReadOnlyBooleanWrapper(false);
        }

        return _snapped;
    }

    /**
     * Sets the content of the scene.
     *
     * @param parent The parent {@link Parent} of the scene.
     */
    public void setContent(Parent parent)
    {
        ((AnchorPane) getRoot()).getChildren().set(0, parent);
        AnchorPane.setLeftAnchor(parent, 0.0D);
        AnchorPane.setTopAnchor(parent, 0.0D);
        AnchorPane.setRightAnchor(parent, 0.0D);
        AnchorPane.setBottomAnchor(parent, 0.0D);
    }

    /**
     * Set a node that can be pressed and dragged to move the stage.
     *
     * @param node The node.
     */
    public void setMoveControl(Node node)
    {
        _controller.setMoveControl(node);
    }

    /**
     * Determines whether the stage is maximized or not.
     *
     * @return {@code boolean} - true if maximized otherwise false.
     */
    public boolean getMaximized()
    {
        return _maximized != null && _maximized.get();
    }

    /**
     * Sets whether the stage is maximized or not.
     *
     * @param value true to maximize, false to unmaximize.
     */
    public void setMaximized(boolean value)
    {
        maximizedProperty().set(value);
    }

    /**
     * Determines whether the stage is minimized or not.
     *
     * @return {@code boolean} - true if minimized otherwise false.
     */
    public boolean isMinimized()
    {
        return _minimized != null && _minimized.get();
    }

    /**
     * Sets whether the stage is minimized or not.
     *
     * @param value true to minimize, false to unminimize.
     */
    public void setMinimized(boolean value)
    {
        minimizedProperty().set(value);
    }

    /**
     * Determines whether the stage is resizable.
     *
     * @return {@code boolean} - true if resizable otherwise false.
     */
    public boolean getResizable()
    {
        return _resizable == null || _resizable.get();
    }

    /**
     * Sets whether the scene is resizable.
     *
     * @param value true to enable, false to disable.
     */
    public void setResizable(boolean value)
    {
        resizableProperty().set(value);
    }

    /**
     * Determines whether aero snapping/quarter tiling is enabled.
     *
     * @return {@code boolean} - true if enabled otherwise false.
     */
    public boolean getAeroSnap()
    {
        return _aeroSnap == null || _aeroSnap.get();
    }

    /**
     * Sets whether aero snap/quarter tiling is enabled.
     * <p>
     * Aero snap/quarter tiling is enabled by default.
     * When set to false, the transparent window is destroyed.
     * </p>
     *
     * @param value true to enable, false to disable.
     */
    public void setAeroSnap(boolean value)
    {
        aeroSnapProperty().set(value);
    }

    /**
     * Returns the amount of space needed from the edge to trigger aero snap/quarter tiling.
     *
     * @return {@code Double}
     */
    public Double getAeroSnapAllowance()
    {
        return _aeroSnapAllowance == null ? 10 : _aeroSnapAllowance.get();
    }

    /**
     * Sets the amount of space needed from the edge to trigger aero snap/quarter tiling.
     * <p>
     * The default value is 10.
     * </p>
     *
     * @param value The amount of space needed from the edge to trigger aero snap/quarter
     *              tiling.
     */
    public void setAeroSnapAllowance(Double value)
    {
        aeroSnapAllowanceProperty().set(value);
    }

    /**
     * Returns the amount of space needed from the edge to trigger aero snap/quarter tiling
     * for corners explicitly.
     *
     * @return {@code Double}
     */
    public Double getAeroSnapCornerAllowance()
    {
        return _aeroSnapCornerAllowance == null ? 50 : _aeroSnapCornerAllowance.get();
    }

    /**
     * Sets the amount of space needed from the edge to trigger aero snap/quarter tiling
     * for corners explicitly.
     * <p>
     * The default value is 50.
     * </p>
     *
     * @param value The amount of space needed from the edge to trigger aero snap/quarter
     *              tiling for corners explicitly.
     */
    public void setAeroSnapCornerAllowance(Double value)
    {
        aeroSnapCornerAllowanceProperty().set(value);
    }

    /**
     * Determines whether the stage can be (un)maximized by double-clicking the move control.
     *
     * @return {@code boolean} - true if enabled otherwise false.
     */
    public boolean getDoubleClickMaximizable()
    {
        return _doubleClickMaximizable == null || _doubleClickMaximizable.get();
    }

    /**
     * Sets whether the stage can be (un)maximized by double-clicking the move control.
     * <p>
     * Double-click maximization is enabled by default.
     * </p>
     *
     * @param value true to enable, false to disable.
     */
    public void setDoubleClickMaximizable(boolean value)
    {
        doubleClickMaximizableProperty().set(value);
    }

    /**
     * Determines whether the stage will snap while vertically resizing.
     *
     * @return {@code boolean} - true if enabled otherwise false.
     */
    public boolean getVerticalResizeSnap()
    {
        return _verticalResizeSnap == null || _verticalResizeSnap.get();
    }

    /**
     * Sets whether the stage will snap while vertically resizing.
     * <p>
     * Vertical resize snapping is enabled by default.
     * </p>
     *
     * @param value true to enable, false to disable.
     */
    public void setVerticalResizeSnap(boolean value)
    {
        verticalResizeSnapProperty().set(value);
    }

    /**
     * Determines whether the stage can be moved past (over) the taskbar.
     *
     * @return {@code boolean} - true if enabled otherwise false.
     */
    public boolean getPreventMouseOverTaskbar()
    {
        return _preventMouseOverTaskbar == null || _preventMouseOverTaskbar.get();
    }

    /**
     * Sets whether the stage can be moved past (over) the taskbar.
     *
     * @param value true to enable, false to disable.
     */
    public void setPreventMouseOverTaskbar(boolean value)
    {
        preventMouseOverTaskbarProperty().set(value);
    }

    /**
     * Determines whether the stage is snapped.
     *
     * @return {@code boolean} - true if snapped otherwise false.
     */
    public boolean getSnapped()
    {
        return _snapped != null && _snapped.get();
    }

    protected void setSnapped(boolean value)
    {
        snappedProperty().set(value);
    }

    /**
     * Toggle to maximize/unmaximize the application.
     * <p>
     * Always called with the negation of the current state
     * </p>
     */
    public void maximizeStage()
    {
        setMaximized(maximizedProperty().not().get());
    }

    /**
     * Toggle to minimize/unminimize the application.
     * <p>
     * Always called with the negation of the current state
     * </p>
     */
    public void minimizeStage()
    {
        setMinimized(minimizedProperty().not().get());
    }

    /**
     * Returns a HashSet containing/accepting instances of {@link Direction} of which their
     * window snap is disabled.
     *
     * @return The hashset, an instance of {@code HashSet<Direction>} with the
     * current disabled directions.
     */
    public Set<Direction> getDisabledDirections()
    {
        return _disabledDirections;
    }

    /**
     * Set a specific direction as disabled for aero snap/quarter tiling.
     *
     * @param direction The direction to disable.
     */
    public void disableDirection(Direction direction)
    {
        _disabledDirections.add(direction);
    }

    /**
     * Gets the size of the stage.
     *
     * @return The size of this stage, an instance of {@link Dimension} with the
     * current width and height.
     */
    public Dimension getStageSize()
    {
        if (_controller._prevSize.getWidth() == 0)
        {
            _controller._prevSize.setWidth(_stage.getWidth());
        }
        if (_controller._prevSize.getHeight() == 0)
        {
            _controller._prevSize.setHeight(_stage.getHeight());
        }

        return _controller._prevSize;
    }

    /**
     * Gets the position of the stage.
     *
     * @return The position of this stage, an instance of {@link Delta} with the
     * current x and y coordinates.
     */
    public Delta getStagePosition()
    {
        if (_controller._prevPos.getX() == null)
        {
            _controller._prevPos.setX(_stage.getX());
        }
        if (_controller._prevPos.getY() == null)
        {
            _controller._prevPos.setY(_stage.getY());
        }

        return _controller._prevPos;
    }

    /**
     * Apply a pre-defined style to the transparent window.
     * <p>
     * The default value is {@link TransparentWindowStyle#MINIMALISTIC}.
     * <br>
     * The style can be changed at any time.
     * </p>
     *
     * @param style The style of the transparent window, instance of
     *              {@link TransparentWindowStyle}.
     */
    public void setTransparentWindowStyle(TransparentWindowStyle style)
    {
        TransparentWindow transparentWindow = getTransparentWindow();

        if (transparentWindow != null)
        {
            transparentWindow.setStyle(style);
        }
    }

    /**
     * The transparent window which allows the library to have aero snap controls
     *
     * @return The transparent window, instance of {@link TransparentWindow} extends
     * {@link StackPane}.
     */
    public TransparentWindow getTransparentWindow()
    {
        return _controller.getTransparentWindow();
    }

    /**
     * Loads the debug CSS for the borderless scene and transparent window.
     */
    public void debug()
    {
        String cssUrl = requireNonNull(DockableScene.class.getResource("/css/debug.css")).toExternalForm();
        // Our default CSS will always be at index 0
        getStylesheets().set(0, cssUrl);
        getTransparentWindow().getStylesheets().set(0, cssUrl);
    }

    private AnchorPane loadRoot(DockableSceneController controller)
    {
        return controller.load();
    }
}
