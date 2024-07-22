package com.Bentaii.BorderlessFX.window;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A transparent window.
 * <p>
 * This class is used to create a transparent window. It is used as a visual feedback for
 * aero snap/quarter tiling.
 * </p>
 */
public class TransparentWindow extends StackPane
{

    public static final String DEFAULT_STYLE_CLASS = "transparent-window";
    private Stage stage = new Stage();

    public TransparentWindow()
    {
        // Defaults
        this.setId("transparentWindow");
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        getStylesheets().add(TransparentWindow.class.getResource("/css/styles.css")
            .toExternalForm());
        setStyle(TransparentWindowStyle.MINIMALISTIC);

        // Stage
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.NONE);
        stage.setScene(new Scene(this, Color.TRANSPARENT));
    }

    /**
     * @return The transparent window's stage, instance of {@link Stage}
     */
    public Stage getStage()
    {
        return stage;
    }

    /**
     * Sets the style of the transparent window.
     *
     * @param style The style to set, instance of {@link TransparentWindowStyle}
     */
    public void setStyle(TransparentWindowStyle style)
    {
        // Remove previous style
        getStyleClass().removeIf(s -> !s.equals(DEFAULT_STYLE_CLASS));

        if (style.equals(TransparentWindowStyle.NONE))
        {
            return;
        }

        getStyleClass().add(style.getStyleClass());
    }

    /**
     * Show the transparent window's stage
     */
    public void show()
    {
        if (!stage.isShowing())
        {
            stage.show();
        }
        else
        {
            stage.requestFocus();
        }
    }

    /**
     * Close the transparent window's stage
     */
    public void close()
    {
        stage.close();
    }

    /**
     * Removes transparent window's stage reference and closes the stage, releasing any memory
     */
    public void destroy()
    {
        stage.close();
        stage = null;
    }

    public enum ConsoleTab
    {

        CONSOLE,
        SPEECH_RECOGNITION
    }

    /**
     * An enum representing the different styles of a transparent window.
     */
    public enum TransparentWindowStyle
    {

        WHITE_NOISE("white-noise"),
        GLASS_GLARE("glass-glare"),
        GLASS_TRANSPARENT("glass-transparent"),
        GNOME("gnome"),
        MINIMALISTIC("minimalistic"),
        NONE(null);

        private final String styleClass;

        TransparentWindowStyle(String styleClass)
        {
            this.styleClass = styleClass;
        }

        public String getStyleClass()
        {
            return styleClass;
        }
    }
}
