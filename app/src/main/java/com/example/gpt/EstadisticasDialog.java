package com.example.gpt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.UUID;

//Simple dialog que se encarga de hacer una estadistica po medio de 3 queries, cada estadistica es por campaña
public class EstadisticasDialog extends DialogFragment {

    public static final String CAMPAÑA_ID = "campañaId";
    private SQLiteDatabase mDataBase;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context mContext = getActivity().getApplicationContext();
        mDataBase = new GPTBaseHelper(mContext).getWritableDatabase();
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.estadisticas_dialog, null);
        UUID campañaId = (UUID) getArguments().getSerializable("CAMPAÑA_ID");
        TextView mHembrasEditText = view.findViewById(R.id.numero_hembras);
        TextView mMachosEditText = view.findViewById(R.id.numero_machos);
        TextView mDineroTotal = view.findViewById(R.id.dinero_recabado);

        //Se obtiene la strins especifica de cada query como resultante obtenido, para poder mostrar en el dialog
        String hembras = Integer.toString(getEstadisticas("SELECT COUNT(*) FROM esterilizaciones INNER JOIN gatos ON esterilizaciones.gato_id = gatos.uuid WHERE esterilizaciones.campaña_id='" + campañaId.toString() + "' AND gatos.sexo='Hembra'"));
        String machos = Integer.toString(getEstadisticas("SELECT COUNT(*) FROM esterilizaciones INNER JOIN gatos ON esterilizaciones.gato_id = gatos.uuid WHERE esterilizaciones.campaña_id='" + campañaId.toString() + "' AND gatos.sexo='Macho'"));
        String precios = Integer.toString(getEstadisticas("SELECT SUM(esterilizaciones.precio + esterilizaciones.costo_extra) FROM esterilizaciones WHERE esterilizaciones.campaña_id = '" + campañaId.toString() + "' AND esterilizaciones.pagado=1"));

        mHembrasEditText.setText(hembras);
        mMachosEditText.setText(machos);
        mDineroTotal.setText("$"+precios);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Estadisticas de la campaña")
                .setIcon(R.drawable.campana)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    //Se manda a llamar a una query dependiendo del caso,
    public int getEstadisticas(String query){
        Cursor cursor = mDataBase.rawQuery(query,null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        return count;
    }
}


