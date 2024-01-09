package widget;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class widget extends JFrame {

	private static final long serialVersionUID = 1L;
	private final String apiKey = "08793ec6c63c4f22835640326e";
	private final String apiUrl = "https://api.checkwx.com/metar/LRIA/decoded";
	private static String icao;
	// presiunea atmosferica
	private static double mbPress;

	// nori: tip, altitudine plafonului
	private static String type;
	private static Object alt;

	// dewpoint
	private static long dew_temp;

	// temperature
	private static long air_temp;

	// wind, dir, speed;
	private static long degrees;
	private static long speed;

	// date&time
	private static String date;
	private JFrame frmMetarWidget;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					widget window = new widget();
					window.frmMetarWidget.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private String fetchWeatherData() {
		try {
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("X-API-Key", apiKey);

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();
			connection.disconnect();

			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error fetching weather data";
		}
	}

	public widget() {
		initialize();
	}

	private void initialize() {

		frmMetarWidget = new JFrame();
		frmMetarWidget.setTitle("METAR widget");
		frmMetarWidget.setBounds(100, 100, 630, 300);
		frmMetarWidget.getContentPane().setLayout(null);

		JButton btn = new JButton("Refresh");
		btn.setBounds(218, 227, 89, 23);
		frmMetarWidget.getContentPane().add(btn);

		JLabel title = new JLabel("METAR ");
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setBounds(201, 11, 128, 14);

		JLabel presLbl = new JLabel("Atmospheric pressure:");
		presLbl.setHorizontalAlignment(SwingConstants.LEFT);
		presLbl.setBounds(336, 116, 219, 37);
		frmMetarWidget.getContentPane().add(presLbl);

		JLabel timeLbl = new JLabel();
		timeLbl.setHorizontalAlignment(SwingConstants.LEFT);
		timeLbl.setText("Date & Time:");
		timeLbl.setBounds(10, 49, 297, 37);
		frmMetarWidget.getContentPane().add(timeLbl);

		JLabel windLbl = new JLabel("Wind direction & speed:");
		windLbl.setHorizontalAlignment(SwingConstants.LEFT);
		windLbl.setBounds(10, 109, 297, 50);
		frmMetarWidget.getContentPane().add(windLbl);

		JLabel dewLbl = new JLabel("Dew point: ");
		dewLbl.setHorizontalAlignment(SwingConstants.LEFT);
		dewLbl.setBounds(10, 166, 297, 37);
		frmMetarWidget.getContentPane().add(dewLbl);

		JLabel cloudsLbl = new JLabel("Clouds:");

		cloudsLbl.setHorizontalAlignment(SwingConstants.LEFT);
		cloudsLbl.setBounds(336, 166, 286, 37);
		frmMetarWidget.getContentPane().add(cloudsLbl);

		JLabel tempLbl = new JLabel("Air temperature:");
		tempLbl.setHorizontalAlignment(SwingConstants.LEFT);
		tempLbl.setBounds(336, 49, 219, 37);
		frmMetarWidget.getContentPane().add(tempLbl);

		frmMetarWidget.getContentPane().add(title);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String weatherData = fetchWeatherData();
				// test string used in order to not send useless requests
				//String weatherData ="{"results":1,"data":[{"icao":"LRBC","barometer":{"hg":29.85,"hpa":1011.0,"kpa":101.08,"mb":1010.84},"clouds":[{"base_feet_agl":5500,"base_meters_agl":1676,"code":"SCT","text":"Scattered","feet":5500,"meters":1676}],"dewpoint":{"celsius":1,"fahrenheit":34},"elevation":{"feet":600.0,"meters":183.0},"flight_category":"VFR","humidity":{"percent":54},"observed":"2024-01-05T10:30:00","station":{"geometry":{"coordinates":[26.910299,46.5219],"type":"Point"},"location":"Bacau, Romania","name":"Bacau Airport","type":"Airport"},"temperature":{"celsius":10,"fahrenheit":50},"raw_text":"LRBC 051030Z 02006KT 340V070 9999 SCT055 10/01 Q1011","visibility":{"miles":"Greater than 6","miles_float":6.0,"meters":"9,700","meters_float":9700.0},"wind":{"degrees":20,"speed_kph":11,"speed_kts":6,"speed_mph":7,"speed_mps":3}}]}";
				JSONParser parser = new JSONParser();

				try {
					JSONObject json = (JSONObject) parser.parse(weatherData);
					JSONArray data = (JSONArray) json.get("data");

					JSONObject firstDataObject = (JSONObject) data.get(0);
					// icao tag
					icao = (String) firstDataObject.get("icao");
					title.setText("METAR " + icao);

					// atm pressure
					JSONObject barometerObject = (JSONObject) firstDataObject.get("barometer");
					mbPress = (double) barometerObject.get("mb");
					presLbl.setText("Atmospheric pressure: " + mbPress + " mb");

					// clouds: type, altitude
					JSONArray cloudsArray = (JSONArray) firstDataObject.get("clouds");
					JSONObject cloudsObject = (JSONObject) cloudsArray.get(0);
					type = (String) cloudsObject.get("text");
					if((Object)cloudsObject.get("meters")==null){
						alt=(String)cloudsObject.get("code");
						cloudsLbl.setText("Clouds: " + type + "  "+alt);
						}
					else{
						alt = (long) cloudsObject.get("meters");
						cloudsLbl.setText("Clouds: " + type + " altitude of " + alt + " meters");
						}

					// dewpoint: temp
					JSONObject dewObject = (JSONObject) firstDataObject.get("dewpoint");
					dew_temp = (long) dewObject.get("celsius");
					dewLbl.setText("Dew point: " + dew_temp + "°C");

					// air temp
					JSONObject tempObject = (JSONObject) firstDataObject.get("temperature");
					air_temp = (long) tempObject.get("celsius");
					tempLbl.setText("Air temperature: " + air_temp);

					// wind: degrees, speed
					JSONObject windObject = (JSONObject) firstDataObject.get("wind");
					degrees = (long) windObject.get("degrees");
					speed = (long) windObject.get("speed_mps");
					windLbl.setText("Wind direction & speed: " + degrees + " ~ " + speed + " mps");

					// date&time
					date = (String) firstDataObject.get("observed");

					timeLbl.setText("Observing moment: " + date);

				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				System.out.println(weatherData);
			}
		});

		frmMetarWidget.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}
