package hr.unipu.app.upravljanjerobotskimvozilom;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * Ova klasa služi za metode aktivnosti main.
 *
 * @author Leopold Juraga i Aljoša Kancijanić
 * @version 1.0
 */
public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Atributi za aktivnost.
     *
     * @param SERVICE_ID atribut za jedinstveni ID mobilne i things aplikacije
     * @param mGoogleApiClient atribut za Google Api Client
     * @param endpoint atribut za ono na šta će se Google Nearby spojiti
     * @param connectionStatusText atribut za TextView stanja veze preko Google Nearby-a sa mobilnom aplikacijom
     * @param smjerText atribut za TextView smjera RoboCar-a
     * @param connected atribut za dali je veza sa mobilnom aplikacijom preko Google Nearby-a uspostavljena
     * @param smjer atribut za String smjera RoboCar-a
     * @param TAG atribut za Log TAG
     * @param gpio1 atribut za Gpio 1
     * @param gpio2 atribut za Gpio 2
     * @param gpio3 atribut za Gpio 3
     * @param gpio4 atribut za Gpio 4
     */
    private static final String SERVICE_ID = "UPRAVLJANJE_ROBOTSKIM_VOZILOM_UNIQUE_SERVICE_ID";
    private GoogleApiClient mGoogleApiClient;
    private String endpoint;
    private TextView connectionStatusText;
    private TextView smjerText;
    boolean connected = false;
    String smjer;
    String TAG = "Test";
    private  Integer smjerValue=-1;
    private Gpio gpio1;
    private Gpio gpio2;
    private Gpio gpio3;
    private Gpio gpio4;

    /**
     * Metoda u kojoj se određuje šta će se dogoiti sa primljenim informacijama preko Google Nearby-a sa mobilne aplikacije.
     */
    private PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String s, Payload payload) {
            smjer = new String(payload.asBytes());
            if (smjer.equals("Napred")){
                Log.e(TAG, "Smjer: " + smjer);
                smjerText.setText(getResources().getString(R.string.forward));
                smjerValue = 1;
            }else if (smjer.equals("Lijevo")){
                Log.e(TAG, "Smjer: " + smjer);
                smjerText.setText(getResources().getString(R.string.left));
                smjerValue = 2;
            }else if (smjer.equals("Nazad")){
                Log.e(TAG, "Smjer: " + smjer);
                smjerText.setText(getResources().getString(R.string.back));
                smjerValue = 4;
            }else if (smjer.equals("Desno")){
                Log.e(TAG, "Smjer: " + smjer);
                smjerText.setText(getResources().getString(R.string.right));
                smjerValue = 3;
            }else if (smjer.equals("Stop")){
                Log.e(TAG, "Smjer: " + smjer);
                smjerText.setText(getResources().getString(R.string.stop));
                smjerValue=-1;
            }
            controll(smjerValue);
        }

        @Override
        public void onPayloadTransferUpdate(String s, PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    /**
     * Metoda koja prati stanje veze sa mobilnom aplikacijom preko Google Nearby-a.
     */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    endpoint = endpointId;

                    Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, mPayloadCallback).setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    if( status.isSuccess() ) {
                                        connectionStatusText.setText(getResources().getString(R.string.connected));
                                        Log.e(TAG, "connected");
                                    }
                                }
                            });
                    Nearby.Connections.stopAdvertising(mGoogleApiClient);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    connectionStatusText.setText(getResources().getString(R.string.connected));
                    Log.e(TAG, "connected");
                    connected = true;
                }

                @Override
                public void onDisconnected(String endpointId) {
                    connectionStatusText.setText(getResources().getString(R.string.disconnected));
                    Log.e(TAG, "disconnected");
                    if (connected) {
                        mGoogleApiClient.disconnect();
                        mGoogleApiClient.connect();
                    }else if (!connected) {
                        try {
                            Nearby.Connections.stopAdvertising(mGoogleApiClient);
                        }catch (IllegalStateException e){

                        }
                    }
                    if (!smjer.equals("Stop")) {
                        smjerText.setText(getResources().getString(R.string.stop));
                    }
                    connected = false;
                    startAdvertising();
                }
            };

    /**
     * Metoda koja se izvršava odmah kod kreiranja aktivnosti.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(Nearby.CONNECTIONS_API).enableAutoManage(this, this).build();

        connectionStatusText = (TextView) findViewById(R.id.connectionStatus);
        smjerText = (TextView) findViewById(R.id.smjer);

        smjer = "Stop";
        PeripheralManager service = PeripheralManager.getInstance();
        Log.d(TAG, "Available GPIO: " + service.getGpioList());
        try {
            gpio1 = service.openGpio("BCM12");
            gpio1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            gpio2 = service.openGpio("BCM16");
            gpio2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            gpio3 = service.openGpio("BCM20");
            gpio3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            gpio4 = service.openGpio("BCM21");
            gpio4.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException ex) {
            Log.e(TAG, "Error on PeripheralIO API", ex);
        }
    }

    /**
     * Metoda za kontrolu Gpio vrijednosti.
     *
     * @param value atribut za odabir koji case se treba izvršiti
     */
    private void controll(int value){
        try {
            switch (value) {
                case 1:
                    gpio1.setValue(true);
                    gpio3.setValue(true);
                    Log.e(TAG, "Moving naprijed");
                    break;
                case 2:
                    gpio3.setValue(true);
                    Log.e(TAG, "Moving Lijevo");
                    break;
                case 3:
                    gpio2.setValue(true);
                    Log.e(TAG, "Moving Desno");
                    break;
                case 4:
                    gpio2.setValue(true);
                    gpio4.setValue(true);
                    Log.e(TAG, "Moving nazad");
                    break;
                case -1:
                    gpio1.setValue(false);
                    gpio2.setValue(false);
                    gpio3.setValue(false);
                    gpio4.setValue(false);
                    Log.e(TAG, "STOP!!!!!!");
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Metoda za traženje mobilne aplikacije preko Google Nearby-a koja se izvršava kad se veza sa mobilnom aplikacijom izgubi ili se mobilna aplikacija odspoji.
     */
    private void startAdvertising() {
        Log.d(TAG, "startAdvertising started");
        try {
            Nearby.Connections.startAdvertising(
                    mGoogleApiClient,
                    "Droid",
                    SERVICE_ID,
                    mConnectionLifecycleCallback,
                    new AdvertisingOptions(Strategy.P2P_STAR));
        }catch (IllegalStateException e){

        }
    }

    /**
     * Metoda koja se izvršava kad je veza preko Google Nearby-a sa mobilnom aplikacijom uspostavljena.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startAdvertising();
    }

    /**
     * Metoda koja se izvršava kad je veza preko Google Nearby-a sa mobilnom aplikacijom prekinuta.
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Metoda koja se izvršava kad je veza preko Google Nearby-a sa mobilnom aplikacijom neuspjela.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Metoda koja se izvršava odmah kod uništenja aktivnosti.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (gpio1 != null) {
            try {
                gpio1.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (gpio2 != null) {
            try {
                gpio2.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (gpio3 != null) {
            try {
                gpio3.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (gpio4 != null) {
            try {
                gpio4.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }
}