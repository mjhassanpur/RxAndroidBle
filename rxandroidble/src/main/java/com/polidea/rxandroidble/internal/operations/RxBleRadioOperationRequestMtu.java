package com.polidea.rxandroidble.internal.operations;

import android.bluetooth.BluetoothGatt;
import com.polidea.rxandroidble.exceptions.BleGattCannotStartException;
import com.polidea.rxandroidble.exceptions.BleGattOperationType;
import com.polidea.rxandroidble.internal.RxBleRadioOperation;
import com.polidea.rxandroidble.internal.connection.RxBleGattCallback;
import rx.Subscription;

public class RxBleRadioOperationRequestMtu extends RxBleRadioOperation<Integer> {

  private final RxBleGattCallback bleGattCallback;

  private final BluetoothGatt bluetoothGatt;

  private final int mtu;

  public RxBleRadioOperationRequestMtu(RxBleGattCallback bleGattCallback, BluetoothGatt bluetoothGatt,
      int mtu) {
    this.bleGattCallback = bleGattCallback;
    this.bluetoothGatt = bluetoothGatt;
    this.mtu = mtu;
  }

  @Override
  public void run() {
    //noinspection Convert2MethodRef
    final Subscription subscription = bleGattCallback
        .getOnMtuChanged()
        .take(1)
        .doOnCompleted(() -> releaseRadio())
        .subscribe(getSubscriber());

    final boolean success = bluetoothGatt.requestMtu(mtu);
    if (!success) {
      subscription.unsubscribe();
      onError(new BleGattCannotStartException(BleGattOperationType.ON_MTU_CHANGED));
    }
  }
}
