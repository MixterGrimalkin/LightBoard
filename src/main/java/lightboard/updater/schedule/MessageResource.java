package lightboard.updater.schedule;

import lightboard.scene.SceneManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("message")
public class MessageResource {

    private static MessageUpdater updater;

    private static Integer scene = null;

    public static void bindUpdater(MessageUpdater updater) {
        MessageResource.updater = updater;
    }

    public static void bindScene(int sceneNumber) { scene = sceneNumber; }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void postMessage(String message) {
        if ( updater!=null ) {
            updater.postMessage(message);
        }
        if ( scene!=null ) {
            SceneManager.loadScene(scene);
        }
    }


}