package com.example.gpt;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.util.UUID;

//Fragmento para poder recibier los datos del gato y de la esterilizacion es su defecto y crearla
public class EsterilizacionFragment extends Fragment {
    private CheckBox mFajaCheckBox, mAnticipoCheckBox, mCostoExtraCheckButton;
    private EditText mAnticipoEditText, mCostoExtraEditText, mPrecioEditText;
    private TextView mCostoTotalTextView;
    private Button mPagadoButton;
    private UUID campañaId;
    private EventBus bus = EventBus.getDefault();
    private Esterilizacion mEsterilizacion = new Esterilizacion();
    private Gato mGato;
    private Persona mPersona;
    private boolean objetoEnviadoPersona = false;
    private boolean objetoEnviadoEsterilizacion = false;
    private CatLab mCatLab;
    private GatoHogarLab mGatoHogarLab;
    private PersonaStorage mPersonaStorage;
    EsterilizacionStorage mEsterilizacionStorage;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //Dentro de este apartado, se inica el registro para poder estar recibiendo los datos correspondientes sin tener que estar enviandio argumentos y recibiendolos cada rato
        //Esto es debido a que el usuario puede iterar entre los dos fragmentos con gran facilidad y la informacion se pierde
        //Cuando se registra, cuando el bus postee algo, inmediatamente llama el metodo correspondiente con el argumento que se envio y recibe el objeto
        bus.register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        mEsterilizacionStorage = EsterilizacionStorage.get(getActivity());
        campañaId = (UUID) getArguments().getSerializable("CAMPAÑA_ID");
        View view = inflater.inflate(R.layout.esterilizacion_datos_fragment, null);
        mFajaCheckBox = view.findViewById(R.id.faja);
        mAnticipoCheckBox = view.findViewById(R.id.anticipo);
        mAnticipoEditText = view.findViewById(R.id.anticipo_cantidad);
        mCostoExtraEditText = view.findViewById(R.id.costo_extra);
        mCostoTotalTextView = view.findViewById(R.id.costo_total);
        mPrecioEditText = view.findViewById(R.id.precio);
        Button mTerminarRegistroButton = view.findViewById(R.id.terminar_esterilizacion);
        mCostoExtraCheckButton = view.findViewById(R.id.costo_extra_check);
        mPagadoButton = view.findViewById(R.id.pagado);


        if(mEsterilizacion.ismPagado()){
            mPagadoButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        }else{
            mPagadoButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
        }
        if(objetoEnviadoEsterilizacion && mEsterilizacion.getmIdGato() != null){
            setHasOptionsMenu(true);
            mTerminarRegistroButton.setText(R.string.actualizar_datos);
            ColocarDatos();
        }


        mAnticipoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                   mAnticipoEditText.setVisibility(View.VISIBLE);

                }
                else {
                    mAnticipoEditText.setVisibility(View.GONE);
                }
            }
        });

        mCostoExtraCheckButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mCostoExtraEditText.setVisibility(View.VISIBLE);
                }
                else {
                    mCostoExtraEditText.setVisibility(View.GONE);
                }
            }
        });

        mAnticipoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(mAnticipoEditText.getVisibility()==View.VISIBLE){
                        mEsterilizacion.setmAnticipo(Integer.parseInt(mAnticipoEditText.getText().toString()));
                    }

                } catch (Exception e) {
                    mEsterilizacion.setmAnticipo(0);

                    e.printStackTrace();
                }
            }
        });

        mCostoExtraEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    if(mCostoExtraEditText.getVisibility()==View.VISIBLE){
                        mEsterilizacion.setmCostoExtra(Integer.parseInt(mCostoExtraEditText.getText().toString()));
                    }
                    int cantidad = mEsterilizacion.getmPrecio() + Integer.parseInt(mCostoExtraEditText.getText().toString());
                    mCostoTotalTextView.setText(getString(R.string.costo_total, cantidad));
                } catch (Exception e) {
                    e.printStackTrace();
                    mEsterilizacion.setmCostoExtra(0);
                    int cantidad = mEsterilizacion.getmPrecio();
                    mCostoTotalTextView.setText(getString(R.string.costo_total, cantidad));
                }

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPrecioEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    mEsterilizacion.setmPrecio(Integer.parseInt(mPrecioEditText.getText().toString()));
                    int cantidad =  mEsterilizacion.getmPrecio() + mEsterilizacion.getmCostoExtra();
                    mCostoTotalTextView.setText(getString(R.string.costo_total, cantidad));
                } catch (Exception e) {
                    mEsterilizacion.setmPrecio(0);
                    int cantidad = mEsterilizacion.getmCostoExtra();
                    mCostoTotalTextView.setText(getString(R.string.costo_total, cantidad));
                    e.printStackTrace();
                }

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mAnticipoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mAnticipoEditText.setVisibility(View.VISIBLE);
                }else {
                    mEsterilizacion.setmAnticipo(0);
                    mAnticipoEditText.setVisibility(View.GONE);
                }
            }
        });

        mFajaCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mEsterilizacion.setmFaja(isChecked);
            }
        });

        mCostoExtraCheckButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCostoExtraEditText.setVisibility(View.VISIBLE);
                }else {
                    mEsterilizacion.setmCostoExtra(0);
                    mCostoExtraEditText.setVisibility(View.GONE);
                }
            }
        });

        mPagadoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mEsterilizacion.ismPagado()){
                    mEsterilizacion.setmPagado(true);
                    mPagadoButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                }else{
                    mEsterilizacion.setmPagado(false);
                    mPagadoButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                }
            }
        });


        mTerminarRegistroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCatLab = CatLab.get(getActivity());
                mGatoHogarLab = GatoHogarLab.get(getActivity());
                mPersonaStorage = PersonaStorage.get(getActivity());
                //Dependiendo de si fue instanciado una nueva esterilizacion o fue enviada, se actualizaran o se crearan los datos
                if((objetoEnviadoEsterilizacion && mGato.isValidacion()) && mEsterilizacion.getmIdGato() != null){
                    actualizarDatos();
                    getActivity().finish();
                }
                else{
                    if(mGato.isValidacion() && mEsterilizacion.getmPrecio() != 0){
                        crearDatos();
                        getActivity().finish();
                    }
                    else{
                        Toast.makeText(getActivity(), "Datos incompletos",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return view;
        }

    private void ColocarDatos() {
        //Si es un objeto ya enviado, se colocarán los datos correspondientes
        mPrecioEditText.setText(Integer.toString(mEsterilizacion.getmPrecio()));
        int cantidad = mEsterilizacion.getmCostoExtra() + mEsterilizacion.getmPrecio();
        mCostoTotalTextView.setText(getString(R.string.costo_total, cantidad));
        if(mEsterilizacion.getmCostoExtra() != 0){
            mCostoExtraEditText.setVisibility(View.VISIBLE);
            mCostoExtraCheckButton.setChecked(true);
            mCostoExtraEditText.setText(Integer.toString(mEsterilizacion.getmCostoExtra()));
        }
        if(mEsterilizacion.getmAnticipo() != 0){
            mAnticipoEditText.setVisibility(View.VISIBLE);
            mAnticipoCheckBox.setChecked(true);
            mAnticipoEditText.setText(Integer.toString(mEsterilizacion.getmAnticipo()));
        }
        if(mEsterilizacion.ismFaja()){
            mFajaCheckBox.setChecked(true);
        }
        if(mEsterilizacion.ismPagado()){
            mPagadoButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        }
    }

    @Override
    public void onPause() {
        //Cuando entra en op pause, significa que cambió de fragmento, por lo que se mandan las variables creadas de este método para recibirlas en GatoFragment
        bus.post(mGato);
        bus.post(mEsterilizacion);
        if(mPersona!=null){
            bus.post(mPersona);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //Si se destgruye, significa que termina la actividad
        super.onDestroy();
        bus.unregister(this);
    }

    @Subscribe
    public void recibirGato(Gato gato){
        mGato = gato;
    }

    public void crearDatos(){
        //Se hacen las validaciones correspondientes para poder ingresar los datos en las respectivas tablas así como el ingreso
        if(mCatLab.getmGato(mGato.getmIdGato())==null){
            mCatLab.addGato(mGato, getActivity());
        }else{
            mCatLab.updateGato(mGato);
        }

        if(objetoEnviadoPersona){
            if(mPersonaStorage.getmPersona(mPersona.getmIdPersona()) == null){
                mPersonaStorage.addPersona(mPersona);
            }
            else{
                mPersonaStorage.updatePersona(mPersona);
            }

            if(mGatoHogarLab.getmGatoHogar(mGato.getmIdGato()) == null){
                GatoHogar mGatoHogar = new GatoHogar(mGato.getmIdGato());
                mGatoHogar.setmPersonaId(mPersona.getmIdPersona());
                mGatoHogarLab.addGatoHogar(mGatoHogar, getActivity());
            }
        }
        mEsterilizacion.setmIdCampaña(campañaId);
        mEsterilizacion.setmIdGato(mGato.getmIdGato());
        mEsterilizacionStorage.addEsterilizacion(mEsterilizacion, getActivity());
        CrearIngreso();
    }

    public void actualizarDatos(){
        //Se realizan las validaciones correspondiente spara actualizar los datos de las respectivas tablas
        if(objetoEnviadoPersona){
            GatoHogar mGatoHogar = new GatoHogar(mGato.getmIdGato());
            if(mPersonaStorage.getmPersona(mPersona.getmIdPersona()) == null){
                mPersonaStorage.addPersona(mPersona);
            }

            if(mGatoHogarLab.getmGatoHogar(mGato.getmIdGato()) == null){
                mGatoHogar.setmPersonaId(mPersona.getmIdPersona());
                mGatoHogarLab.addGatoHogar(mGatoHogar, getActivity());
            }
            else {
                mGatoHogar.setmPersonaId(mPersona.getmIdPersona());
            }
            mPersonaStorage.updatePersona(mPersona);
            mGatoHogarLab.updateGatoHogar(mGatoHogar);
        }
        else{
            if(mGatoHogarLab.getmGatoHogar(mGato.getmIdGato()) != null){
                mGatoHogarLab.deleteGatoHogar(GPTDbSchema.GatoHogarTable.Cols.FKUUID_GATO + " = ?", new String[]{mGato.getmIdGato().toString()});
            }
        }

        Ingreso mIngreso = IngresoBank.get(getActivity()).getmIngreso(mEsterilizacion.getmIdEsterilizacion());
        if (IngresoBank.get(getActivity()).getmIngreso(mEsterilizacion.getmIdEsterilizacion())!=null) {
            if(mEsterilizacion.ismPagado()){
                mIngreso.setmCantidad(mEsterilizacion.getmPrecio() + mEsterilizacion.getmCostoExtra());
                IngresoBank.get(getActivity()).updateIngreso(mIngreso,GPTDbSchema.IngresoTable.NAME,GPTDbSchema.IngresoTable.Cols.UUID);
            }else{
                IngresoBank.get(getActivity()).deleteIngreso(GPTDbSchema.IngresoTable.Cols.UUID + "= ?", new String[]{mIngreso.getmIdIngreso().toString()}, GPTDbSchema.IngresoTable.NAME);
            }
        }else{
            if(mEsterilizacion.ismPagado()){
                CrearIngreso();
            }
        }

        mCatLab.updateGato(mGato);
        mEsterilizacionStorage.updateEsterilizacion(mEsterilizacion);
    }

    public void CrearIngreso(){
        if(mEsterilizacion.ismPagado()){
            Ingreso mIngreso = new Ingreso(mEsterilizacion.getmIdEsterilizacion());
            Campaña mCampaña = CampañaStorage.get(getActivity()).getCampaña(mEsterilizacion.getmIdCampaña());
            mIngreso.setmAutomatico(true);
            mIngreso.setMotivo("Esterilizacion");
            mIngreso.setmCantidad(mEsterilizacion.getmPrecio() + mEsterilizacion.getmCostoExtra());
            mIngreso.setmFecha(mCampaña.getmFechaCampaña());
            IngresoBank.get(getActivity()).addIngreso(mIngreso, getActivity(),GPTDbSchema.IngresoTable.NAME );
        }
    }

    @Subscribe
    public void recibirEsterilizacion(Esterilizacion esterilizacion){
        mEsterilizacion = esterilizacion;
        objetoEnviadoEsterilizacion = true;
    }

    @Subscribe
    public void recibirResponsable(Persona persona){
        mPersona = persona;
        objetoEnviadoPersona = true;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.esterilizacion_borrar, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eliminar_esterilizacion:
                final AlertDialog.Builder mDeleteDialog = new AlertDialog.Builder(getActivity());
                mDeleteDialog.setTitle("Borrar esterilizacion")
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setMessage("¿Estas segura de querer borrar la esterilización?")
                        .setPositiveButton("ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Elimina el crimen de la BD y regresa a la pantalla anterior
                                        mEsterilizacionStorage.deleteEsterilizaciones(GPTDbSchema.EsterilizacionTable.Cols.UUID + "= ?", new String[] {mEsterilizacion.getmIdEsterilizacion().toString()});
                                        getActivity().finish();
                                    }
                                })
                        //Cancela la accion de delete
                        .setNegativeButton("cancelar",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                        .create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

