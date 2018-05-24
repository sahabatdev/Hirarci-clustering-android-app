package com.datamining.hciris;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    HashMap<String,Model> hashData = new HashMap<>();
    HashMap<String,Model> hashBase = new HashMap<>();
    List<HashMap<String,Model>> hashHasil = new ArrayList<>();
    List<List<String>> dataCluster = new ArrayList<>();
    List<Double> listVariant = new ArrayList<>();
    List<Model> listRataRataPerCluster = new ArrayList<>();
    List<Integer> listDataPerCluster = new ArrayList<>();
    Double[][] dataMatriks = new Double[150][150];
    Boolean[][] isLooping = new Boolean[150][150];
    Double sepalLength, sepalWidth, petalWidth, petalLength, sepalLengthHash, sepalWidthHash, petalWidthHash, petalLengthHash;
    TextView tvHasil;
    EditText etTarget;
    Button btnProses, btnEror, btnVariant;
    private int target=0;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading Proses");
        progress.setCancelable(false);

        tvHasil = findViewById(R.id.tv_hasil);
        etTarget = findViewById(R.id.et_target);
        btnProses = findViewById(R.id.btn_proses);
        btnEror = findViewById(R.id.btn_eror);
        btnVariant = findViewById(R.id.btn_variant);

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etTarget.getText().toString().isEmpty() && !etTarget.getText().toString().equals("0")) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                target = Integer.parseInt(etTarget.getText().toString());
                                InisialisasiGetDataFromJson();
                                fillArrayIsLooping(isLooping);
                                if (prosesHirarkiClustering(target)) return;
                                switchToArray();
                                showDataCluster();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();
                }else{
                    Toast.makeText(MainActivity.this, "Harap memasukkan jumlah cluster yang beanr", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnVariant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double withinVar = getWithinVariant();
                Double beetweenVar = getVarianBeetween();
                Log.d("HASIL VARIANT WITHIN",withinVar+"");
                Log.d("HASIL VARIANT BEETWEEN",beetweenVar+"");
                Double allVariant = withinVar/beetweenVar;
                Log.d("HASIL VARIANT ALL",allVariant+"");
            }
        });

        btnEror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String[][] strLabel = {
                                    {"setosa","virginica","versicolor"}
                                    ,{"setosa","versicolor","virginica"}
                                    ,{"virginica","setosa","versicolor"}
                                    ,{"virginica","versicolor","setosa"}
                                    ,{"versicolor","virginica","setosa"}
                                    ,{"versicolor","setosa","virginica"}};
                            int tempMin=150;
                            for(int j = 0 ; j<strLabel.length ;j++) {
                                int k = 0;
                                int selisih=0;
                                Log.d("SIZE",""+hashHasil.size());
                                for (HashMap<String, Model> l : hashHasil) {
                                    String label = strLabel[j][k];
                                    Log.d("SIZE",""+l.size());
                                    for (Map.Entry<String, Model> m : l.entrySet()) {
                                        Log.d("LABEL",label +" - "+hashBase.get(m.getKey()).getSpecies());
                                        if(!label.equals(hashBase.get(m.getKey()).getSpecies())){
                                            selisih++;
                                        }
                                    }
                                    k++;
                                }
                                if(selisih<tempMin){
                                    tempMin = selisih;
                                }
                                Log.d("SELISIH", ""+selisih);
                            }

                            Integer persen = (tempMin*100)/150;
                            Log.d("PERSENTASE RATIO EROR", ""+persen+" %");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        });
    }

    private void showDataCluster() {
        int j = 0;
        for(List<String> lim : dataCluster){
            System.out.print("ATAS ==============================================================\n");
            System.out.print("JUMLAH : "+lim.size()+" Data \n");
            for(String s : lim){
                System.out.print("INDEX : "+s+" Species : "+hashBase.get(s).getSpecies()+"\n");
            }
            System.out.println(hashHasil.get(j).get(lim.get(0)).getPetalLength());
            System.out.println(hashHasil.get(j).get(lim.get(0)).getPetalWidth());
            System.out.println(hashHasil.get(j).get(lim.get(0)).getSepalLength());
            System.out.println(hashHasil.get(j).get(lim.get(0)).getSepalWidth());
            System.out.println(hashHasil.get(j).get(lim.get(0)).getSpecies());
            Double variant = getVariant(lim.size(),hashHasil.get(j));
            listVariant.add(variant);
            System.out.print("VARIANT CLUSTER : "+ variant +"\n");
            System.out.print("BAWAH ==============================================================\n");
            j++;
        }
    }

    private void switchToArray() {
        for(Map.Entry<String,Model> m:hashData.entrySet()){
            List<String> l = new ArrayList<>();
            HashMap<String,Model> hm = new HashMap<>();
            l.add(m.getKey());
            hm.put(m.getKey(),hashBase.get(m.getKey()));
                for(Integer d : m.getValue().getNode()){
                    l.add(String.valueOf(d));
                    hm.put(String.valueOf(d),hashBase.get(m.getKey()));
                }
            hashHasil.add(hm);
            dataCluster.add(l);
        }
    }

    private boolean prosesHirarkiClustering(int target) {
        while (hashData.size()>target){
            int indeks1=0, indeks2=0;
            Double minValue = 9999.0;
            for (int k = 0; k < 150; k++) {
                for (int l = 0; l < 150; l++) {
                    if(isLooping[k][l]){
                        sepalLength = hashData.get(String.valueOf(k)).getSepalLength() - hashData.get(String.valueOf(l)).getSepalLength();
                        sepalWidth = hashData.get(String.valueOf(k)).getSepalWidth() - hashData.get(String.valueOf(l)).getSepalWidth();
                        petalWidth = hashData.get(String.valueOf(k)).getPetalWidth() - hashData.get(String.valueOf(l)).getPetalWidth();
                        petalLength = hashData.get(String.valueOf(k)).getPetalLength() - hashData.get(String.valueOf(l)).getPetalLength();

                        dataMatriks[k][l]=Math.sqrt(Math.pow(sepalLength, 2) + Math.pow(sepalWidth, 2) + Math.pow(petalWidth, 2) + Math.pow(petalLength, 2));
                        if(k!=l && dataMatriks[k][l]>0.0) {
                            if (dataMatriks[k][l] < minValue) {
                                minValue = dataMatriks[k][l];
                                indeks1 = k;
                                indeks2 = l;
                            }
                        }
                    }
                }
            }
            if(minValue!=9999.0) {
                Log.d("DATA MINIMAL ", indeks1 + " : " + indeks2 + " : " + minValue);
                for (int o = 0; o < 150; o++) {
                    isLooping[o][indeks2] = false;
                    isLooping[indeks2][o] = false;
                }
                sepalLengthHash = (hashData.get(String.valueOf(indeks1)).getSepalLength() + hashData.get(String.valueOf(indeks2)).getSepalLength()) / 2;
                sepalWidthHash = (hashData.get(String.valueOf(indeks1)).getSepalWidth() + hashData.get(String.valueOf(indeks2)).getSepalWidth()) / 2;
                petalWidthHash = (hashData.get(String.valueOf(indeks1)).getPetalWidth() + hashData.get(String.valueOf(indeks2)).getPetalWidth()) / 2;
                petalLengthHash = (hashData.get(String.valueOf(indeks1)).getPetalLength() + hashData.get(String.valueOf(indeks2)).getPetalLength()) / 2;
                List<Integer> lis = hashData.get(String.valueOf(indeks1)).getNode();
                lis.addAll(hashData.get(String.valueOf(indeks2)).getNode());
                lis.add(indeks2);
                hashData.remove(String.valueOf(indeks1));
                hashData.remove(String.valueOf(indeks2));
                hashData.put(String.valueOf(indeks1), new Model(sepalLengthHash, sepalWidthHash, petalLengthHash, petalWidthHash, lis));
            }else{
                return true;
            }
        }
        return false;
    }

    private void fillArrayIsLooping(Boolean[][] isLooping) {
        for(int k=0 ; k<150 ; k++){
            for(int l=0 ; l < 150 ; l++){
                isLooping[k][l] = true;
            }
        }
    }

    private void InisialisasiGetDataFromJson() {
        Gson gson = new Gson();
        List<Model> list =  gson.fromJson(loadJSONFromAsset(), new TypeToken<List<Model>>(){}.getType());
        int i=0;
        for(Model m : list){
            hashData.put(String.valueOf(i),new Model(m.getSepalLength(),m.getSepalWidth(),m.getPetalLength(),m.getPetalWidth(),new ArrayList<Integer>()));
            hashBase.put(String.valueOf(i),new Model(m.getSepalLength(),m.getSepalWidth(),m.getPetalLength(),m.getPetalWidth(),m.getSpecies()));
            i++;
        }
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("iris.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public Double getVariant(int n, HashMap<String,Model> list){
        listDataPerCluster.add(n);
        Double sepalL=0.0, sepalW=0.0, petalW=0.0, petalL=0.0, avgSL=0.0, avgSW=0.0, avgPW=0.0, avgPL=0.0;

        for(Map.Entry<String,Model> m : list.entrySet()){
            sepalL+=m.getValue().getSepalLength();
            sepalW+=m.getValue().getSepalWidth();
            petalW+=m.getValue().getPetalWidth();
            petalL+=m.getValue().getPetalLength();
        }
        avgSL = sepalL/n;
        avgSW = sepalW/n;
        avgPL = petalL/n;
        avgPW = petalW/n;

        listRataRataPerCluster.add(new Model(avgSL,avgSW,avgPL,avgPW));

        Double v2 = 0.0;
        Double tmp = 0.0;
        for(Map.Entry<String,Model> m : list.entrySet()){
            tmp += (Math.pow(m.getValue().getSepalLength()-avgSL,2) + Math.pow(m.getValue().getSepalWidth()-avgSW,2) + Math.pow(m.getValue().getPetalLength()-avgPL,2) + Math.pow(m.getValue().getPetalWidth()-avgPW,2));
        }

        Double dou = 1/(Double.valueOf(n)-1.0);
        v2 = dou * tmp;

        Log.d("Nilai","N : "+n +" DOUBLE : "+dou+" V2 : "+v2);

        return v2;

        //dAverage = rata-rata dari data pada suatu cluster
        //d[i] = data ke i


    }

    private Double getWithinVariant() {
        /*+--------------------------------------------------------+*/
        //Variance within cluster
        Double temp=0.0; //variance within
        Double nN = 150.0; //jumlah semua data
        for (int i=0; i < target; i++) {
            temp += (listDataPerCluster.get(i)-1) * listVariant.get(i);
            Log.d("LIST VARIANT",i+" - "+listVariant.get(i));
        }

        Double dou = (1/(nN-Double.valueOf(target)));

        return (dou*temp);
    }

   private Double getVarianBeetween() {
        /*+--------------------------------------------------------+*/
        //Variance within cluster

       Double sepalL=0.0, sepalW=0.0, petalW=0.0, petalL=0.0, avgSL=0.0, avgSW=0.0, avgPW=0.0, avgPL=0.0;

       for(Model m : listRataRataPerCluster){
           sepalL+=m.getSepalLength();
           sepalW+=m.getSepalWidth();
           petalW+=m.getPetalWidth();
           petalL+=m.getPetalLength();
       }
       avgSL = sepalL/target;
       avgSW = sepalW/target;
       avgPL = petalL/target;
       avgPW = petalW/target;

        Double temp=0.0; //variance within
        Double nN = 150.0; //jumlah semua data

        for (Model m : listRataRataPerCluster) {
            temp += (Math.pow(m.getSepalLength()-avgSL,2) + Math.pow(m.getSepalWidth()-avgSW,2) + Math.pow(m.getPetalLength()-avgPL,2) + Math.pow(m.getPetalWidth()-avgPW,2));
        }

        Double dou = (1/(Double.valueOf(target)-1.0));

        return dou*temp;
    }


}
