import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by Szilank on 2016. 07. 03..
 */
public class Server extends AbstractVerticle {

	public static void main(String[] args) {
		Consumer<Vertx> runner = vertx1 -> {
			vertx1.deployVerticle(new Server());
		};
		Vertx vertx = Vertx.vertx();
		runner.accept(vertx);
	}

	@Override
	public void start() throws Exception {

		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create());

		// Handle static content
		router.route().handler(StaticHandler.create("src/angular"));

		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}

	public static void runExample(String exampleDir, String verticleID, VertxOptions options, DeploymentOptions deploymentOptions) {
		if (options == null) {
			// Default parameter
			options = new VertxOptions();
		}
		// Smart cwd detection

		// Based on the current directory (.) and the desired directory (exampleDir), we try to compute the vertx.cwd
		// directory:
		try {
			// We need to use the canonical file. Without the file name is .
			File current = new File(".").getCanonicalFile();
			if (exampleDir.startsWith(current.getName()) && !exampleDir.equals(current.getName())) {
				exampleDir = exampleDir.substring(current.getName().length() + 1);
			}
		} catch (IOException e) {
			// Ignore it.
		}

		System.setProperty("vertx.cwd", exampleDir);
		Consumer<Vertx> runner = vertx -> {
			try {
				if (deploymentOptions != null) {
					vertx.deployVerticle(verticleID, deploymentOptions);
				} else {
					vertx.deployVerticle(verticleID);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		};
		if (options.isClustered()) {
			Vertx.clusteredVertx(options, res -> {
				if (res.succeeded()) {
					Vertx vertx = res.result();
					runner.accept(vertx);
				} else {
					res.cause().printStackTrace();
				}
			});
		} else {
			Vertx vertx = Vertx.vertx(options);
			runner.accept(vertx);
		}
	}
}
