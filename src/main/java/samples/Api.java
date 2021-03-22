package samples;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Duration;

@Path("/")
public class Api {

    private static final Logger logger = LoggerFactory.getLogger(Api.class);

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