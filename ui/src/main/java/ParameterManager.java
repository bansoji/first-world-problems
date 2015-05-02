import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Gavin Tam on 15/04/15.
 */
public class ParameterManager {

    private Map<String,String> params = new HashMap<>();

    public boolean updateParams(String paramFile)
    {
        PropertiesConfiguration props;
        try {
            props = new PropertiesConfiguration(paramFile);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        //update properties
        for (String key: params.keySet()) {
            props.setProperty(key, params.get(key));
        }
        try {
            props.save();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Properties getProperties(String paramFile) {
        FileInputStream in;
        try {
            in = new FileInputStream(paramFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        //get current properties
        Properties props = new Properties();
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public int getNumParams() {
        return params.size();
    }

    public Map<String,String> getParams() {
        return params;
    }

    public void put(String key, String value) {
        params.put(key,value);
    }

    public void clear() {
        params.clear();
    }
}
