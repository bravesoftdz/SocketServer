package net.learn2develop.socketserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;



import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView textView1;
	
	static String SERVER_IP;
	static final int SERVER_PORT = 5000;
	
	Handler handler = new Handler();
	ServerSocket serverSocket;
	
	TableLayout tbLayout;

	//---get the local IPv4 address---
	public String getLocalIpv4Address() {
		try {
			for (Enumeration<NetworkInterface> networkInterfaceEnum = NetworkInterface
					.getNetworkInterfaces(); networkInterfaceEnum
					.hasMoreElements();) {
				NetworkInterface networkInterface = networkInterfaceEnum
						.nextElement();
				for (Enumeration<InetAddress> ipAddressEnum = networkInterface
						.getInetAddresses(); ipAddressEnum.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) ipAddressEnum
							.nextElement();
					// ---check that it is not a loopback address and
					// it is IPv4---
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(inetAddress
									.getHostAddress())) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("IPAddress", ex.toString());
		}
		return null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//---make the TextView scrollable---
		textView1 = (TextView) findViewById(R.id.textView1);
		textView1.setMovementMethod(new ScrollingMovementMethod());
		tbLayout = (TableLayout) findViewById(R.id.tbLayout);
		
	}
	
	public class ServerThread implements Runnable {
		public void run() {
			try {
				if (SERVER_IP != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							textView1.setText(textView1.getText()
									+ "Server listening on IP: " + SERVER_IP
									+ "\n");
						}
					});

					//---create an instance of the server socket---
					serverSocket = new ServerSocket(SERVER_PORT);

					while (true) {
						//---wait for incoming clients---
						Socket client = serverSocket.accept();

						//---the above code is a blocking call;
						// i.e. it will block until a client connects---

						//---client has connected---
						handler.post(new Runnable() {
							@Override
							public void run() {
								textView1.setText(textView1.getText()
										+ "Connected to client." + "\n");
							}
						});
						String connect = "Connected to Server"+ "\n";
						OutputStream outputStreamFirst = 
								client.getOutputStream();
						outputStreamFirst.write(connect.getBytes());

						try {
							//---get an InputStream object to read from the
							// socket---
							BufferedReader br = new BufferedReader(
									new InputStreamReader(
											client.getInputStream()));

							OutputStream outputStream = 
									client.getOutputStream();

							//---read all incoming data terminated with a \n
							// char---
							String line = null;
							final StringBuffer sb = new StringBuffer();
							while ((line = br.readLine()) != null) {
								final String strReceived = line;

								//---send whatever you received back to the
								// client---
								String s = line + "\n";
								outputStream.write(s.getBytes());
								sb.append(line);
								handler.post(new Runnable() {
									@Override
									public void run() {
										textView1.setText(textView1.getText()
												+ strReceived + "\n");
									}
								});
							
							}
							
							handler.post(new Runnable() {
								@Override
								public void run() {
									addRecievedDataToView(sb.toString());
									
								}
							});

							//---client has disconnected from the server---
							handler.post(new Runnable() {
								@Override
								public void run() {
									textView1.setText(textView1.getText()
											+ "Client disconnected." + "\n");
								}
							});
							
						} catch (Exception e) {
							final String error = e.getLocalizedMessage();
							handler.post(new Runnable() {
								@Override
								public void run() {
									textView1.setText(textView1.getText()
											+ error);
								}
							});							
						}
					}
				} else {
					handler.post(new Runnable() {
						@Override
						public void run() {
							textView1.setText(textView1.getText()
									+ "No internet connection on device."
									+ "\n");
						}
					});
				}
			} catch (Exception e) {
				final String error = e.getLocalizedMessage();
				handler.post(new Runnable() {
					@Override
					public void run() {
						textView1.setText(textView1.getText() + error + "\n");
					}
				});
			}
			
			handler.post(new Runnable() {
				@Override
				public void run() {
					textView1.setText(textView1.getText() + "\n" + "Server exited"
							+ "\n");
				}
			});
		}
	}

	
	
	
	
	@Override
	protected void onStart() {
		super.onStart();
		//---get the IP address of itself---
		SERVER_IP = getLocalIpv4Address();

		//---start the server---
		Thread serverThread = new Thread(new ServerThread());
		serverThread.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		try {			
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void addRecievedDataToView(String recievedData){
		
		String dataWillBeSplitted = recievedData;
		TextView tvNo = new TextView(getApplicationContext());
		TextView tvID = new TextView(getApplicationContext());
		TextView tvName = new TextView(getApplicationContext());
		TextView tvPrice = new TextView(getApplicationContext());
		TextView tvQty = new TextView(getApplicationContext());
		TableRow tr = new TableRow(getApplicationContext());
		
		for(int i=0; i<getSplittedData(dataWillBeSplitted).length; i++){
			String[] split = getSplittedOrder(dataWillBeSplitted);
			tvNo.setText(String.valueOf(i+1));
			tvID.setText(split[0].toString());
			tvName.setText(split[1].toString());
			tvPrice.setText(split[2].toString());
			tvQty.setText(split[3].toString());
			tr.addView(tvNo);
			tr.addView(tvID);
			tr.addView(tvName);
			tr.addView(tvPrice);
			tr.addView(tvQty);
			tbLayout.addView(tr);
		}
	
	}
	
	private String[] getSplittedData(String unSplitData){
		String[] split = unSplitData.split("/");
		return split;
	}
	
	private String[] getSplittedOrder(String unSplitData){
		String[] split = unSplitData.split("#");
		return split;
	}
}