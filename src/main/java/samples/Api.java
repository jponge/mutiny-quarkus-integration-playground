package samples;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.mutiny.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Duration;

@Path("/")
public class Api {

    private static final Logger logger = LoggerFactory.getLogger(Api.class);

    private WebClient webClient;

    @Inject
    Vertx vertx;

    @PostConstruct
    void init() {
        webClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultPort(443)
                .setDefaultHost("api.chucknorris.io")
                .setSsl(true));
    }

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        logger.info("hello");
        return "Hello!";
    }

    @GET
    @Path("async-hello")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> asyncHello() {
        logger.info("async-hello");
        return Uni.createFrom().item("Hello!")
                .onItem().delayIt().by(Duration.ofSeconds(5))
                .onItem().invoke(item -> logger.info("Delayed item: {}", item));
    }

    @GET
    @Path("async-request")
    public Uni<String> asyncRequest() {
        logger.info("async-request");
        return webClient.get("/jokes/random")
                .timeout(5000)
                .putHeader("Accept", "application/json")
                .as(BodyCodec.jsonObject())
                .send()
                .onItem().transform(response -> response.body().getString("value"))
                .onFailure().recoverWithItem("Chuck Norris is sleeping");
    }

    @GET
    @Path("log")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> log() {
        logger.info("log");
        return Uni.createFrom().item("Hello!")
                .log("step-1")
                .onItem().delayIt().by(Duration.ofSeconds(5))
                .onItem().transform(String::toUpperCase)
                .log("step-2");
    }

    @GET
    @Path("stream-log")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> streamLog() {
        logger.info("stream-log");
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                .onItem().transform(n -> "tick #" + n)
                .log("stream");
    }

    @GET
    @Path("dont-block")
    @Produces(MediaType.TEXT_PLAIN)
    public String dontBlock() {
        logger.info("dont-block");
        String woops = Uni.createFrom().item("Woops")
                .onItem().delayIt().by(Duration.ofSeconds(5))
                .await().indefinitely();
        return woops;
    }

    @GET
    @Path("can-block")
    @Produces(MediaType.TEXT_PLAIN)
    @Blocking
    public String canBlock() {
        logger.info("can-block");
        String woops = Uni.createFrom().item("Woops")
                .onItem().delayIt().by(Duration.ofSeconds(5))
                .await().indefinitely();
        return woops;
    }
}