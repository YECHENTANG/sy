package uk.ac.bris.cs.scotlandyard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import impl.org.controlsfx.tools.rectangle.change.NewChangeStrategy;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.Utils;
import uk.ac.bris.cs.scotlandyard.ui.controller.LocalGameController;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Main entry point
 */
public final class Main {


	// prevents JPMS related issues
	public static final class JFXApp extends Application {
		public LocalGameController controller;

		@Override public void start(Stage stage) {
			Thread.currentThread().setUncaughtExceptionHandler(
					(thread, throwable) -> Utils.handleFatalException(throwable));
			controller = LocalGameController.newGame(Utils.setupResources(), stage);
		}

		@Override
		public void stop() throws Exception {
			controller.onApplicationStop();
		}
	}

	public static void main(String[] args) { JFXApp.launch(JFXApp.class, args); }

}
