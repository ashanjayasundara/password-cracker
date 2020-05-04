package codec;

import com.google.gson.Gson;
import org.joda.time.DateTime;
import utils.TimeUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author ashan on 2020-05-03
 */
public class DynamicMsg implements Serializable {
    public String _msgType;


    public String toString(){
        return new Gson().toJson(this);
    }

    public String get_msgType() {
        return _msgType;
    }

    public void set_msgType(String _msgType) {
        this._msgType = _msgType;
    }
}
