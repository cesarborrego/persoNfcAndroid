package c.neo.tolldemoperso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MenuPersoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_perso);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_perso, menu);
		return true;
	}
	
	public void openTollPerso(View view){
		Intent intentPerso = new Intent(this, PersoTagActivity.class);
		 startActivity(intentPerso);
	}
	
	public void openParkingPerso(View view){
		Intent intentPerso = new Intent(this, PersoParkingActivity.class);
		 startActivity(intentPerso);
	}
	
	public void openCedulaBOPerso(View view){
		Intent intentPerso = new Intent(this, PersoCedula_BOActivity.class);
		 startActivity(intentPerso);
	}
	
	public void openFrooviePerso(View view){
		Intent intentPerso = new Intent(this, PersoFroovieActivity.class);
		startActivity(intentPerso);
	}

	public void openLicenseCIPerso(View view){
		Intent intentPerso = new Intent(this, PersoLicense_CIActivity.class);
		startActivity(intentPerso);
	}
	
	public void openOcesaDemoPerso(View view){
		Intent intentPerso = new Intent(this, PersoOcesaDemoActivity.class);
		startActivity(intentPerso);
	}
	
	public void openSkiDataPerso(View view){
		Intent intentPerso = new Intent(this, PersoSkiDataActivity.class);
		startActivity(intentPerso);
	}
	
	public void openTarjetasCirculacionECUPerso(View view){
		Intent intentPerso = new Intent(this, PersoTarjetaCirculacionECActivity.class);
		startActivity(intentPerso);
	}
	
	public void openTLRequisicion(View view){
		Intent intentPerso = new Intent(this, PersoTLRequisicionActivity.class);
		startActivity(intentPerso);
	}
	
	public void openPRAPerso(View view){
		Intent intentPerso = new Intent(this, PersoEC_ArmasActivity.class);
		startActivity(intentPerso);
	}
	
	public void openCedulaNEOPerso(View view){
		Intent intentPerso = new Intent(this, PersoCedula_NEOActivity.class);
		 startActivity(intentPerso);
	}
	
	public void openMetPerso(View view){
		Intent intentPerso = new Intent(this, PersoPoliza_MetlifeActivity.class);
		 startActivity(intentPerso);
	}
	
	public void openBoAutoPerso(View view){
		Intent intentPerso = new Intent(this, PersoBO_Auto_Activity.class);
		 startActivity(intentPerso);
	}
	
	public void openCombustibleEC(View view){
		Intent intentPerso = new Intent(this, PersoCombustible_EC_Activity.class);
		 startActivity(intentPerso);
	}
	
	public void openFidelityCard(View view){
		Intent intentPerso = new Intent(this, PersoFidelity_Card.class);
		 startActivity(intentPerso);
	}
	
	public void openTitulos(View view){
		Intent intentPerso = new Intent(this, PersoTitulos.class);
		 startActivity(intentPerso);
	}
	
	public void openUmsa(View view){
		Intent intentPerso = new Intent(this, Perso_UMSA_ID.class);
		 startActivity(intentPerso);
	}
	
	public void openEngomado(View view){
		Intent intentPerso = new Intent(this, Perso_Validacion_Placas.class);
		 startActivity(intentPerso);
	}
}
