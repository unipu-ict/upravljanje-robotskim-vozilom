package hr.unipu.app.upravljanjerobotskimvozilom;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Ova klasa služi za metode aktivnosti main.
 *
 * @author Leopold Juraga
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    int LOCATION_PERMISSION_REQUEST_CODE = 1;
    SuceljeFragment suceljeFragment;
    UputeFragment uputeFragment;
    OprogramuFragment oprogramuFragment;
    String TAG = "startDiscovery";

    /**
     * Metoda koja se izvršava odmah kod kreiranja aktivnosti.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        suceljeFragment = new SuceljeFragment();
        uputeFragment = new UputeFragment();
        oprogramuFragment = new OprogramuFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, suceljeFragment, "suceljeFragment").commit();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            showPermissionDialog();
        }
    }

    /**
     * Metoda za prozor u kojem se traži dopuštenje lokacije.
     */
    private void showPermissionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.location_permision_message_title));
        builder.setMessage(getResources().getString(R.string.location_permision_message_text));
        builder.setPositiveButton(getResources().getString(R.string.location_permision_message_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.location_permision_message_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });
        AlertDialog permissionDialog = builder.create();
        permissionDialog.show();
    }

    /**
     * Metoda koja se izvršava odmah kod korisnikovog odabira dali će dozvoliti aplikaciji dopuštenje lokacije.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                finish();
                System.exit(0);
            }
        }
    }

    /**
     * Metoda koja stvara meni.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
    }

    /**
     * Metoda za akcije stavki menija.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment previusFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        Log.d(TAG, "previusFragment: " + previusFragment);
        if (id == R.id.action_instructions) {
            if (suceljeFragment != null && suceljeFragment.isVisible()) {
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, uputeFragment).hide(suceljeFragment).commit();
            }
            else if (suceljeFragment != null && suceljeFragment.isHidden()) {
                getSupportFragmentManager().beginTransaction().remove(previusFragment).add(R.id.fragmentContainer, uputeFragment).commit();
            }
        }
        if (id == R.id.action_about) {
            if (suceljeFragment != null && suceljeFragment.isVisible()) {
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, oprogramuFragment).hide(suceljeFragment).commit();
            }
            else if (suceljeFragment != null && suceljeFragment.isHidden()) {
                getSupportFragmentManager().beginTransaction().remove(previusFragment).add(R.id.fragmentContainer, oprogramuFragment).commit();
            }
        }
        if (id == R.id.action_exit) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metoda koja se izvršava kad se stisne Back tipka.
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.exit_title)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.exit_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();
                        finish();
                        System.exit(0);
                    }
                }).setNegativeButton(R.string.exit_no, null).show();
    }

    /**
     * Metoda koja se izvršava odmah kod zaustavljanja aktivnosti.
     */
    @Override
    public void onStop(){
        super.onStop();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}