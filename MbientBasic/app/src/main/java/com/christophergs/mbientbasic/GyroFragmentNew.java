/*
 * Copyright 2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user who
 * downloaded the software, his/her employer (which must be your employer) and
 * MbientLab Inc, (the "License").  You may not use this Software unless you
 * agree to abide by the terms of the License which can be found at
 * www.mbientlab.com/terms . The License limits your use, and you acknowledge,
 * that the  Software may not be modified, copied or distributed and can be used
 * solely and exclusively in conjunction with a MbientLab Inc, product.  Other
 * than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this
 * Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * MBIENTLAB OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT, NEGLIGENCE,
 * STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED
 * TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST
 * PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY
 * DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact MbientLab Inc, at www.mbientlab.com.
 */

package com.christophergs.mbientbasic;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.components.YAxis;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.christophergs.mbientbasic.help.HelpOption;
import com.christophergs.mbientbasic.help.HelpOptionAdapter;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.data.Units;
import com.mbientlab.metawear.module.Gyro;
import com.mbientlab.metawear.module.Bmi160Gyro;
import com.mbientlab.metawear.module.Bmi160Gyro.*;

/**
 * Created by etsai on 8/19/2015.
 */
public class GyroFragmentNew extends ThreeAxisChartFragment {
    private static final float[] AVAILABLE_RANGES= {125.f, 250.f, 500.f, 1000.f, 2000.f};
    private static final float INITIAL_RANGE= 125.f, GYR_ODR= 50.f;
    private static final String STREAM_KEY= "gyro_stream";
    private static final String TAG = "MetaWear";

    private Bmi160Gyro gyroModule= null;
    private int rangeIndex= 0;

    public GyroFragmentNew() {
        super("rotation", R.layout.fragment_sensor_config_spinner2,
                R.string.navigation_fragment_gyro, STREAM_KEY, -INITIAL_RANGE, INITIAL_RANGE, GYR_ODR);
    }

    @Override
    protected void boardReady() throws UnsupportedModuleException {
        gyroModule= mwBoard.getModule(Bmi160Gyro.class);
    }

    @Override
    protected void fillHelpOptionAdapter(HelpOptionAdapter adapter) {
        adapter.add(new HelpOption(R.string.config_name_gyro_range, R.string.config_desc_gyro_range));
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final YAxis leftAxis = chart.getAxisLeft();

        ((TextView) view.findViewById(R.id.config_option_title)).setText(R.string.config_name_gyro_range);

        Spinner rotationRangeSelection= (Spinner) view.findViewById(R.id.config_option_spinner);
        rotationRangeSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rangeIndex = position;
                leftAxis.setAxisMaxValue(AVAILABLE_RANGES[rangeIndex]);
                leftAxis.setAxisMinValue(-AVAILABLE_RANGES[rangeIndex]);

                refreshChart(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<CharSequence> spinnerAdapter= ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.values_gyro_range, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rotationRangeSelection.setAdapter(spinnerAdapter);
        rotationRangeSelection.setSelection(rangeIndex);
    }

    @Override
    public void setup() {
        gyroModule.configure()
                .setOutputDataRate(OutputDataRate.ODR_50_HZ)
                .setFullScaleRange(FullScaleRange.FSR_500)
                .commit();

        AsyncOperation<RouteManager> routeManagerResult= gyroModule.routeData().fromAxes().stream(STREAM_KEY).commit();
        routeManagerResult.onComplete(dataStreamManager);
        routeManagerResult.onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                gyroModule.start();
                /*result.subscribe(STREAM_KEY, new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message msg) {
                        final long estimatedTime_gyro = System.nanoTime() - startTime_gyro;
                        final CartesianFloat spinData = msg.getData(CartesianFloat.class);
                        Log.i(TAG, String.format("Gyro_Log: %s, %d, %tY%<tm%<td-%<tH%<tM%<tS%<tL", spinData.toString(), estimatedTime_gyro, msg.getTimestamp()));
                    }
                });*/
            }
        });
    }

    @Override
    public void clean() {
        gyroModule.stop();
    }

    public String blah(String foo) {
        Log.i(TAG, String.format("blah says: %s", foo));
        return foo;
    }
}
