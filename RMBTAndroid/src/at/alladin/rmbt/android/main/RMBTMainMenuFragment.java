/*******************************************************************************
 * Copyright 2013-2016 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.android.main;

import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import at.alladin.openrmbt.android.R;
import at.alladin.rmbt.android.main.InfoCollector.InfoCollectorType;
import at.alladin.rmbt.android.main.InfoCollector.OnInformationChangedListener;
import at.alladin.rmbt.android.main.titlepage.IpCheckRunnable;
import at.alladin.rmbt.android.main.titlepage.IpCheckRunnable.OnIpCheckFinishedListener;
import at.alladin.rmbt.android.main.titlepage.NetworkWatcherRunnable;
import at.alladin.rmbt.android.main.titlepage.NetworkWatcherRunnable.OnActiveNetworkChangeListener;
import at.alladin.rmbt.android.util.CheckIpTask.IpVersionType;
import at.alladin.rmbt.android.util.ConfigHelper;
import at.alladin.rmbt.android.util.Helperfunctions;
import at.alladin.rmbt.android.util.InformationCollector;
import at.alladin.rmbt.android.util.net.InterfaceTrafficGatherer;
import at.alladin.rmbt.android.util.net.InterfaceTrafficGatherer.TrafficClassificationEnum;
import at.alladin.rmbt.android.util.net.NetworkFamilyEnum;
import at.alladin.rmbt.android.util.net.NetworkInfoCollector;
import at.alladin.rmbt.android.util.net.NetworkInfoCollector.CaptivePortalStatusEnum;
import at.alladin.rmbt.android.util.net.NetworkInfoCollector.OnNetworkInfoChangedListener;
import at.alladin.rmbt.android.util.net.NetworkUtil;
import at.alladin.rmbt.android.util.net.NetworkUtil.MinMax;

/**
 * 
 * @author
 * 
 */
public class RMBTMainMenuFragment extends Fragment
{
	public static enum OverlayType {
		IPV4(R.string.title_screen_ipv4_overlay, R.id.title_page_ipv4_button), 
		IPV6(R.string.title_screen_ipv6_overlay, R.id.title_page_ipv6_button), 
		TRAFFIC(R.string.title_screen_traffic, R.id.title_page_traffic_button), 
		LOCATION(R.string.result_page_title_map, R.id.title_page_location_button);
		
		protected int resId;
		protected int buttonId;
		
		OverlayType(int resId, int buttonId) {
			this.resId = resId;
			this.buttonId = buttonId;
		}
		
		public int getResourceId() {
			return resId;
		}
		
		public int getButtonId() {
			return buttonId;
		}
	}
	
	public final static String BUNDLE_INFO_LAST_ANTENNA_IMAGE = "last_antenna_image";
	public final static String BUNDLE_INFO_CAPTIVE_PORTAL_STATUS = "captive_portal_status";
	public final static String BUNDLE_INFO_COLLECTOR = "info";
	public final static int BACKGROUND_TRAFFIC_MEASUREMENT_TIME = 1000;
	public final static int INFORMATION_COLLECTOR_TIME = 1000;
	
    private InformationCollector informationCollector;
    
    /**
	 * 
	 */
    private static final String DEBUG_TAG = "RMBTMainMenuFragment";
    
    private TextView startButtonText;
    private TextView infoNetwork;
    private TextView infoNetworkLabel;
    private TextView infoNetworkType;
    private TextView infoSignalStrength;
    private TextView infoSignalStrengthExtra;
    //private TextView infoIp;
    //private TextView infoTraffic;
    private View ipv4Button;
    private View ipv6Button;
    private View locationButton;
    private View trafficButton;
    private View startButton;
    
    private ImageView ipv4View;
    private ImageView ipv6View;
    private ImageView locationView;
    private ImageView antennaView;
    private ImageView ulSpeedView;
    private ImageView dlSpeedView;
    private ImageView captivePortalWarning;
    public boolean runInfoRunnable = false;
    public Handler infoHandler = new Handler();

	private InfoCollector infoCollector = InfoCollector.getInstance();
	private InterfaceTrafficGatherer interfaceTrafficGatherer;
	private Animation pulseAnimation;
	
	private NetworkWatcherRunnable networkWatcherRunnable;
	private IpCheckRunnable ipv4CheckRunnable;
	private IpCheckRunnable ipv6CheckRunnable;
    
	private Integer lastSignal;
    private ListView infoOverlayList;
    private RelativeLayout infoOverlay;
    private TextView infoOverlayTitle;
    private Map<OverlayType, InfoArrayAdapter> infoValueListAdapterMap = new HashMap<RMBTMainMenuFragment.OverlayType, InfoArrayAdapter>();
    
    //developer code info
    private TextView developerCodeInfoView;
    
    /**
     * number format used for ul/dl traffic
     */
    DecimalFormat speedFormat;

    /**
	 * 
	 */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    	Log.i(DEBUG_TAG, "onCreate");
        pulseAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.pulse);
        interfaceTrafficGatherer = new InterfaceTrafficGatherer();
        informationCollector = new InformationCollector(getActivity(), false, false);
        speedFormat = new DecimalFormat(String.format("@@ %s", getActivity().getResources().getString(R.string.test_mbps)));
        
        ipv4CheckRunnable = new IpCheckRunnable(getActivity(), IpVersionType.V4, true);
        ipv4CheckRunnable.addListener(new OnIpCheckFinishedListener() {

			@Override
			public void onFinish(InetAddress privAddress,
					InetAddress pubAddress, InetAddress oldPrivAddress,
					InetAddress oldPubAddress) {
				if ((privAddress != null && !privAddress.equals(oldPrivAddress)) ||
						(pubAddress != null && !pubAddress.equals(oldPubAddress)) ||
						(privAddress == null && oldPrivAddress != null || pubAddress == null && oldPubAddress != null)) {
					Log.d(DEBUG_TAG, "new ipv4, run captive portal check...");
					((RMBTMainActivity) getActivity()).getNetworkInfoCollector().checkForCaptivePortal();
				}
			}
		});
        
        ipv6CheckRunnable = new IpCheckRunnable(getActivity(), IpVersionType.V6, true);
        ipv6CheckRunnable.addListener(new OnIpCheckFinishedListener() {

			@Override
			public void onFinish(InetAddress privAddress,
					InetAddress pubAddress, InetAddress oldPrivAddress,
					InetAddress oldPubAddress) {
				if ((privAddress != null && !privAddress.equals(oldPrivAddress)) ||
						(pubAddress != null && !pubAddress.equals(oldPubAddress)) ||
						(privAddress == null && oldPrivAddress != null || pubAddress == null && oldPubAddress != null)) {
					Log.d(DEBUG_TAG, "new ipv6, run captive portal check...");
					((RMBTMainActivity) getActivity()).getNetworkInfoCollector().checkForCaptivePortal();
				}
			}
		});
        
        networkWatcherRunnable = new NetworkWatcherRunnable(getActivity());
        networkWatcherRunnable.addListener(new OnActiveNetworkChangeListener() {
			
			@Override
			public void onChange(Set<InetAddress> oldInterfaceSet,
					Set<InetAddress> newInterfaceSet, String oldSsid, String newSsid) {
				ipv4CheckRunnable.clearIps();
				ipv6CheckRunnable.clearIps();
				((RMBTMainActivity) getActivity()).getNetworkInfoCollector().checkForCaptivePortal();
			}
		});        
    }
    
    /**
     * 
     * @return
     */
    public RMBTMainActivity getMainActivity()
    {
        return (RMBTMainActivity) getActivity();
    }
    
    /**
	 * 
	 */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        
        final View view = inflater.inflate(R.layout.title_screen, container, false);
        return createView(view, inflater, savedInstanceState);
    }

    /**
     * 
     * @param view
     * @param inflater
     * @param savedInstanceState
     * @return
     */
    private View createView(View view, LayoutInflater  inflater, Bundle savedInstanceState) {
        
        startButton = view.findViewById(R.id.title_page_start_button);
        startButtonText = (TextView) view.findViewById(R.id.title_page_start_button_text);
        
        if (startButton != null)
        {
           startButton.setOnClickListener(new OnClickListener()
               { public void onClick(View v) { ((RMBTMainActivity) getActivity()).checkPermissionsAndStartTest();} });
        }
                
        final View startButtonLayout = view.findViewById(R.id.title_page_start_button_layout);
        if (startButtonLayout != null)
        {
            final Animation delayedPulseAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.delayed_pulse);
            delayedPulseAnimation.setAnimationListener(new Animation.AnimationListener()
            {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                
                @Override
                public void onAnimationEnd(Animation animation)
                {
                    
                    startButtonLayout.startAnimation(animation);
                }
            });
            startButtonLayout.startAnimation(delayedPulseAnimation);
        }
        
        //infoLocation = (TextView) view.findViewById(R.id.info_gps_location);
        //infoLocation.setVisibility(View.GONE);
        infoNetwork = (TextView) view.findViewById(R.id.info_network_name);
        setViewVisibility(infoNetwork, View.GONE);
        
        infoNetworkLabel = (TextView) view.findViewById(R.id.info_network_name_label);
        setViewVisibility(infoNetworkLabel, View.GONE);

        infoNetworkType = (TextView) view.findViewById(R.id.info_network_type);
        setViewVisibility(infoNetworkType, View.GONE);
        
        infoSignalStrength = (TextView) view.findViewById(R.id.info_signal_strength);
        setViewVisibility(infoSignalStrength, View.GONE);

        infoSignalStrengthExtra = (TextView) view.findViewById(R.id.info_signal_strength_extra);
        setViewVisibility(infoSignalStrengthExtra, View.INVISIBLE);

        locationView = (ImageView) view.findViewById(R.id.location_image);
        setViewVisibility(locationView, View.INVISIBLE);

        locationButton = view.findViewById(R.id.title_page_location_button);
        if (locationButton != null) {
        	locationButton.setOnClickListener(detailShowOnClickListener);
        }
        
        ipv4View = (ImageView) view.findViewById(R.id.ipv4_status);
        ipv4Button = view.findViewById(R.id.title_page_ipv4_button);
        if (ipv4Button != null) {
        	ipv4Button.setOnClickListener(detailShowOnClickListener);
        }
        
        ipv6View = (ImageView) view.findViewById(R.id.ipv6_status);
        ipv6Button = view.findViewById(R.id.title_page_ipv6_button);
        if (ipv6Button != null) {
        	ipv6Button.setOnClickListener(detailShowOnClickListener);
        }

        trafficButton = view.findViewById(R.id.title_page_traffic_button);
        if (trafficButton != null) {
        	trafficButton.setOnClickListener(detailShowOnClickListener);
        }
        
        infoOverlayList = (ListView) view.findViewById(R.id.info_overlay_list);
        if (infoOverlayList != null) {
        	infoValueListAdapterMap.put(OverlayType.TRAFFIC, new InfoArrayAdapter(getActivity(), OverlayType.TRAFFIC,
        			InfoOverlayEnum.DL_TRAFFIC,InfoOverlayEnum.UL_TRAFFIC));
        	
        	infoValueListAdapterMap.put(OverlayType.IPV4, new InfoArrayAdapter(getActivity(), OverlayType.IPV4,
        			InfoOverlayEnum.IPV4, InfoOverlayEnum.IPV4_PUB));
        	
        	infoValueListAdapterMap.put(OverlayType.IPV6, new InfoArrayAdapter(getActivity(), OverlayType.IPV6, 
        			InfoOverlayEnum.IPV6, InfoOverlayEnum.IPV6_PUB));
        	
        	infoValueListAdapterMap.put(OverlayType.LOCATION, new InfoArrayAdapter(getActivity(), OverlayType.LOCATION,
        			InfoOverlayEnum.LOCATION));
        }

        infoOverlayTitle = (TextView) view.findViewById(R.id.info_overlay_title);
        
        infoOverlay = (RelativeLayout) view.findViewById(R.id.info_overlay);
        if (infoOverlay != null) {
        	if (infoOverlay.getVisibility() == View.GONE) {
        		infoOverlay.setOnClickListener(detailHideOnClickListener);
        		infoOverlayList.setOnItemClickListener(detailHideOnItemClickListener);
        	}
        }

        captivePortalWarning = (ImageView) view.findViewById(R.id.captive_portal_image);
        
        antennaView = (ImageView) view.findViewById(R.id.antenne_image);
        if (antennaView != null) {
        	//antennaView.setOnClickListener(detailShowOnClickListener);
        	
        	if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_INFO_LAST_ANTENNA_IMAGE)) {
        		int antennaImageId = savedInstanceState.getInt(BUNDLE_INFO_LAST_ANTENNA_IMAGE);
        		antennaView.setImageResource(antennaImageId);
        		antennaView.setTag(antennaImageId);
        	}
        }
        ulSpeedView = (ImageView) view.findViewById(R.id.traffic_ul_image);
        dlSpeedView = (ImageView) view.findViewById(R.id.traffic_dl_image);
        
        developerCodeInfoView = (TextView) view.findViewById(R.id.developerCodeInfo);
        
        return view;
    }
    
    @Override
    public void onPause() {
    	Log.i(DEBUG_TAG, "onPause");
    	super.onPause();
        infoCollector.removeListener(onInfoChangedListener);
        NetworkInfoCollector.getInstance().removeOnNetworkInfoChangedListener(onNetworkChangedListener);
    	runInfoRunnable = false;
        //infoLocation.setVisibility(View.GONE);
    	setViewVisibility(infoNetwork, View.GONE);
        setViewVisibility(infoNetworkLabel, View.GONE);
        setViewVisibility(infoNetworkType, View.GONE);
        setViewVisibility(infoSignalStrength, View.GONE);
        setViewVisibility(infoSignalStrengthExtra, View.INVISIBLE);
        setViewVisibility(locationView, View.INVISIBLE);        
        
        if (informationCollector != null) {
            informationCollector.unload();
        }
        infoHandler.removeCallbacks(infoRunnable);
        infoHandler.removeCallbacks(interfaceTrafficRunnable);
        
    }
    
    @Override
    public void onResume() {
    	Log.i(DEBUG_TAG, "onResume");
    	super.onResume();

    	if (startButtonText != null) {
        	startButtonText.setText(ConfigHelper.isLoopMode(getActivity()) ? R.string.menu_button_loop : R.string.menu_button_start);
        }
    	
        infoCollector.addListener(onInfoChangedListener);
        NetworkInfoCollector.getInstance().addOnNetworkChangedListener(onNetworkChangedListener);
        if (informationCollector != null) {
            runInfoRunnable = true;
            informationCollector.init();
        }
        infoHandler.post(infoRunnable);
        infoHandler.post(interfaceTrafficRunnable);
        infoCollector.refresh();        
        
        getActivity().getActionBar().show();
        ((RMBTMainActivity) getActivity()).setLockNavigationDrawer(false);
    }

    public Runnable interfaceTrafficRunnable = new Runnable() {
		
		@Override
		public void run() {
			if (interfaceTrafficGatherer != null) {
				interfaceTrafficGatherer.run();
				final long rxRate = interfaceTrafficGatherer.getRxRate();
				final long txRate = interfaceTrafficGatherer.getTxRate();
				TrafficClassificationEnum rxTrafficClass = TrafficClassificationEnum.classify(rxRate);
				TrafficClassificationEnum txTrafficClass = TrafficClassificationEnum.classify(txRate);
				
				infoCollector.setUlTraffic(txTrafficClass);
				infoCollector.setDlTraffic(rxTrafficClass);
			}
			
			if (infoOverlay != null && infoOverlay.getVisibility() == View.VISIBLE) {
				((BaseAdapter) infoOverlayList.getAdapter()).notifyDataSetChanged();
			}
			
			infoHandler.postDelayed(interfaceTrafficRunnable, BACKGROUND_TRAFFIC_MEASUREMENT_TIME);
		}
	};
    
    /**
     * 
     */
    public Runnable infoRunnable = new Runnable() {
		@Override
		public void run() {				
			if (informationCollector != null) {
				String signalTerm = getActivity().getString(R.string.term_signal);
				
				Location loc = informationCollector.getLocationInfo();
		        infoCollector.setLocation(loc);
				
				int lastNetworkType = informationCollector.getNetwork();
                String lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
				//System.out.println("lastNetworkType: " + lastNetworkType + ", lastNetworkTypeString: " + lastNetworkTypeString);
				
				Integer curSignal = lastSignal;
				if (!"UNKNOWN".equals(lastNetworkTypeString)) {
					setViewVisibility(infoSignalStrength, View.VISIBLE);
					
	                Integer signal = informationCollector.getSignal();
	                if (!"BLUETOOTH".equals(lastNetworkTypeString) && !"ETHERNET".equals(lastNetworkTypeString)) {
	                	if (signal != null && signal > Integer.MIN_VALUE && signal < 0) {
		                	int signalType = informationCollector.getSignalType();
		                	curSignal = signal;

		                	if (signalType == InformationCollector.SINGAL_TYPE_RSRP) {
			                	infoSignalStrength.setText(getString(R.string.term_signal) + ": " + signal + " dBm");
			                	infoCollector.setSignal(signal);
			                	Integer signalRsrq = informationCollector.getSignalRsrq();
			                	if (signalRsrq != null) {
			    					if (infoSignalStrengthExtra.getVisibility() == View.INVISIBLE) {
			    						infoSignalStrengthExtra.setVisibility(View.VISIBLE);
			    					}
			    					
			    					infoCollector.setSignalRsrq(signalRsrq);
			    					infoSignalStrengthExtra.setText(getString(R.string.term_signal_quality) + ": " + signalRsrq + " dB");
			                	}
			                	else {
			                		infoSignalStrengthExtra.setVisibility(View.INVISIBLE);
			                	}
		                	}
		                	else {
		                		infoCollector.setSignal(curSignal);
		                		infoSignalStrength.setText(signalTerm + ": " + signal + " dBm");
		                		infoSignalStrengthExtra.setVisibility(View.INVISIBLE);
		                	}
	                	}
	                	else {
	                		curSignal = Integer.MIN_VALUE;
    						infoSignalStrength.setVisibility(View.INVISIBLE);
    						infoSignalStrengthExtra.setVisibility(View.INVISIBLE);
	                	}
	                }
				}
				
				if (infoNetworkType != null) {
					if (lastNetworkTypeString != null && !"UNKNOWN".equals(lastNetworkTypeString))  {

						if (infoNetwork != null) {
							if (infoNetwork.getVisibility() == View.GONE) {
								infoNetwork.setVisibility(View.VISIBLE);
								setViewVisibility(infoNetworkLabel, View.VISIBLE);
							}

							String networkName = informationCollector.getOperatorName();
							if (networkName != null && !"()".equals(networkName)) {
								infoCollector.setNetworkName(networkName);
								infoNetwork.setText(networkName);
							}
							else {
								infoCollector.setNetworkName(networkName);
								infoNetwork.setVisibility(View.GONE);
								setViewVisibility(infoNetworkLabel, View.GONE);
							}
						}

						if (antennaView != null && antennaView.getVisibility() != View.VISIBLE) {
							antennaView.setVisibility(View.VISIBLE);
						}

						lastSignal = curSignal;

	                	NetworkFamilyEnum networkFamily = NetworkFamilyEnum.getFamilyByNetworkId(lastNetworkTypeString);
	                	if (NetworkFamilyEnum.UNKNOWN.equals(networkFamily)) {
	                		infoNetworkType.setVisibility(View.GONE);
	                		infoCollector.setNetworkTypeString(lastNetworkTypeString);
	                		infoNetworkType.setText(lastNetworkTypeString);	
	                	}
	                	else {
	                		
	                		if (lastNetworkTypeString.equals(NetworkFamilyEnum.WLAN.getNetworkFamily())) {
	                			infoNetworkType.setVisibility(View.GONE);
	                		}
	                		else {
	                			infoNetworkType.setVisibility(View.VISIBLE);
	                			
		                		if (lastNetworkTypeString.equals(networkFamily.getNetworkFamily())) {
		                			infoCollector.setNetworkTypeString(lastNetworkTypeString);
			                		infoNetworkType.setText(lastNetworkTypeString);	                			
		                		}
		                		else {
		                			infoCollector.setNetworkTypeString(networkFamily.getNetworkFamily() + "/" + lastNetworkTypeString);
			                		infoNetworkType.setText(networkFamily.getNetworkFamily() + "/" + lastNetworkTypeString);
		                		}	                		
	                		}	                		
	                	}
	                }
	                else {
		                curSignal = Integer.MIN_VALUE;
		                lastSignal = curSignal;
//		                Log.d(DEBUG_TAG,"lastNetworkTypeString: " + lastNetworkTypeString);
		                infoCollector.setSignal(lastSignal);
		                infoCollector.setNetworkName(null);
						infoNetworkType.setVisibility(View.GONE);
						infoNetwork.setVisibility(View.GONE);
						setViewVisibility(infoNetworkLabel, View.GONE);
						setViewVisibility(infoSignalStrength, View.GONE);
						setViewVisibility(infoSignalStrengthExtra, View.INVISIBLE);
						if (antennaView != null && antennaView.getVisibility() != View.VISIBLE) {
							refreshAntennaImage(curSignal);
						}
	                }
				}
				else if (antennaView != null && antennaView.getVisibility() != View.VISIBLE) {
//					Log.d(DEBUG_TAG,"lastNetworkTypeString: " + lastNetworkTypeString);
                	curSignal = Integer.MIN_VALUE;
                	lastSignal = curSignal;
                	infoCollector.setSignal(lastSignal);
			        refreshAntennaImage(Integer.MIN_VALUE);
				}
				
				
				if (curSignal != null) {
					refreshAntennaImage(curSignal);
				}
				
				//infoCollector.setIpv4(ConfigHelper.getLastIp(getActivity())); 
				
				networkWatcherRunnable.run();
				
				ipv4CheckRunnable.run();
				ipv6CheckRunnable.run();

				infoCollector.refreshIpAndAntenna();
				infoCollector.dispatchInfoChangedEvent(InfoCollectorType.IPV4, null, infoCollector.getIpv4());
				infoCollector.dispatchInfoChangedEvent(InfoCollectorType.IPV6, null, infoCollector.getIpv6());
				
				final NetworkInfoCollector netInfo = ((RMBTMainActivity) getActivity()).getNetworkInfoCollector();
				if (netInfo != null) {
					infoCollector.setCaptivePortalFound(netInfo.getCaptivePortalStatus().equals(CaptivePortalStatusEnum.FOUND));
				}
				
//				if (netInfo != null) {
//					if (ConfigHelper.isIpPolling(getActivity())) {
//						netInfo.gatherIpInformation(true);
//					}
//					else {
//						if (NetworkInfoCollector.IP_METHOD == NetworkInfoCollector.IP_METHOD_NETWORKINTERFACE) {
//							netInfo.gatherInterfaceInformation(true);
//						}
//						else {
//							netInfo.gatherIpInformation(false);
//						}
//					}
//					
//					infoCollector.setHasControlServerConnection(netInfo.hasIpFromControlServer());
//					infoCollector.setCaptivePortalFound(netInfo.getCaptivePortalStatus().equals(CaptivePortalStatusEnum.FOUND));
//					infoCollector.refreshIpAndAntenna();
//					infoCollector.dispatchInfoChangedEvent(InfoCollectorType.IPV4, null, infoCollector.getIpv4());
//					infoCollector.dispatchInfoChangedEvent(InfoCollectorType.IPV6, null, infoCollector.getIpv6());
//					
//					netInfo.onNetworkChange(getActivity(), null);
//				}
				
				if (netInfo.getCaptivePortalStatus() == CaptivePortalStatusEnum.FOUND 
						|| netInfo.getCaptivePortalStatus() == CaptivePortalStatusEnum.NOT_FOUND) {
					setCaptivePortalStatus(netInfo.getCaptivePortalStatus() == CaptivePortalStatusEnum.FOUND);
				}
			}
			
			if (runInfoRunnable) { 
				informationCollector.reInit();
				infoHandler.postDelayed(infoRunnable, INFORMATION_COLLECTOR_TIME);	
			}
			
			if (developerCodeInfoView != null && ConfigHelper.isDevEnabled(getActivity())) {
				setViewText(developerCodeInfoView, getActivity().getString(R.string.title_screen_developer));
				setViewVisibility(developerCodeInfoView, View.VISIBLE);
			}
		}		
	};
	
	public void refreshAntennaImage(int signal) {
		if (antennaView.getVisibility() != View.VISIBLE) {
			antennaView.setVisibility(View.VISIBLE);
		}

		int antennaImageRes = getAntennaImageResourceId(signal);
		antennaView.setImageResource(antennaImageRes);
		antennaView.setTag(antennaImageRes);
	}
	
	/**
	 * 
	 * @param signal
	 * @return
	 */
	private int getAntennaImageResourceId(int signal) {
		int lastNetworkType = informationCollector.getNetwork();
		String lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
		
		if (lastNetworkType == TelephonyManager.NETWORK_TYPE_UNKNOWN) // || signal == Integer.MIN_VALUE)
		    return R.drawable.signal_no_connection;
		
		boolean wlan = "WLAN".equals(lastNetworkTypeString);
		
    	int signalType = informationCollector.getSignalType();
    	
    	double relativeSignal = -1d;
    	MinMax<Integer> signalBounds = NetworkUtil.getSignalStrengthBounds(signalType);
    	//System.out.println("SIGNAL STRENGTH TYPE: " + signalType + " -> " + signalBounds + " / SIGNAL: " + signal);
        if (! (signalBounds.min == Integer.MIN_VALUE || signalBounds.max == Integer.MAX_VALUE)) {
            relativeSignal = (double)(signal - signalBounds.min) / (double)(signalBounds.max - signalBounds.min);
        }
       
		//System.out.println("relativeSignal: " + relativeSignal + ", networkType: " + networkType + ", lastNetworkTypeString: " + lastNetworkTypeString);
		if (relativeSignal < 0.25d) {
			return (wlan ? R.drawable.signal_wlan_25 : R.drawable.signal_mobile_25);
		}
		else if (relativeSignal < 0.5d) {
			return (wlan ? R.drawable.signal_wlan_50 : R.drawable.signal_mobile_50);
		}
		else if (relativeSignal < 0.75d) {
			return (wlan ? R.drawable.signal_wlan_75 : R.drawable.signal_mobile_75);
		}
		else {
			return (wlan ? R.drawable.signal_wlan : R.drawable.signal_mobile);
		}
	}
	/**
	 * 
	 */
	private final OnInformationChangedListener onInfoChangedListener = new OnInformationChangedListener() {		
		@Override
		public void onInformationChanged(InfoCollectorType type, Object oldValue, Object newValue) {
			switch (type) {
			case LOCATION:
				if (locationView != null) {
					if (locationView.getVisibility() == View.INVISIBLE) {
						locationView.setVisibility(View.VISIBLE);
					}
					locationView.setImageResource(newValue != null ? R.drawable.ic_action_location_found : R.drawable.ic_action_location_off);
				}
				if (newValue == null) {
					infoValueListAdapterMap.get(OverlayType.LOCATION).removeElement(InfoOverlayEnum.LOCATION_ACCURACY);
					infoValueListAdapterMap.get(OverlayType.LOCATION).removeElement(InfoOverlayEnum.LOCATION_AGE);
					infoValueListAdapterMap.get(OverlayType.LOCATION).removeElement(InfoOverlayEnum.LOCATION_SOURCE);
					infoValueListAdapterMap.get(OverlayType.LOCATION).removeElement(InfoOverlayEnum.LOCATION_ALTITUDE);
				}
				else {
					infoValueListAdapterMap.get(OverlayType.LOCATION).addElement(InfoOverlayEnum.LOCATION_ACCURACY);
					infoValueListAdapterMap.get(OverlayType.LOCATION).addElement(InfoOverlayEnum.LOCATION_AGE);
					infoValueListAdapterMap.get(OverlayType.LOCATION).addElement(InfoOverlayEnum.LOCATION_SOURCE);
					infoValueListAdapterMap.get(OverlayType.LOCATION).addElement(InfoOverlayEnum.LOCATION_ALTITUDE);
				}
				break;
			case NETWORK_TYPE:
				if (antennaView != null) {
					//System.out.println("NETWORK_TYPE changed to: " + newValue);
				    Integer signal = informationCollector.getSignal();
				    refreshAntennaImage(signal != null ? signal : Integer.MIN_VALUE);
				}
				//no break; here!!
				//if the network type or the network family changes, the same label TextView is used
			case NETWORK_FAMILY:
				if (infoNetworkType != null && infoNetworkType.getVisibility() == View.VISIBLE) {
					infoNetworkType.startAnimation(pulseAnimation);
					//((RMBTMainActivity)getActivity()).checkIp();
				}
				break;
			case NETWORK_NAME:
				//reset all IPs on network change name:
				ipv4CheckRunnable.clearIps();
				ipv6CheckRunnable.clearIps();
				((RMBTMainActivity) getActivity()).getNetworkInfoCollector().checkForCaptivePortal();

				if (infoNetwork != null && infoNetwork.getVisibility() == View.VISIBLE) {
					if (newValue != null && !((String) newValue).trim().equals("()")) {
						Log.d(DEBUG_TAG, "networkName: " + newValue);
						infoNetwork.startAnimation(pulseAnimation);
					}
				}
				break;
			case SIGNAL_RSRQ:
				if (infoSignalStrengthExtra != null && infoSignalStrengthExtra.getVisibility() == View.VISIBLE) {
					if (oldValue != null && ((Integer)oldValue != 0)) {
						infoSignalStrengthExtra.startAnimation(pulseAnimation);
					}
				}
				break;
			case SIGNAL:
				if (antennaView != null && antennaView.getVisibility() == View.VISIBLE && newValue != null) {
					refreshAntennaImage((Integer) newValue);
				}
			
				if (type.equals(InfoCollectorType.SIGNAL) && infoSignalStrength != null && infoSignalStrength.getVisibility() == View.VISIBLE) {
					if (oldValue != null && ((Integer)oldValue != 0)) {
						infoSignalStrength.startAnimation(pulseAnimation);
					}
				}
				break;
			case IPV4:
			case IPV6:
				if (getActivity() != null) {
					NetworkInfoCollector netInfo = ((RMBTMainActivity) getActivity()).getNetworkInfoCollector();
					if (netInfo != null) {
						if (ipv4CheckRunnable.getPubAddress() != null) {
							infoValueListAdapterMap.get(OverlayType.IPV4).addElement(InfoOverlayEnum.IPV4_PUB);
						}
						else {
							infoValueListAdapterMap.get(OverlayType.IPV4).removeElement(InfoOverlayEnum.IPV4_PUB);
						}
						
						if (ipv6CheckRunnable.getPubAddress() != null) {
							infoValueListAdapterMap.get(OverlayType.IPV6).addElement(InfoOverlayEnum.IPV6_PUB);
						}
						else {
							infoValueListAdapterMap.get(OverlayType.IPV6).removeElement(InfoOverlayEnum.IPV6_PUB);
						}
						
						if (ipv4View != null) {
							ipv4View.setImageResource(ipv4CheckRunnable.getIpStatus(ipv6CheckRunnable).getResourceId());
						}
						if (ipv6View != null) {
							ipv6View.setImageResource(ipv6CheckRunnable.getIpStatus(ipv4CheckRunnable).getResourceId());
						}
					}
				}
				break;
			case UL_TRAFFIC:
				if (ulSpeedView != null) {
					TrafficClassificationEnum trafficEnum = (TrafficClassificationEnum) newValue;
					ulSpeedView.setImageResource(trafficEnum.getResId());
					ulSpeedView.setRotation(180f);
				}
				break;
			case DL_TRAFFIC:
				if (dlSpeedView != null) {
					TrafficClassificationEnum trafficEnum = (TrafficClassificationEnum) newValue;
					dlSpeedView.setImageResource(trafficEnum.getResId());
				}
				break;
			case CAPTIVE_PORTAL_STATUS:
				setCaptivePortalStatus((Boolean) newValue);
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * 
	 * @param hasCaptivePortal
	 */
	private void setCaptivePortalStatus(boolean hasCaptivePortal) {
		if (captivePortalWarning != null) {
			captivePortalWarning.setVisibility(hasCaptivePortal ? View.VISIBLE : View.GONE);

		}

		if (!hasCaptivePortal) {
			infoValueListAdapterMap.get(OverlayType.IPV4).removeElement(InfoOverlayEnum.CAPTIVE_PORTAL_STATUS);
			infoValueListAdapterMap.get(OverlayType.IPV6).removeElement(InfoOverlayEnum.CAPTIVE_PORTAL_STATUS);
		}
		else {
			infoValueListAdapterMap.get(OverlayType.IPV4).addElement(InfoOverlayEnum.CAPTIVE_PORTAL_STATUS);
			infoValueListAdapterMap.get(OverlayType.IPV6).addElement(InfoOverlayEnum.CAPTIVE_PORTAL_STATUS);
		}
	}

	/**
	 * 
	 */
	private final OnClickListener detailShowOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (infoOverlay != null) {
				switch (v.getId()) {
				case R.id.title_page_ipv4_button:
					infoOverlayTitle.setText(getResources().getText(OverlayType.IPV4.getResourceId()));
					infoOverlayList.setAdapter(infoValueListAdapterMap.get(OverlayType.IPV4));
					break;
				case R.id.title_page_ipv6_button:
					infoOverlayTitle.setText(getResources().getText(OverlayType.IPV6.getResourceId()));
					infoOverlayList.setAdapter(infoValueListAdapterMap.get(OverlayType.IPV6));
					break;
				case R.id.title_page_location_button:
					infoOverlayTitle.setText(getResources().getText(R.string.title_screen_info_overlay_location));
					infoOverlayList.setAdapter(infoValueListAdapterMap.get(OverlayType.LOCATION));
					break;
				case R.id.title_page_traffic_button:
				default:
					infoOverlayTitle.setText(getResources().getText(OverlayType.TRAFFIC.getResourceId()));
					infoOverlayList.setAdapter(infoValueListAdapterMap.get(OverlayType.TRAFFIC));
					break;
				}
				//System.out.println("SHOWING INFO OVERLAY");
				if (ipv4Button != null) {
					ipv4Button.setOnClickListener(detailHideOnClickListener);
				}
				if (ipv6Button != null) {
					ipv6Button.setOnClickListener(detailHideOnClickListener);
				}
				//antennaView.setOnClickListener(detailHideOnClickListener);
				trafficButton.setOnClickListener(detailHideOnClickListener);
				locationButton.setOnClickListener(detailHideOnClickListener);
				infoOverlay.setVisibility(View.VISIBLE);
				infoOverlay.bringToFront();
				infoOverlayList.invalidate();
			}
		}
	};
	
	/**
	 * 
	 */
	private final OnClickListener detailHideOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (infoOverlay != null) {
				if ((((InfoArrayAdapter) infoOverlayList.getAdapter()).getOverlayType().getButtonId() == v.getId()) ||
						v.getId() == R.id.info_overlay) {
					hideOverlayAndReenableOnClickListeners();
				}
				else {
					detailShowOnClickListener.onClick(v);
				}
			}			
		}
	};
	
	/**
	 * 
	 */
	OnItemClickListener detailHideOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			hideOverlayAndReenableOnClickListeners();
		}
	};
	
	/**
	 * 
	 */
	private void hideOverlayAndReenableOnClickListeners() {
		infoOverlay.setVisibility(View.GONE);
		if (ipv4Button != null) {
			ipv4Button.setOnClickListener(detailShowOnClickListener);
		}
		if (ipv6Button != null) {
			ipv6Button.setOnClickListener(detailShowOnClickListener);
		}
		//antennaView.setOnClickListener(detailShowOnClickListener);
		trafficButton.setOnClickListener(detailShowOnClickListener);
		locationButton.setOnClickListener(detailShowOnClickListener);							
	}
	
	/**
	 * 
	 */
	private OnNetworkInfoChangedListener onNetworkChangedListener = new OnNetworkInfoChangedListener() {

		@Override
		public void onChange(InfoFlagEnum infoFlag, Object newValue) {

			switch (infoFlag) {
			case NETWORK_CONNECTION_CHANGED:
				if ((Boolean) newValue) {
					if (startButton != null && startButtonText != null) {
						startButton.setAlpha(1f);
						startButton.setEnabled(true);
						startButtonText.setAlpha(1f);
					}
				}
				else {
					if (startButton != null && startButtonText != null) {
						startButton.setAlpha(0.3f);
						startButtonText.setAlpha(0.3f);
						startButton.setEnabled(false);
					}
				}
				break;
			default:
				if (infoCollector != null) {
					if (infoFlag == InfoFlagEnum.PRIVATE_IPV4_CHANGED || infoFlag == InfoFlagEnum.PRIVATE_IPV6_CHANGED) {
						if (NetworkInfoCollector.getInstance().hasConnectionFromAndroidApi()) {
							infoCollector.dispatchInfoChangedEvent(InfoCollectorType.SIGNAL, 0, infoCollector.getSignal());
							infoCollector.dispatchInfoChangedEvent(InfoCollectorType.SIGNAL_RSRQ, 0, infoCollector.getSignalRsrq());
						}
						else {
							infoCollector.setSignal(Integer.MIN_VALUE);
							infoCollector.setSignalRsrq(null);
						}
					}

					infoCollector.dispatchInfoChangedEvent(InfoCollectorType.IPV4, infoCollector.getIpv4(), newValue);
					infoCollector.dispatchInfoChangedEvent(InfoCollectorType.IPV6, infoCollector.getIpv6(), newValue);
				}						
			}
		}
    	
    };

	/**
	 * 
	 * @author lb
	 *
	 */
	private class InfoArrayAdapter extends ArrayAdapter<InfoOverlayEnum> {

		private List<InfoOverlayEnum> infoList;
		private final Activity context;
		private final OverlayType overlayType;
		
		class ViewHolder {
			 public TextView name;
			 public TextView value;
		}
		
		public InfoArrayAdapter(Activity context, OverlayType overlayType, InfoOverlayEnum... infoArray) {
			super(context, R.layout.test_result_detail_item);
			this.context = context;
			this.overlayType = overlayType;
			this.infoList = new ArrayList<RMBTMainMenuFragment.InfoOverlayEnum>();
			for (InfoOverlayEnum e : infoArray) {
				infoList.add(e);
			}
		}
		
		public OverlayType getOverlayType() {
			return overlayType;
		}
		
		public void removeElement(InfoOverlayEnum e) {
			if (this.infoList.contains(e)) {
				//System.out.println("removing element: " + e);
				this.infoList.remove(e);					
				notifyDataSetChanged();
			}
		}
		
		public void addElement(InfoOverlayEnum e) {
			addElement(e, Integer.MAX_VALUE);
		}
		
		public void addElement(InfoOverlayEnum e, int index) {
			if (!this.infoList.contains(e)) {
				//System.out.println("adding element: " + e);
				if (index > this.infoList.size()) {
					this.infoList.add(e);
				}
				else {
					this.infoList.add(index, e);	
				}
				notifyDataSetChanged();
			}
		}
		
		@Override
		public int getCount() {
			return (infoList != null ? infoList.size() : 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		    View rowView = convertView;
		    // reuse views
		    if (rowView == null) {
			      LayoutInflater inflater = context.getLayoutInflater();
			      rowView = inflater.inflate(R.layout.info_overlay_detail_item, null);
			      // configure view holder
			      ViewHolder viewHolder = new ViewHolder();
			      viewHolder.name = (TextView) rowView.findViewById(R.id.name);
			      viewHolder.value = (TextView) rowView.findViewById(R.id.value);
			      rowView.setTag(viewHolder);
		    }
		    
		    // fill data
		    NetworkInfoCollector netInfo = ((RMBTMainActivity) getActivity()).getNetworkInfoCollector();
		    
		    ViewHolder holder = (ViewHolder) rowView.getTag();
		    holder.name.setText(infoList.get(position).getTitle(context));
		    switch (infoList.get(position)) {
		    case DL_TRAFFIC:
		    	holder.value.setText(speedFormat.format(((double)interfaceTrafficGatherer.getRxRate() / 125000D)));
		    	break;
		    case UL_TRAFFIC:
		    	holder.value.setText(speedFormat.format(((double)interfaceTrafficGatherer.getTxRate() / 125000D)));
		    	break;
		    case LOCATION:
		    	String locationString = "";
		    	if (infoCollector.getLocation() != null) {
		    		locationString = Helperfunctions.getLocationString(getActivity(), getResources(), infoCollector.getLocation() ,0);
		    	}
		    	else {
		    		locationString = getResources().getString(R.string.not_available);
		    	}
	    		holder.value.setText(locationString);
		    	break;
		    case LOCATION_ACCURACY:
		    	locationString = "";
		    	if (infoCollector.getLocation() != null) { 
		    		final int satellites;
		    		if (infoCollector.getLocation().getExtras() != null)
		    		    satellites = infoCollector.getLocation().getExtras().getInt("satellites");
		    		else
		    		    satellites = 0;
                    locationString = Helperfunctions.convertLocationAccuracy(getResources(), 
		    				infoCollector.getLocation().hasAccuracy(), 
		    				infoCollector.getLocation().getAccuracy(),
		    				satellites);
		    	}
	    		holder.value.setText(locationString);		    	
		    	break;
		    case LOCATION_AGE:
		    	locationString = "";
		    	if (infoCollector.getLocation() != null) {
		    		locationString = Helperfunctions.convertLocationTime(infoCollector.getLocation().getTime());
		    	}
	    		holder.value.setText(locationString);		    	
		    	break;
		    case LOCATION_SOURCE:
		    	locationString = "";
		    	if (infoCollector.getLocation() != null) {
		    		locationString = Helperfunctions.convertLocationProvider(getResources(), infoCollector.getLocation().getProvider());
		    	}
	    		holder.value.setText(locationString);
		    	break;
		    case LOCATION_ALTITUDE:
		    	locationString = "";
		    	if (infoCollector.getLocation() != null) {
		    		if (infoCollector.getLocation().hasAltitude()) {
		    			locationString = Helperfunctions.convertLocationAltitude(getResources(), 
		    					infoCollector.getLocation().hasAltitude(), infoCollector.getLocation().getAltitude());
		    		}
		    		else {
		    			locationString = getActivity().getString(R.string.not_available);
		    		}
		    	}
	    		holder.value.setText(locationString);
		    	break;		    	
		    case IPV4:
		    	if (ipv4CheckRunnable.getPrivAddress() != null) {
			    	holder.value.setText(ipv4CheckRunnable.getPrivAddress().getHostAddress());
		    	}
		    	else {
		    		holder.name.setText("");
		    		holder.value.setText(getResources().getString(R.string.not_available));
		    	}
		    	break;
		    case IPV6:
		    	if (ipv6CheckRunnable.getPrivAddress() != null) {
			    	holder.value.setText(ipv6CheckRunnable.getPrivAddress().getHostAddress());
		    	}
		    	else {
		    		holder.name.setText("");
		    		holder.value.setText(getResources().getString(R.string.not_available));
		    	}
		    	break;
		    case IPV4_PUB:
		    	if (ipv4CheckRunnable.getPubAddress() != null) {
			    	holder.value.setText(ipv4CheckRunnable.getPubAddress().getHostAddress());
		    	}
		    	else {
		    		holder.value.setText(getResources().getString(R.string.not_available));
		    	}
		    	break;
		    case IPV6_PUB:
		    	if (ipv6CheckRunnable.getPubAddress() != null) {
			    	holder.value.setText(ipv6CheckRunnable.getPubAddress().getHostAddress());
		    	}
		    	else {
		    		holder.value.setText(getResources().getString(R.string.not_available));
		    	}
		    	break;
		    case CAPTIVE_PORTAL_STATUS:
		    	holder.value.setText(netInfo.getCaptivePortalStatus().getTitle(context));
		    	break;
		    case IS_LINK_LOCAL6:
		    	if (ipv6CheckRunnable.getPrivAddress() != null) {
		    		holder.value.setText("" + ipv6CheckRunnable.getPrivAddress().isLinkLocalAddress());
		    	}
		    	break;
		    case IS_LOOPBACK6:
		    	if (ipv6CheckRunnable.getPrivAddress() != null) {
		    		holder.value.setText("" + ipv6CheckRunnable.getPrivAddress().isLoopbackAddress());
		    	}
		    	break;
		    case IS_LOOPBACK4:
		    	if (ipv4CheckRunnable.getPrivAddress() != null) {
		    		holder.value.setText("" + ipv4CheckRunnable.getPrivAddress().isLoopbackAddress());
		    	}
		    	break;
		    default:
		    	break;
		    }
		    return rowView;
		    
		}
	}
	
	public static enum InfoOverlayEnum {
		IPV4(R.string.title_screen_info_overlay_ipv4_private),
		IS_LOOPBACK4(R.string.title_screen_info_overlay_is_loopback),
		IPV6(R.string.title_screen_info_overlay_ipv6_private),
		IS_LOOPBACK6(R.string.title_screen_info_overlay_is_loopback),
		IS_LINK_LOCAL6(R.string.title_screen_info_overlay_is_link_local),
		IPV4_PUB(R.string.title_screen_info_overlay_ipv4_public),
		IPV6_PUB(R.string.title_screen_info_overlay_ipv6_public), 
		UL_TRAFFIC(R.string.title_screen_info_overlay_ul_traffic), 
		DL_TRAFFIC(R.string.title_screen_info_overlay_dl_traffic), 
		CONTROL_SERVER_CONNECTION(R.string.title_screen_info_overlay_control_server_conn),
		CAPTIVE_PORTAL_STATUS(R.string.title_screen_info_overlay_captive_portal_status),
		LOCATION_ACCURACY(R.string.title_screen_info_overlay_location_accuracy),
		LOCATION_AGE(R.string.title_screen_info_overlay_location_age),
		LOCATION_SOURCE(R.string.title_screen_info_overlay_location_source),
		LOCATION_ALTITUDE(R.string.title_screen_info_overlay_location_altitude),
		LOCATION(R.string.title_screen_info_overlay_location_position);
		
		protected final int resourceId;
		protected final String title;
		
		private InfoOverlayEnum(final String title) {
			this.title = title;
			this.resourceId = -1;
		}
		
		private InfoOverlayEnum(final int resourceId) {
			this.resourceId = resourceId;
			this.title = null;
		}
		
		public int getResourceId() {
			return this.resourceId;
		}
		
		public String getTitle(Context context) {
			if (title != null) {
				return title;
			}
			
			return context.getString(resourceId);
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean onBackPressed() {
		int screenSize = getResources().getConfiguration().screenLayout &
		        Configuration.SCREENLAYOUT_SIZE_MASK;

		if (infoOverlay.getVisibility() == View.VISIBLE && screenSize < Configuration.SCREENLAYOUT_SIZE_LARGE) {
			infoOverlay.setVisibility(View.GONE);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (antennaView != null && antennaView.getTag() != null) {
			outState.putInt(BUNDLE_INFO_LAST_ANTENNA_IMAGE, (Integer) antennaView.getTag());
		}
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	LayoutInflater inflater = LayoutInflater.from(getActivity());
    	populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    /**
     * 
     * @param inflater
     * @param view
     */
	private void populateViewForOrientation(LayoutInflater inflater, ViewGroup view) {
		int antennaResId = Integer.MIN_VALUE;
		if (antennaView != null && antennaView.getTag() != null) {
			antennaResId = (Integer) antennaView.getTag();
		}
		
		view.removeAllViewsInLayout();
        View v = inflater.inflate(R.layout.title_screen, view);
        
        createView(v, inflater, null);
        
        if (antennaResId != Integer.MIN_VALUE) {
        	antennaView.setImageResource(antennaResId);
        	antennaView.setVisibility(View.VISIBLE);
        }
        
        //restore all information now:
        infoCollector.refresh();
        infoCollector.dispatchInfoChangedEvent(InfoCollectorType.DL_TRAFFIC, null, TrafficClassificationEnum.classify(interfaceTrafficGatherer.getRxRate()));
        infoCollector.dispatchInfoChangedEvent(InfoCollectorType.UL_TRAFFIC, null, TrafficClassificationEnum.classify(interfaceTrafficGatherer.getTxRate()));
        
    	if (startButtonText != null) {
        	startButtonText.setText(ConfigHelper.isLoopMode(getActivity()) ? R.string.menu_button_loop : R.string.menu_button_start);
        }
	}
	
	/**
	 * 
	 * @return
	 */
	public InformationCollector getInformationCollector() {
		return informationCollector;
	}

	public void setViewVisibility(View view, int visibility) {
		if (view != null && view.getVisibility()!=visibility) {
			view.setVisibility(visibility);
		}
	}
	
	public void setViewText(TextView view, String text) {
		setViewText(view, text, null);
	}
	
	public void setViewText(TextView view, String text, TextView alternativeView) {
		if (view != null) {
			view.setText(text);
		}
		else if (alternativeView != null) {
			alternativeView.setText(alternativeView.getText() + " " + text);
		}
	}
}
