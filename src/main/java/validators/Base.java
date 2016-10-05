package validators;

import org.json.JSONObject;

import java.util.List;

public interface Base {
    List<String> validate(JSONObject data, boolean insert);
}