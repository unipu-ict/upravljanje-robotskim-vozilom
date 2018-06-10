package hr.unipu.app.upravljanjerobotskimvozilom;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

/**
 * Ova klasa služi za metode fragmenta sučelja.
 *
 * @author Leopold Juraga
 * @version 1.0
 */
public class SuceljeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    /**
     * Atributi za aktivnost.
     *
     * @param SERVICE_ID atribut za jedinstveni ID mobilne i things aplikacije
     * @param mGoogleApiClient atribut za Google Api Client
     * @param mEndpoint atribut za ono na šta će se Google Nearby spojiti
     * @param toggle atribut za Switch spajanja/odspajanja preko Google Nearby-a sa RoboCar-om
     * @param statusSwitch atribut za Switch stanja veze preko Google Nearby-a sa RoboCar-om
     * @param onoff atribut za dali je Switch toggle on ili off
     * @param connected atribut za dali je veza sa RoboCar-om preko Google Nearby-a uspostavljena
     * @param connectedString atribut za String kad je veza sa RoboCar-om preko Google Nearby-a spojena
     * @param disconnectedString atribut za String kad je veza sa RoboCar-om preko Google Nearby-a odspojena
     * @param TAG atribut za Log TAG
     */
    private static final String SERVICE_ID = "UPRAVLJANJE_ROBOTSKIM_VOZILOM_UNIQUE_SERVICE_ID";
    private GoogleApiClient mGoogleApiClient;
    private String mEndpoint;
    private Switch toggle;
    private Switch statusSwitch;
    boolean onoff = false;
    boolean connected = false;
    String connectedString;
    String disconnectedString;
    String TAG = "Test";

    /**
     * Metoda u kojoj se određuje šta će se dogoiti sa primljenim informacijama preko Google Nearby-a sa RoboCar-a.
     */
    private PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {

        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    /**
     * Metoda koja prati stanje veze sa RoboCar-om preko Google Nearby-a.
     */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, mPayloadCallback);
                    mEndpoint = endpointId;
                    Nearby.Connections.stopDiscovery(mGoogleApiClient);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    notification(connectedString);
                    connected = true;
                    Log.e(TAG, "Spojen na RoboCar");
                    statusSwitch.setChecked(true);
                    statusSwitch.setText(connectedString);
                    statusSwitch.setTextColor(Color.parseColor("#ff669900"));
                }

                @Override
                public void onDisconnected(String endpointId) {
                    notification(disconnectedString);
                    connected = false;
                    startDiscovery();
                    if (onoff) {
                        toogleOff();
                        toggle.setChecked(false);
                        onoff = false;
                    }
                    Log.e(TAG, "Odspojen sa RoboCar");
                    statusSwitch.setChecked(false);
                    statusSwitch.setText(disconnectedString);
                    statusSwitch.setTextColor(Color.parseColor("#ffcc0000"));
                }
            };

    /**
     * Metoda koja prati dali je RoboCar pronađen ili izgubljen preko Google Nearby-a.
     */
    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback = new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    if( discoveredEndpointInfo.getServiceId().equalsIgnoreCase(SERVICE_ID)) {
                        Nearby.Connections.requestConnection(mGoogleApiClient, "Droid", endpointId, mConnectionLifecycleCallback);
                    }
                }

                @Override
                public void onEndpointLost(String endpointId) {
                }
            };

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SuceljeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SuceljeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SuceljeFragment newInstance(String param1, String param2) {
        SuceljeFragment fragment = new SuceljeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Metoda koja se izvršava odmah kod kreiranja fragmenta.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        connectedString = getResources().getString(R.string.connected);
        disconnectedString = getResources().getString(R.string.disconnected);

        try {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity(), this, this).addApi(Nearby.CONNECTIONS_API).enableAutoManage(getActivity(), this).build();
        }catch (IllegalStateException e){

        }

        notification(disconnectedString);
        Log.d(TAG, "onoff: " + onoff);
    }

    /**
     * Metoda koja se izvršava odmah kod pokretanja fragmenta.
     */
    @Override
    public void onStart() {
        super.onStart();
        notification(disconnectedString);
        onoff = false;
        toggle.setChecked(false);

        // Call GoogleApiClient connection when starting the Activity
        try {
            mGoogleApiClient.connect();
        }catch (NullPointerException e){

        }
    }

    /**
     * Metoda u kojoj su definirani view za sve elemente sučelja.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_sucelje, container, false);
        toggle = (Switch) view.findViewById(R.id.onoff);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    toogleOn();
                    onoff = true;
                    toggle.setText(getResources().getString(R.string.disconnect));
                } else {
                    toogleOff();
                    onoff = false;
                    toggle.setText(getResources().getString(R.string.connect));
                    notification(disconnectedString);
                    statusSwitch.setChecked(false);
                    statusSwitch.setText(disconnectedString);
                    statusSwitch.setTextColor(Color.parseColor("#ffcc0000"));
                }
            }
        });
        statusSwitch = (Switch) view.findViewById(R.id.status);
        JoystickView joystick = (JoystickView) view.findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            public String smjer;
            @Override
            public void onMove(int angle, int strength) {
                Log.d("KutJacina", "Kut: " + angle + "Jacina: " + strength);
                if (angle >= 45 && angle <= 139){
                    smjer = "Napred";
                    send(smjer);
                    Log.d("Smjer", "Napred");
                }else if (angle >= 140 && angle <= 229){
                    smjer = "Lijevo";
                    send(smjer);
                    Log.d("Smjer", "Lijevo");
                }else if (angle >= 230 && angle <= 319){
                    smjer = "Nazad";
                    send(smjer);
                    Log.d("Smjer", "Nazad");
                }
                else if (angle >= 0 && angle <= 44){
                    smjer = "Desno";
                    send(smjer);
                    Log.d("Smjer", "Desno");
                }else if (angle >= 320 && angle <= 360){
                    smjer = "Desno";
                    send(smjer);
                    Log.d("Smjer", "Desno");
                }
                if (angle == 0 && strength == 0){
                    smjer = "Stop";
                    send(smjer);
                    Log.d("Smjer", "Stop");
                }
                Log.d("Kut", "Kut: " + angle);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Metoda koja se izvršava kad se stisne gumb za spajanje sa RoboCar-om.
     */
    private void toogleOn(){
        startDiscovery();
        Log.d(TAG, "startDiscovery toogleOn started");
        Log.d(TAG, "onoff: " + onoff);
    }

    /**
     * Metoda koja se izvršava kad se stisne gumb za odspajanje sa RoboCar-a.
     */
    private void toogleOff(){
        if (connected) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }else if (!connected) {
            try {
                Nearby.Connections.stopDiscovery(mGoogleApiClient);
            }catch (IllegalStateException e){

            }
        }
        connected = false;
        Log.d(TAG, "onoff: " + onoff);
    }

    /**
     * Metoda za traženje RoboCar-a preko Google Nearby-a koja se izvršava kad se stisne gumb za spajanje na RoboCar.
     */
    private void startDiscovery() {
        Log.d(TAG, "startDiscovery starting");
        try {
            Nearby.Connections.startDiscovery(
                    mGoogleApiClient,
                    SERVICE_ID,
                    mEndpointDiscoveryCallback,
                    new DiscoveryOptions(Strategy.P2P_STAR));
        }catch (IllegalStateException e){

        }
    }

    /**
     * Metoda za slanje smjera preko Google Nearby-a RoboCar-u.
     *
     * @param smjerZaSlanje atribut za smjer kretanja RoboCar-a
     */
    public void send(String smjerZaSlanje) {
        try {
            Nearby.Connections.sendPayload(mGoogleApiClient, mEndpoint, Payload.fromBytes(smjerZaSlanje.getBytes()));
        }catch (IllegalStateException e){

        }
    }

    /**
     * Metoda za kreiranje notifikacije za stanje konekcije preko Google Nearby-a sa RoboCar-om.
     *
     * @param connectionStatus atribut za stanje konekcije preko Google Nearby-a sa RoboCar-om
     */
    private void notification(String connectionStatus){
        String CHANNEL_ID = "Connection Status";
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder connectionStatusNotification = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getResources().getString(R.string.connection_status))
                .setContentText(connectionStatus)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setOngoing(true);
        int notificationId = 1;
        notificationManager.notify(notificationId, connectionStatusNotification.build());
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * Metoda koja se izvršava kad je veza preko Google Nearby-a sa RoboCar-om uspostavljena.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (onoff) {
            startDiscovery();
            Log.d(TAG, "startDiscovery onConnected started");
        }
    }

    /**
     * Metoda koja se izvršava kad je veza preko Google Nearby-a sa RoboCar-om prekinuta.
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Metoda koja se izvršava kad je veza preko Google Nearby-a sa RoboCar-om neuspjela.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Metoda koja se izvršava odmah kod zaustavljanja fragmenta.
     */
    @Override
    public void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        try {
            mGoogleApiClient.disconnect();
        }catch (NullPointerException e){

        }

        if (mGoogleApiClient != null){
            mGoogleApiClient.stopAutoManage(getActivity());
        }
    }

    /**
     * Metoda koja se izvršava odmah kod uništenja fragmenta.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        toogleOff();
    }
}
