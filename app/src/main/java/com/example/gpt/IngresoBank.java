package com.example.gpt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//SingleTon para los ingresos
public class IngresoBank {
    private static IngresoBank sIngresoBank;
    private Context mContext;
    private SQLiteDatabase mDataBase;


    public static IngresoBank get(Context context){
        if( sIngresoBank== null){
            sIngresoBank = new IngresoBank(context);
        }
        return sIngresoBank;
    }

    private IngresoBank(Context context) {
        mContext = context.getApplicationContext();
        mDataBase = new GPTBaseHelper(mContext).getWritableDatabase();
    }


    public void addIngreso(Ingreso i, Context context, String table){
        ContentValues values= getContentValues(i);
        mDataBase.insert(table, null, values);
    }

    public void updateIngreso(Ingreso ingreso, String table, String clause){
        String uuidString = ingreso.getmIdIngreso().toString();
        ContentValues values = getContentValues(ingreso);
        mDataBase.update(table, values, clause + "= ?", new String[] {uuidString});
    }

    public void deleteIngreso( String whereClause, String[] whereArgs, String table){
        mDataBase.delete(
                table,
                whereClause,
                whereArgs
        );
    }
    public void deleteIngresos( String query){
        mDataBase.execSQL(query);
    }

    List<Ingreso> getmIngresos(String query){
        List <Ingreso> ingresos = new ArrayList<>();
        GPTCursorWrapper cursor = (GPTCursorWrapper) queryIngresos(query);
        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                //Envia los datos encontrados para que sean inicializados
                ingresos.add(cursor.getIngreso());
                cursor.moveToNext();
            }
        }
        finally {
            cursor.close();
        }
        return ingresos;
    }


    public Ingreso getmIngreso(UUID id){
        GPTCursorWrapper cursor = (GPTCursorWrapper) queryIngreso(
                GPTDbSchema.IngresoTable.Cols.UUID + " = ? ",
                new String[] {id.toString()}
        );

        try {
            if(cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            //Regresa el objeto con los datos de la campaña correspondiente
            return cursor.getIngreso();
        }
        finally {
            cursor.close();
        }
    }

    private CursorWrapper queryIngresos(String query){
        Cursor cursor = mDataBase.rawQuery(query, null);
        return new GPTCursorWrapper(cursor);
    }

    private CursorWrapper queryIngreso(String whereClause, String[] whereArgs){
        Cursor cursor = mDataBase.query(
                GPTDbSchema.IngresoTable.NAME,
                null, // column - null selects all columns
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new GPTCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Ingreso ingreso){
        ContentValues values = new ContentValues();
        values.put(GPTDbSchema.IngresoTable.Cols.UUID, ingreso.getmIdIngreso().toString());
        values.put(GPTDbSchema.IngresoTable.Cols.CANTIDAD, ingreso.getmCantidad());
        values.put(GPTDbSchema.IngresoTable.Cols.MOTIVO, ingreso.getMotivo());
        values.put(GPTDbSchema.IngresoTable.Cols.AUTOMATICO, ingreso.ismAutomatico());
        values.put(GPTDbSchema.IngresoTable.Cols.FECHA, ingreso.getmFecha().getTime());
        return values;
    }
}
