import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

public class cache {
    private static HashMap<String, Image> data = new HashMap<>();
    public static Image getCache(String in) throws IOException{
        if(data.containsKey(in)){
            return data.get(in);
        } else {
            data.put(in, new Image(Path.of(in)));
            return data.get(in);
        }
    }
}
