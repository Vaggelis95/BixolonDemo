package com.zeustech.bixolontester;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import jpos.POSPrinterConst;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bxl.BXLConst;
import com.bxl.config.editor.BXLConfigLoader;
import com.zeustech.bixolontester.PrinterControl.BixolonPrinter;

import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String PRINTER_NAME = "SPP-R210";
    private BixolonPrinter printer;

    private ProgressBar progressBar;
    private Button openPrinter, closePrinter, printText;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // One Instance
        printer = BixPrinter.getInstance(getApplicationContext()).getBxlPrinter();

        progressBar = findViewById(R.id.progress_bar);
        openPrinter = findViewById(R.id.open_printer);
        closePrinter = findViewById(R.id.close_printer);
        editText = findViewById(R.id.edit_text);
        printText = findViewById(R.id.print_text);
        setUpListeners();
    }

    private void setUpListeners() {
        openPrinter.setOnClickListener(this);
        closePrinter.setOnClickListener(this);
        printText.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_printer : {
                mHandler.obtainMessage(0).sendToTarget();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String address = getPrinterAddress();
                        boolean success;
                        if (address != null) {
                            success = printer.printerOpen(
                                    BXLConfigLoader.DEVICE_BUS_BLUETOOTH,
                                    PRINTER_NAME,
                                    address,
                                    true);
                        } else {
                            success = false;
                        }
                        String message = success ? "Success Opened!!" : "Fail to printer open!!";
                        mHandler.obtainMessage(1, 0, 0, message).sendToTarget();
                    }
                }).start();
                break;
            }
            case R.id.close_printer : {
                printer.printerClose();
                break;
            }
            case R.id.print_text : {
                String strData = editText.getText().toString() + "\n";
                int alignment = BixolonPrinter.ALIGNMENT_LEFT; // alignment
                int attribute = 0;
                attribute |= BixolonPrinter.ATTRIBUTE_FONT_A; // font
                attribute |= BixolonPrinter.ATTRIBUTE_BOLD; // bold
                attribute |= BixolonPrinter.ATTRIBUTE_UNDERLINE; // underline
                printer.setCharacterSet(BXLConst.CS_1253_GREEK);

                printer.printText(strData, alignment, attribute, 2);
                printer.printBarcode(
                        strData,
                        POSPrinterConst.PTR_BCS_QRCODE,
                        8,
                        8,
                        POSPrinterConst.PTR_BC_CENTER,
                        POSPrinterConst.PTR_BC_TEXT_BELOW);
                break;
            }
            default: break;
        }
    }

    @Nullable
    private String getPrinterAddress() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDeviceSet) {
            if (Objects.equals(device.getName(), PRINTER_NAME)) {
                return device.getAddress();
            }
        }
        return null;
    }

    public final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            } else {
                String data = (String) msg.obj;
                if (data != null && data.length() > 0) {
                    Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(ProgressBar.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            return false;
        }
    });

}
