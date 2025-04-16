package plugin.Abstractions;

import java.io.InputStream;

public interface Follower {

    public void SendData(InputStream file, String fileName) throws Exception;
}
