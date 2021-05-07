package no.met.jtimeseries.marinogram;

        import org.json.JSONObject;
        import org.json.JSONTokener;

        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;

public class MarinogramAPIHandler {

    public void fetchAsJson(URL url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Met - jtimeseries");
            conn.connect();

            //Getting the response code
            int responsecode = conn.getResponseCode();
            if (responsecode < 200 && responsecode > 300) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {
                InputStreamReader reader = new InputStreamReader(url.openStream());
                JSONTokener tokener = new JSONTokener(reader);
                JSONObject root = new JSONObject(tokener);
                System.out.println("Id  : " + root.getLong("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
