package com.polidea.rxandroidble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.support.annotation.NonNull;

import com.polidea.rxandroidble.exceptions.BleCannotSetCharacteristicNotificationException;
import com.polidea.rxandroidble.exceptions.BleCharacteristicNotFoundException;
import com.polidea.rxandroidble.exceptions.BleConflictingNotificationAlreadySetException;
import com.polidea.rxandroidble.exceptions.BleGattCannotStartException;
import com.polidea.rxandroidble.exceptions.BleGattException;
import com.polidea.rxandroidble.exceptions.BleGattOperationType;

import java.util.UUID;

import rx.Observable;

/**
 * The BLE connection handle, supporting GATT operations. Operations are enqueued and the library makes sure that they are not
 * executed in the same time within the client instance.
 */
public interface RxBleConnection {

    interface Connector {

        Observable<RxBleConnection> prepareConnection(Context context, boolean autoConnect);
    }

    class RxBleConnectionState {

        public static final RxBleConnectionState CONNECTING = new RxBleConnectionState("CONNECTING");
        public static final RxBleConnectionState CONNECTED = new RxBleConnectionState("CONNECTED");
        public static final RxBleConnectionState DISCONNECTED = new RxBleConnectionState("DISCONNECTED");
        public static final RxBleConnectionState DISCONNECTING = new RxBleConnectionState("DISCONNECTING");
        private final String description;

        RxBleConnectionState(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "RxBleConnectionState{" + description + '}';
        }
    }

    /**
     * Performs GATT service discovery and emits discovered results. After service discovery you can walk through
     * {@link android.bluetooth.BluetoothGattService}s and {@link BluetoothGattCharacteristic}s.
     * <p>
     * Result of the discovery is cached internally so consecutive calls won't trigger BLE operation and can be
     * considered relatively lightweight.
     *
     * @return Observable emitting result a GATT service discovery.
     * @throws BleGattCannotStartException with {@link BleGattOperationType#SERVICE_DISCOVERY} type, when it wasn't possible to start
     *                                     the discovery for internal reasons.
     * @throws BleGattException            in case of GATT operation error with {@link BleGattOperationType#SERVICE_DISCOVERY} type.
     */
    Observable<RxBleDeviceServices> discoverServices();

    /**
     * Setup characteristic notification in order to receive callbacks when given characteristic has been changed. Returned observable will
     * emit Observable<byte[]> once the notification setup has been completed. It is possible to setup more observables for the same
     * characteristic and the lifecycle of the notification will be shared among them.
     * <p>
     * Notification is automatically unregistered once this observable is unsubscribed.
     *
     * NOTE: due to stateful nature of characteristics if one will setupIndication() before setupNotification()
     * the notification will not be set up and will emit an BleCharacteristicNotificationOfOtherTypeAlreadySetException
     *
     * @param characteristicUuid Characteristic UUID for notification setup.
     * @return Observable emitting another observable when the notification setup is complete.
     * @throws BleCharacteristicNotFoundException              if characteristic with given UUID hasn't been found.
     * @throws BleCannotSetCharacteristicNotificationException if setup process notification setup process fail. This may be an internal
     *                                                         reason or lack of permissions.
     * @throws BleConflictingNotificationAlreadySetException if indication is already setup for this characteristic
     */
    Observable<Observable<byte[]>> setupNotification(@NonNull UUID characteristicUuid);

    /**
     * Setup characteristic indication in order to receive callbacks when given characteristic has been changed. Returned observable will
     * emit Observable<byte[]> once the indication setup has been completed. It is possible to setup more observables for the same
     * characteristic and the lifecycle of the indication will be shared among them.
     * <p>
     * Indication is automatically unregistered once this observable is unsubscribed.
     *
     * NOTE: due to stateful nature of characteristics if one will setupNotification() before setupIndication()
     * the indication will not be set up and will emit an BleCharacteristicNotificationOfOtherTypeAlreadySetException
     *
     * @param characteristicUuid Characteristic UUID for indication setup.
     * @return Observable emitting another observable when the indication setup is complete.
     * @throws BleCharacteristicNotFoundException              if characteristic with given UUID hasn't been found.
     * @throws BleCannotSetCharacteristicNotificationException if setup process indication setup process fail. This may be an internal
     *                                                         reason or lack of permissions.
     * @throws BleConflictingNotificationAlreadySetException if notification is already setup for this characteristic
     */
    Observable<Observable<byte[]>> setupIndication(@NonNull UUID characteristicUuid);

    /**
     * Convenience method for characteristic retrieval. First step is service discovery which is followed by service/characteristic
     * traversal. This is an alias to:
     * <ol>
     * <li>{@link #discoverServices()}
     * <li>{@link RxBleDeviceServices#getCharacteristic(UUID)}
     * </ol>
     *
     * @param characteristicUuid Requested characteristic UUID.
     * @return Observable emitting matching characteristic or error if hasn't been found.
     * @throws BleCharacteristicNotFoundException if characteristic with given UUID hasn't been found.
     */
    Observable<BluetoothGattCharacteristic> getCharacteristic(@NonNull UUID characteristicUuid);

    /**
     * Performs GATT read operation on a characteristic with given UUID.
     *
     * @param characteristicUuid Requested characteristic UUID.
     * @return Observable emitting characteristic value or an error in case of failure.
     * @throws BleCharacteristicNotFoundException if characteristic with given UUID hasn't been found.
     * @throws BleGattCannotStartException        if read operation couldn't be started for internal reason.
     * @throws BleGattException                   if read operation failed
     */
    Observable<byte[]> readCharacteristic(@NonNull UUID characteristicUuid);

    /**
     * Performs GATT write operation on a characteristic with given UUID.
     *
     * @param characteristicUuid Requested characteristic UUID.
     * @return Observable emitting characteristic value after write or an error in case of failure.
     * @throws BleCharacteristicNotFoundException if characteristic with given UUID hasn't been found.
     * @throws BleGattCannotStartException        if write operation couldn't be started for internal reason.
     * @throws BleGattException                   if write operation failed
     */
    Observable<byte[]> writeCharacteristic(@NonNull UUID characteristicUuid, @NonNull byte[] data);

    /**
     * Performs GATT write operation on a characteristic with given UUID.
     *
     * @param bluetoothGattCharacteristic Characteristic to write. Use {@link BluetoothGattCharacteristic#setValue(byte[])} to set value.
     * @return Observable emitting characteristic after write or an error in case of failure.
     * @throws BleGattCannotStartException if write operation couldn't be started for internal reason.
     * @throws BleGattException            if write operation failed
     * @see #getCharacteristic(UUID) to obtain the characteristic.
     */
    Observable<BluetoothGattCharacteristic> writeCharacteristic(@NonNull BluetoothGattCharacteristic bluetoothGattCharacteristic);

    Observable<byte[]> readDescriptor(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid);

    Observable<byte[]> writeDescriptor(UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, byte[] data);

    Observable<Integer> readRssi();

    Observable<Integer> requestMtu(int mtu);
}
