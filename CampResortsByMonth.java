import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL; 

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.PrintWriter;

public class CampResortsByMonth {
	private static String requestedMonth = "2023-07"; //Format goes YYYY-MM
	private static String reqUrl = "https://www.recreation.gov/api/permits/249991/availability/month?start_date=" + requestedMonth + "-01T00:00:00.000Z";

	public static void main(String[] args) throws IOException, InterruptedException {
		int i = 1; //Tracks the amount of time a POST request is made
		while(true) {
			System.out.println("Post Requests Made: " + i);
			sendPOST();
			//Thread.sleep(21600000);  //requests every 6 hours
			//Thread.sleep(7200000);     //requests every 2 hours
			Thread.sleep(5400000);     //requests every 1.5 hours
			i++;

		}

	}

	public static void sendGET() throws IOException {

		URL u = new URL(reqUrl); //Creates a URL Object to use for connection
		HttpURLConnection con = (HttpURLConnection) u.openConnection(); //Creates connection with URL object


		if(con.getResponseCode()==HttpURLConnection.HTTP_OK) { //Connection is successful

			//Reads input for all information received from the request
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();


			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String json = response.toString(); //Holds the request result information in string format


			/* NOTE: The Java built-in JSON package doesn't work for
			 * this situation, so manual substring-ing was done.
			 * */


			int ind = json.indexOf(requestedMonth); //Gets information from the first day of the request month


			while(ind != -1 && ind <= json.length()/2) { //Cut the json length in half because it repeats itself

				int firstDigitIndex = json.indexOf("remaining", ind); //Gets remaining slots for current day
				String remaining = json.substring(firstDigitIndex+11, firstDigitIndex+12); //Gets the first digit

				if(Character.isDigit(json.charAt(firstDigitIndex+12))) { //Checks if this is a number greater than 9
					remaining = remaining + json.charAt(firstDigitIndex+12);
				}

				if(!remaining.equals("0")) { //If it's not zero, add it as relevant information

					try {Integer.parseInt(remaining);} catch(Exception e){ //Makes sure remaining is actually a number

						ind = json.indexOf(requestedMonth, ind+1);  //Moves to the next day 
						continue;

					}

					String date = json.substring(ind, ind+10); //Gets entire date
					System.out.println(date);

				} 

				ind = json.indexOf(requestedMonth, ind+1); //Moves to the next date

			}

		} else { //Connection failed

			System.out.println("GET REQUEST FAILED. Status Code: " + con.getResponseCode());

		}

	}

	public static void sendPOST() throws IOException {

		URL u = new URL(reqUrl); //Creates a URL Object to use for connection
		HttpURLConnection con = (HttpURLConnection) u.openConnection(); //Creates connection with URL object
		String res = ""; //Holds all relevant information to send to the receiver



		if(con.getResponseCode()==HttpURLConnection.HTTP_OK) { //Connection is successful

			//Reads input for all information received from the request
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();


			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String json = response.toString(); //Holds the request result information in string format


			/* NOTE: The Java built-in JSON package doesn't work for
			 * this situation, so manual substring-ing was done.
			 * */


			int ind = json.indexOf(requestedMonth); //Gets information from the first day of the request month


			while(ind != -1 && ind <= json.length()/2) { //Cut the json length in half because it repeats itself

				int firstDigitIndex = json.indexOf("remaining", ind); //Gets remaining slots for current day
				String remaining = json.substring(firstDigitIndex+11, firstDigitIndex+12); //Gets the first digit

				if(Character.isDigit(json.charAt(firstDigitIndex+12))) { //Checks if this is a number greater than 9
					remaining = remaining + json.charAt(firstDigitIndex+12);
				}

				if(!remaining.equals("0")) { //If it's not zero, add it as relevant information

					try {Integer.parseInt(remaining);} catch(Exception e){ //Makes sure remaining is actually a number

						ind = json.indexOf(requestedMonth, ind+1);  //Moves to the next day 
						continue;

					}

					String date = json.substring(ind, ind+10); //Gets entire date

					if(date.substring(8).equals("13") || date.substring(8).equals("14") || date.substring(8).equals("15")) 					
						res += json.substring(ind, ind+10) + "   " + json.substring(ind+35, ind+46) + remaining + "\n";

				} 

				ind = json.indexOf(requestedMonth, ind+1); //Moves to the next date

			}

		} else { //Connection failed

			System.out.println("POST REQUEST FAILED. Status Code: " + con.getResponseCode());

		}



		if(res.length()==0) return; //If not relevant info, don't continue to avoid duplicate information
		String payloadInfo = "name=Kevin+You&email=kevin.you2940%40gmail.com&message=" + res + "&sub=Send+Message";



		//Opens lastReceivedDates.txt file that holds the previously sent information to make sure no
		//duplicate information is sent again to avoid unnecessary action.
		File lastReceivedDatesFile = new File("src/lastReceivedDates.txt");
		FileWriter datesWriter = new FileWriter(lastReceivedDatesFile.getAbsolutePath(), true);
		Scanner fileReader = new Scanner(lastReceivedDatesFile);
		String lastDatesStr = ""; //Holds the previous file contents in String format



		while(fileReader.hasNextLine()) {

			lastDatesStr += fileReader.nextLine() + "\n"; //Appends lastDatesStr

		}


		if (lastDatesStr.equals(res)) { //Checks if the information is a duplicate

			datesWriter.close();
			fileReader.close();
			return;

		} else {

			//Deletes the current files contents and replaces it with the current file contents
			PrintWriter writer = new PrintWriter(lastReceivedDatesFile.getAbsolutePath());
			writer.print("");
			writer.close();		
			datesWriter.write(res);

		}

		datesWriter.close();
		fileReader.close();


		//Opens a connection to a third-party email sender
		URL formspree = new URL("https://formspree.io/f/mgebgkrl"); //CHANGE THIS LINK
		HttpURLConnection form = (HttpURLConnection) formspree.openConnection();

		form.setRequestMethod("POST");
		form.setDoOutput(true); //Allows OutputStream to be sent out
		form.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); //IMPORTANT: Information sent should be in URL format



		//Holds all of the information to send out 
		OutputStream o = form.getOutputStream();
		o.write(payloadInfo.getBytes());
		o.flush();
		o.close();
		form.connect(); //Sends the information
		form.getContentLength();

	}

}